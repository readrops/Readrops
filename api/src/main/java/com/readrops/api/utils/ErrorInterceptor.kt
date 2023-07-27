package com.readrops.api.utils

import com.readrops.api.utils.exceptions.HttpException
import okhttp3.Interceptor
import okhttp3.Response

class ErrorInterceptor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        return response
    }
}