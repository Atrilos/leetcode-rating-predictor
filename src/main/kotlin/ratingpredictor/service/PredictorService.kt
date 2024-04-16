package ratingpredictor.service

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import ratingpredictor.dto.ParticipantDto
import ratingpredictor.dto.ParticipantsWithCountDto
import ratingpredictor.exceptions.WrongContestNameException

@Service
@RequiredArgsConstructor
@Slf4j
class PredictorService(private val objectMapper: ObjectMapper) {
    private val baseUrl = "https://leetcode.com"

    fun readContestResultsIntoDB(contestName: String) {
        val client = OkHttpClient().newBuilder().build()
        var pageNum = 0
        val sanitizedName = contestName.checkCorrectContestName()
            ?: throw WrongContestNameException("Incorrect contest name for ${contestName}. Write correct contest name (i.e. weekly-contest-60)")
        val request = Request.Builder()
            .get()
            .url("${baseUrl}/contest/api/ranking/${sanitizedName}/?pagination=${pageNum}&region=global")
            .addHeader("referer", "https://leetcode.com/")
            .addHeader("Content-Type", "application/json")
            .build()
//        client.newCall(request).execute().use { response ->
//            val responseString = response.body?.string()
//            val pn = objectMapper.readTree(responseString).get()
        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string()
            val tree = objectMapper.readTree(responseString)
            val list = tree.get("total_rank")

            val participantsWithCountDto = ParticipantsWithCountDto.builder()
                .contestName(contestName)
                .pageNum(pageNum)
                .userNum(tree.get("user_num").asInt())
                .build()

            for (participant in list) {
                val entry = ParticipantDto.builder()
                    .contestId(participant.get("contest_id").asInt())
                    .username(participant.get("username").asText())
                    .rank(participant.get("rank").asInt())
                    .score(participant.get("score").asInt())
                    .finishTime(participant.get("finish_time").asLong())
                    .build()
                participantsWithCountDto.participants.add(entry)
            }

            println()
        }
    }
}

private fun String.checkCorrectContestName(): String? {
    val trimmed = trim()
    val regex = "(weekly-contest-((0)|([1-9]\\d*)))|(biweekly-contest-((0)|([1-9]\\d*)))".toRegex()

    if (!trimmed.matches(regex)) return null

    return trimmed
}
