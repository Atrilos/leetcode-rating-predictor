package ratingpredictor.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.annotations.VisibleForTesting
import lombok.RequiredArgsConstructor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ratingpredictor.constants.Location
import ratingpredictor.dto.ContestDto
import ratingpredictor.dto.ParticipantDto
import ratingpredictor.dto.UserDataDto
import ratingpredictor.exceptions.WrongContestNameException
import ratingpredictor.service.helper.geometricMean
import ratingpredictor.service.helper.getExpectedRank
import ratingpredictor.service.helper.getRating
import java.io.IOException
import java.util.concurrent.Semaphore
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


    fun predict(contestName: String): List<UserDataDto> {
        val sanitizedName = getSanitizedName(contestName)
        val mergedContest = fetchContest(sanitizedName)

        return getListWithPredictions(mergedContest)
    }

    private fun getListWithPredictions(contest: ContestDto): List<UserDataDto> {
        val participants = contest.participants
        val userDataList = fetchUsers(participants)

        for (user in userDataList) {
            val expectedRank = 0.5 + getExpectedRank(userDataList, user.currentRating)
            val gMean = geometricMean(expectedRank, user.currentRating)
            val expectedRating = getRating(userDataList, gMean)
            var delta = expectedRating - user.currentRating

            if (user.attendedContestsCount == 0) {
                delta *= 0.5
            } else {
                delta = (delta * 2) / 9
            }

            user.expectedRating = user.currentRating + delta
        }

        return userDataList
    }

    @VisibleForTesting
    internal fun fetchUsers(participants: ArrayList<ParticipantDto>): ArrayList<UserDataDto> {
        val result = ArrayList<UserDataDto>()
        val semaphore = Semaphore(-participants.size + 1)

        for (participant in participants) {
            val countryCode = participant.region
            if (countryCode == "CN") {
                fetchUserCN(participant, result, semaphore)
            } else {
                fetchUserUS(participant, result, semaphore)
            }
        }

        semaphore.acquire()

        return result
    }

    private fun fetchUserCN(
        participant: ParticipantDto,
        result: java.util.ArrayList<UserDataDto>,
        semaphore: Semaphore
    ) {
        val url = Location.CN.url
        val username = participant.username
        val mediaType = "application/json".toMediaType()
        val body = ("{\"query\":\"query userPublicProfile(\$userSlug: String!) { userProfilePublicProfile(userSlug: " +
                "\$userSlug) { profile { contestCount ranking { currentRating currentGlobalRanking } } } }\",\"variables\":{\"userSlug\":\"$username\"}}")
            .toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$url/graphql/")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
                semaphore.release()
                log.error(e.message)
                log.error("Username: $username-CN")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()!!
                val node = objectMapper.readTree(responseString)
                    .get("data")
                    .get("userProfilePublicProfile")
                    .get("profile")
                val usernameWithRegion = "$username-CN"

                if (node.get("ranking").isNull) {
                    result.add(
                        UserDataDto.builder()
                            .username(usernameWithRegion)
                            .currentRating(1500.0)
                            .attendedContestsCount(0)
                            .build()
                    )
                } else {
                    result.add(
                        UserDataDto.builder()
                            .username(usernameWithRegion)
                            .currentRating(node.get("ranking").get("currentRating").asDouble())
                            .attendedContestsCount(node.get("contestCount").asInt())
                            .build()
                    )
                }
                semaphore.release()
            }
        })
    }

    private fun fetchUserUS(
        participant: ParticipantDto,
        result: java.util.ArrayList<UserDataDto>,
        semaphore: Semaphore
    ) {
        val url = Location.US.url
        val username = participant.username
        val mediaType = "application/json".toMediaType()
        val body = ("{\"query\":\"query userContestRankingInfo(\$username: String!) { userContestRanking(username:" +
                "\$username) { attendedContestsCount rating globalRanking } }\",\"variables\":{\"username\":\"$username\"}}")
            .toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$url/graphql/")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
                semaphore.release()
                log.error(e.message)
                log.error("Username: $username-US")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()!!
                val node = objectMapper.readTree(responseString).get("data").get("userContestRanking")
                val usernameWithRegion = "$username-US"

                if (node.isNull) {
                    result.add(
                        UserDataDto.builder()
                            .username(usernameWithRegion)
                            .currentRating(1500.0)
                            .attendedContestsCount(0)
                            .build()
                    )
                } else {
                    result.add(
                        UserDataDto.builder()
                            .username(usernameWithRegion)
                            .currentRating(node.get("rating").asDouble())
                            .attendedContestsCount(node.get("attendedContestsCount").asInt())
                            .build()
                    )
                }
                semaphore.release()
            }
        })
    }

    @VisibleForTesting
    internal fun fetchContest(contestName: String): ContestDto {
        val participants = ArrayList<ParticipantDto>()
        val time =
            getTime("${Location.US.url}/contest/api/ranking/${contestName}/?pagination=1&region=${Location.US.region}")
        var totalPages = 0

        for (location in Location.entries) {
            totalPages += getTotalPages("${location.url}/contest/api/ranking/${contestName}/?pagination=1&region=${location.region}")
        }

        val semaphore = Semaphore(-totalPages + 1)

        for (location in Location.entries) {
            fetchContest(contestName, participants, location, semaphore)
        }

        semaphore.acquire()

        return ContestDto.builder()
            .contestName(contestName)
            .time(time)
            .participants(participants)
            .build()
    }

    private fun getTime(fullUrl: String): Double {
        val request = Request.Builder()
            .get()
            .url(fullUrl)
            .addHeader("referer", "https://leetcode.com/")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: throw WrongContestNameException("Incorrect contest number!")
            val tree = objectMapper.readTree(responseString)

            return tree.get("time").asDouble()
        }
    }

    private fun fetchContest(
        contestName: String,
        result: java.util.ArrayList<ParticipantDto>,
        location: Location,
        semaphore: Semaphore
    ) {
        val totalPages =
            getTotalPages("${location.url}/contest/api/ranking/${contestName}/?pagination=1&region=${location.region}")

        for (pageNum in 1..totalPages) {
            val request = Request.Builder()
                .get()
                .url("${location.url}/contest/api/ranking/${contestName}/?pagination=${pageNum}&region=${location.region}")
                .addHeader("referer", "https://leetcode.com/")
                .addHeader("Content-Type", "application/json")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                    semaphore.release()
                    log.error(e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseString = response.body?.string()!! // pretty much impossible to get exception here!
                    val list = objectMapper.readTree(responseString).get("total_rank")

                    for (participant in list) {
                        val entry = ParticipantDto.builder()
                            .contestName(contestName)
                            .username(participant.get("username").asText())
                            .rank(participant.get("rank").asInt())
                            .score(participant.get("score").asInt())
                            .finishTime(participant.get("finish_time").asDouble())
                            .region(location.name)
                            .build()

                        result.add(entry)
                    }
                    semaphore.release()
                }
            })
        }
    }

    @VisibleForTesting
    internal fun getTotalPages(fullUrl: String): Int {
        val request = Request.Builder()
            .get()
            .url(fullUrl)
            .addHeader("referer", "https://leetcode.com/")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: throw WrongContestNameException("Incorrect contest number!")
            val tree = objectMapper.readTree(responseString)

            return ceil(tree.get("user_num").asInt() / 25.0).toInt()
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


