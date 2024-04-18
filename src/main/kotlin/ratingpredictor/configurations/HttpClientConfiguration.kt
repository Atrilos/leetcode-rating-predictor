package ratingpredictor.configurations

import okhttp3.Dispatcher
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.internal.threadFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratingpredictor.configurations.interceptors.MinTimeInterceptor
import ratingpredictor.configurations.interceptors.RetryInterceptor
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Configuration
class HttpClientConfiguration {

    @Bean
    fun httpClient(): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val dispatcher = Dispatcher()
        dispatcher.maxRequestsPerHost = 5

        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .dispatcher(dispatcher)
            .addInterceptor(RetryInterceptor(15))
            .addInterceptor(MinTimeInterceptor(50))
            .build()
    }

}

