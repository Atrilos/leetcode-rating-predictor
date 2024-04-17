package ratingpredictor.configurations.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @param minTime Minimum time between requests in millis
 */
class MinTimeInterceptor(private val minTime: Long) : Interceptor {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private var lastRequestTime = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val currentTime = System.nanoTime()
        val minTimeMillis = minTime * 1_000_000

        if (currentTime - lastRequestTime < minTimeMillis) {
            val delay = minTimeMillis - (currentTime - lastRequestTime)
            Thread.sleep(delay)
            log.trace("Delaying call by ${delay / 1_000_000} ms")
        }

        lastRequestTime = currentTime

        return chain.proceed(request)
    }
}