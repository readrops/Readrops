package com.readrops.app.compose.util.components

import androidx.compose.runtime.Composable


sealed class TextFieldError {
    object EmptyField : TextFieldError()
    object BadUrl : TextFieldError()
    object UnreachableUrl : TextFieldError()
    object NoRSSFeed : TextFieldError()
    object NoRSSUrl : TextFieldError()

    @Composable
    fun errorText(): String =
        // TODO replace by string resources
        when (this) {
            BadUrl -> "Input is not a valid URL"
            EmptyField -> "Field can't be empty"
            NoRSSFeed -> "No RSS feed found"
            NoRSSUrl -> "The provided URL is not a valid RSS feed"
            UnreachableUrl -> "Unreachable URL"
        }
}

