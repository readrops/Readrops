package com.readrops.app.compose.util

import android.content.Context
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import com.readrops.app.compose.R
import java.io.IOException
import java.net.UnknownHostException

object ErrorMessage {

    fun get(exception: Exception, context: Context) = when (exception) {
        is HttpException -> getHttpMessage(exception, context)
        is UnknownHostException -> context.resources.getString(R.string.unreachable_url)
        is NoSuchFileException -> context.resources.getString(R.string.unable_open_file)
        is IOException -> context.resources.getString(R.string.network_failure, exception.message.orEmpty())
        is ParseException, is UnknownFormatException -> context.resources.getString(R.string.processing_feed_error)
        else -> "${exception.javaClass.simpleName}: ${exception.message}"
    }

    private fun getHttpMessage(exception: HttpException, context: Context): String {
        return when (exception.code) {
            in 400..499 -> {
                when (exception.code) {
                    400 -> context.resources.getString(R.string.http_error_400)
                    401 -> context.resources.getString(R.string.http_error_401)
                    403 -> context.resources.getString(R.string.http_error_403)
                    404 -> context.resources.getString(R.string.http_error_404)
                    else -> context.resources.getString(R.string.http_error_4XX, exception.code)
                }
            }

            in 500..599 -> {
                context.resources.getString(R.string.http_error_5XX, exception.code)
            }
            else -> context.resources.getString(R.string.http_error, exception.code)
        }
    }
}