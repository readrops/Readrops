package com.readrops.api.utils.exceptions

import okhttp3.Response
import java.io.IOException


class HttpException(val response: Response) : IOException() {

    val code: Int
        get() = response.code

    override val message: String
        get() = "HTTP " + response.code + " " + response.message
}