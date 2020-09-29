package com.readrops.api.utils

import com.squareup.moshi.JsonReader

fun JsonReader.nextNullableString(): String? =
        if (peek() != JsonReader.Token.NULL) nextString().ifEmpty { null }?.trim() else nextNull()

fun JsonReader.nextNonEmptyString(): String {
    val text = nextString()
    return if (text.isNotEmpty()) text.trim() else throw ParseException("Json value can't be null")
}