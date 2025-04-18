package com.readrops.api.utils.extensions

import com.readrops.api.utils.exceptions.ParseException
import com.squareup.moshi.JsonReader

fun JsonReader.nextNullableString(): String? =
        if (peek() != JsonReader.Token.NULL) nextString().ifEmpty { null }?.trim() else nextNull()

fun JsonReader.nextNonEmptyString(): String {
    val text = nextString()
    return if (text.isNotEmpty()) text.trim() else throw ParseException("Json value can't be null")
}

fun JsonReader.nextNullableInt(): Int? =
        if (peek() != JsonReader.Token.NULL) nextInt() else nextNull()

fun JsonReader.nextNullableLong(): Long? =
    if (peek() != JsonReader.Token.NULL) nextLong() else nextNull()

fun JsonReader.skipField() {
    skipName()
    skipValue()
}

fun JsonReader.skipToEnd() {
    while (hasNext()) {
        skipField()
    }
}

fun Int.toBoolean(): Boolean = this == 1
