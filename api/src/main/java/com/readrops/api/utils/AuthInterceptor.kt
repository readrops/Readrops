package com.readrops.api.utils

import android.net.Uri
import com.readrops.api.services.Credentials
import com.readrops.api.services.freshrss.FreshRSSService
import com.readrops.api.services.nextcloudnews.NextNewsService
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.IllegalArgumentException

class AuthInterceptor(var credentials: Credentials? = null) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val urlBuilder = chain.request().url.newBuilder()

        if (credentials != null) {
            if (credentials!!.url != null) {
                val uri = Uri.parse(credentials!!.url)
                urlBuilder
                        .scheme(uri.scheme!!)
                        .host(uri.host!!)
                        .encodedPath(uri.encodedPath + chain.request().url.encodedPath)
            }

            if (credentials!!.authorization != null) {
                requestBuilder.addHeader("Authorization", credentials!!.authorization)
            }
        }

        return chain.proceed(requestBuilder.url(urlBuilder.build()).build())
    }
}