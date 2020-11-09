package com.readrops.api.utils

import com.readrops.api.services.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI

class AuthInterceptor(var credentials: Credentials? = null) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val urlBuilder = chain.request().url.newBuilder()

        if (credentials != null) {
            if (credentials!!.url != null) {
                val uri = URI.create(credentials!!.url)
                urlBuilder
                        .scheme(uri.scheme!!)
                        .host(uri.host!!)
                        .encodedPath(uri.path + chain.request().url.encodedPath)
            }

            if (credentials!!.authorization != null) {
                requestBuilder.addHeader("Authorization", credentials!!.authorization)
            }
        }

        return chain.proceed(requestBuilder.url(urlBuilder.build()).build())
    }
}