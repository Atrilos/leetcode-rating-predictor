package ratingpredictor.configurations.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratingpredictor.exceptions.TooManyRequestsException
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int) : Interceptor {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    var retryCount = 0

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: Throwable? = null

        while (retryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (!response.isSuccessful) throw TooManyRequestsException("Too many requests!")
                break
            } catch (e: Exception) {
                log.debug("Too many requests! Retry: ${retryCount + 1}")
                exception = e
                retryCount++
                val waitTime = 500L
                Thread.sleep(waitTime)
                request = request.newBuilder().build()
            }
        }

        return response ?: throw IOException(exception)
    }
}