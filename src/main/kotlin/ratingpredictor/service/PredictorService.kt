package ratingpredictor.service

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.notify
import okhttp3.internal.wait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ratingpredictor.constants.Location
import ratingpredictor.dto.ContestDto
import ratingpredictor.dto.ParticipantDto
import ratingpredictor.dto.UserDataDto
import ratingpredictor.exceptions.WrongContestNameException
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

@Service
@RequiredArgsConstructor
class PredictorService(
    private val objectMapper: ObjectMapper,
    private val client: OkHttpClient
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }


    fun predict(contestName: String, username: String): Double {
        val sanitizedName = getSanitizedName(contestName)
        val mergedContest = ContestDto.builder()
            .contestName(sanitizedName)
            .time(getTotalPagesAndTime("${Location.US.url}/contest/api/ranking/${sanitizedName}/?pagination=1&region=${Location.US.region}").second)
            .build()

        for (location in Location.entries) {
            val (totalPages, _) = getTotalPagesAndTime("${location.url}/contest/api/ranking/${sanitizedName}/?pagination=1&region=${location.region}")

            val participants = fetchContest(location, sanitizedName, totalPages).participants
            mergedContest.participants.addAll(participants)
        }

        return predict(mergedContest, username)
    }

    private fun predict(contest: ContestDto, currentUser: String): Double {
        val participants = contest.participants
        val userDataList = fetchUsers(participants)


    }

    private fun fetchUsers(participants: ArrayList<ParticipantDto>): ArrayList<UserDataDto> {
        val result = ArrayList<UserDataDto>()
        val lock = Any()
        val counter = AtomicInteger(participants.size)

        for (participant in participants) {
            val url = Location.valueOf(participant.region).url
            val username = participant.username
            val mediaType = "application/json".toMediaType()
            val body =
                "{\"query\":\"query userContestRankingInfo(\$username: String!) { userContestRanking(username:\$username) { attendedContestsCount rating globalRanking } }\",\"variables\":{\"username\":\"$username\"}}"
                    .toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$url/graphql/")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    if (counter.decrementAndGet() == 0) {
                        lock.notify()
                    }
                    log.error(e.message) // should be resolved through retry policy, if leetcode down Runtime
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()!!
                    val node = objectMapper.readTree(responseString).get("data").get("userContestRanking")

                    if (node.isNull) {
                        result.add(
                            UserDataDto.builder()
                                .username(username)
                                .currentRating(1500.0)
                                .attendedContestsCount(0)
                                .build()
                        )
                    } else {
                        result.add(
                            UserDataDto.builder()
                                .username(username)
                                .currentRating(node.get("rating").asDouble())
                                .attendedContestsCount(node.get("attendedContestsCount").asInt())
                                .build()
                        )
                    }
                    if (counter.decrementAndGet() == 0) {
                        lock.notify()
                    }
                }
            })
        }

        lock.wait()

        return result
    }

    private fun fetchContest(location: Location, contestName: String, totalPages: Int): ContestDto {
        val participants = ArrayList<ParticipantDto>()
        var time = 0.0
        val lock = Any()
        val counter = AtomicInteger(totalPages)
        val url = location.url
        val region = location.region

        for (pageNum in 1..totalPages) {
            val request = Request.Builder()
                .get()
                .url("${url}/contest/api/ranking/${contestName}/?pagination=${pageNum}&region=${region}")
                .addHeader("referer", "https://leetcode.com/")
                .addHeader("Content-Type", "application/json")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    if (counter.decrementAndGet() == 0) {
                        lock.notify()
                    }
                    log.error(e.message) // should be resolved through retry policy, if leetcode down Runtime
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()!! // pretty much impossible to get exception here!
                    val list = objectMapper.readTree(responseString).get("total_rank")

                    time = objectMapper.readTree(responseString).get("time").asDouble()
                    for (participant in list) {
                        val entry = ParticipantDto.builder()
                            .contestName(contestName)
                            .username(participant.get("username").asText())
                            .rank(participant.get("rank").asInt())
                            .score(participant.get("score").asInt())
                            .finishTime(participant.get("finish_time").asDouble())
                            .region(location.name)
                            .build()

                        participants.add(entry)
                    }
                    if (counter.decrementAndGet() == 0) {
                        lock.notify()
                    }
                }
            })
        }

        lock.wait()

        return ContestDto.builder()
            .contestName(contestName)
            .time(time)
            .participants(participants)
            .build()
    }

    private fun getTotalPagesAndTime(fullUrl: String): Pair<Int, Double> {
        val request = Request.Builder()
            .get()
            .url(fullUrl)
            .addHeader("referer", "https://leetcode.com/")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: throw WrongContestNameException("Incorrect contest number!")
            val tree = objectMapper.readTree(responseString)

            return ceil(tree.get("user_num").asInt() / 25.0).toInt() to tree.get("time").asDouble()
        }
    }

    private fun getSanitizedName(contestName: String) = contestName.checkCorrectContestName()
        ?: throw WrongContestNameException("Incorrect contest name for ${contestName}. Write correct contest name (i.e. weekly-contest-60)")

    private fun String.checkCorrectContestName(): String? {
        val trimmed = trim()
        val regex = "(weekly-contest-((0)|([1-9]\\d*)))|(biweekly-contest-((0)|([1-9]\\d*)))".toRegex()

        if (!trimmed.matches(regex)) return null

        return trimmed
    }
}


