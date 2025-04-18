package com.readrops.app.util.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.readrops.app.R


sealed class TextFieldError {
    data object EmptyField : TextFieldError()
    data object BadUrl : TextFieldError()
    data object UnreachableUrl : TextFieldError()
    data object NoRSSFeed : TextFieldError()
    data object NoRSSUrl : TextFieldError()

    @Composable
    fun errorText(): String =
        when (this) {
            BadUrl -> stringResource(R.string.wrong_url)
            EmptyField -> stringResource(R.string.empty_field)
            NoRSSFeed -> stringResource(R.string.no_rss_feed_found)
            NoRSSUrl -> stringResource(R.string.not_valid_rss_feed)
            UnreachableUrl -> stringResource(R.string.unreachable_url)
        }
}

