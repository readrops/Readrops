package com.readrops.app.compose.util.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.api.utils.exceptions.UnknownFormatException
import com.readrops.app.compose.R
import java.io.IOException
import java.net.UnknownHostException

@Composable
fun ErrorDialog(
    exception: Exception,
    onDismiss: () -> Unit
) {
    BaseDialog(
        title = stringResource(id = R.string.error_occured),
        icon = painterResource(id = R.drawable.ic_error),
        onDismiss = onDismiss
    ) {
        Text(text = errorText(exception = exception))
    }
}

// TODO check compatibility with other accounts errors
@Composable
fun errorText(exception: Exception) = when (exception) {
    is HttpException -> stringResource(id = R.string.unreachable_feed_http_error, exception.code.toString())
    is UnknownHostException -> stringResource(R.string.unreachable_feed)
    is NoSuchFileException -> stringResource(R.string.unable_open_file)
    is IOException -> stringResource(R.string.network_failure, exception.message.orEmpty())
    is ParseException, is UnknownFormatException -> stringResource(R.string.processing_feed_error)
    else -> "${exception.javaClass.simpleName}: ${exception.message}"
}