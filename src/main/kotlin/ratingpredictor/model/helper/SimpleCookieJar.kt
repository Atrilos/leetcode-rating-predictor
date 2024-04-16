package ratingpredictor.model.helper

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.springframework.stereotype.Component

@Component
class SimpleCookieJar : CookieJar {
    private val cookieStorage: ArrayList<Cookie> = ArrayList()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookieStorage.clear()
        this.cookieStorage.addAll(cookies)
    }

    override fun loadForRequest(url: HttpUrl): ArrayList<Cookie> {
        return cookieStorage
    }
}