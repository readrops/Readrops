package com.readrops.readropslibrary.utils

import com.squareup.moshi.JsonReader

fun JsonReader.nextNullableString(): String? =
        if (peek() != JsonReader.Token.NULL) nextString() else nextNull()