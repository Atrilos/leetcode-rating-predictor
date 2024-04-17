package ratingpredictor.configurations

import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ratingpredictor.configurations.interceptors.MinTimeInterceptor
import ratingpredictor.configurations.interceptors.RetryInterceptor
import java.net.CookieManager
import java.net.CookiePolicy

@Configuration
class HttpClientConfiguration {

    @Bean
    fun httpClient(): OkHttpClient {
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        return OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .addInterceptor(RetryInterceptor(5))
            .addInterceptor(MinTimeInterceptor(100))
            .build()
    }

}

