package ratingpredictor.configurations.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ratingpredictor.exceptions.NetworkException
import ratingpredictor.exceptions.TooManyRequestsException
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int) : Interceptor {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var retryCount = 0
        var response: Response? = null

        try {

            while (retryCount <= maxRetries) {
                response = chain.proceed(request)
                if (response.isSuccessful) {
                    log.debug("Request successful. {}", request.url.toUrl())
                    return response
                }
                retryCount++
                log.debug("Request unsuccessful! Code: ${response.code}. Retry: $retryCount")
                val waitTime = 5000L + (2500L * (retryCount - 1))
                Thread.sleep(waitTime)
                request = request.newBuilder().build()
                response.close()
            }

            throw TooManyRequestsException("Retry count exceeded!")
        } catch (e: Exception) {
            if (retryCount > maxRetries) {
                if (e is TooManyRequestsException) {
                    throw e
                } else {
                    throw NetworkException("Exception: ${e.javaClass}, message: ${e.message}")
                }
            }
        }

        return response ?: throw NetworkException("")
    }
}