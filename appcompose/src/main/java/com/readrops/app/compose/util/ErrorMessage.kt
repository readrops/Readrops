package com.readrops.app.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import com.readrops.app.compose.R
import java.io.IOException
import java.net.UnknownHostException

object ErrorMessage {

    @Composable
    fun get(exception: Exception) = when (exception) {
        is HttpException -> getHttpMessage(exception)
        is UnknownHostException -> stringResource(R.string.unreachable_url)
        is NoSuchFileException -> stringResource(R.string.unable_open_file)
        is IOException -> stringResource(R.string.network_failure, exception.message.orEmpty())
        is ParseException, is UnknownFormatException -> stringResource(R.string.processing_feed_error)
        else -> "${exception.javaClass.simpleName}: ${exception.message}"
    }

    @Composable
    private fun getHttpMessage(exception: HttpException): String {
        return when (exception.code) {
            in 400..499 -> {
                when (exception.code) {
                    400 -> stringResource(id = R.string.http_error_400)
                    401 -> stringResource(id = R.string.http_error_401)
                    403 -> stringResource(id = R.string.http_error_403)
                    404 -> stringResource(id = R.string.http_error_404)
                    else -> stringResource(id = R.string.http_error_4XX, exception.code)
                }
            }

            in 500..599 -> {
                stringResource(id = R.string.http_error_5XX, exception.code)
            }
            else -> stringResource(id = R.string.http_error, exception.code)
        }
    }
}