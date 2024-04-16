package ratingpredictor.model.helper

import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request


data class Credential(var csrfToken: String = "", var cfBm: String = "") {

    fun toHeaders() = Headers.Builder()
        .add("Cookie", "csrfToken=$csrfToken; __cf_bm=$cfBm")
        .build()

}

fun retrieveCredentials(client: OkHttpClient, url: String): Credential {
    val requestCredentials = Request.Builder()
        .get()
        .url(url)
        .addHeader("user-agent", "Mozilla/5.0 LeetCode API")
        .build()
    client.newCall(requestCredentials).execute().use { response ->
        val credentials = Credential()

        for (entry in response.headers.toMultimap()["set-cookie"] ?: emptyList()) {
            if (entry.startsWith("csrftoken=")) {
                val startIndex = entry.indexOf("=") + 1
                val endIndex = entry.indexOf(";", startIndex)
                credentials.csrfToken = entry.substring(startIndex, endIndex)
            } else if (entry.startsWith("__cf_bm=")) {
                val startIndex = entry.indexOf("=") + 1
                val endIndex = entry.indexOf(";", startIndex)
                credentials.cfBm = entry.substring(startIndex, endIndex)
            }
        }

        return credentials
    }
}