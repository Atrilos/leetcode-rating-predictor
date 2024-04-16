package ratingpredictor.configurations

import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val reqBuilder = request.newBuilder()
                    .header("User-Agent", "PostmanRuntime/7.37.3")
                chain.proceed(reqBuilder.build())
            })
            .build()
    }

}