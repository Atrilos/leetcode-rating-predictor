package ratingpredictor.service

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.configurationprocessor.json.JSONObject
import org.springframework.stereotype.Service


@Service
@RequiredArgsConstructor
class UserService(
    private val objectMapper: ObjectMapper,
    private val client: OkHttpClient
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
        private const val BASE_US = "https://leetcode.com"
        private const val BASE_CN = "https://leetcode.cn"
    }

    fun retrieveUserRatingUS(username: String) {
        val url = BASE_US
        val mediaType = "application/json".toMediaType()
        val body = "{\"query\":\"query userContestRankingInfo(\$username: String!) { userContestRanking(username:\$username) { attendedContestsCount rating globalRanking } }\",\"variables\":{\"username\":\"$username\"}}"
            .toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$url/graphql/")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()
        val response = client.newCall(request).execute()
        println()
    }

    fun getStats(username: String) {
        val url = BASE_US
        val client = OkHttpClient().newBuilder()
            .build()
        val mediaType = "application/json".toMediaType()
        val query = String.format(
            "{\"query\":\"query getUserProfile(\$username: String!) { allQuestionsCount { difficulty count } matchedUser(username: \$username) { contributions { points } profile { reputation ranking } submissionCalendar submitStats { acSubmissionNum { difficulty count submissions } totalSubmissionNum { difficulty count submissions } } } } \",\"variables\":{\"username\":\"%s\"}}",
            username
        )
        val body = query.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("$url/graphql/")
            .method("POST", body)
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()

        // Inspect response
        val responseString = response.body?.string()
        val jsonObject = JSONObject(responseString)
        println()
    }
}
