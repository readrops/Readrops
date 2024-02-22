package com.readrops.api.utils.exceptions

import okhttp3.Response


class HttpException(val response: Response) : Exception() {

    val code: Int
        get() = response.code

    override val message: String
        get() = "HTTP " + response.code + " " + response.message
}