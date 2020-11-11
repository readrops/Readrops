package com.readrops.api.utils

import com.squareup.moshi.JsonReader

fun JsonReader.nextNullableString(): String? =
        if (peek() != JsonReader.Token.NULL) nextString() else nextNull()

fun JsonReader.nextNullableInt(): Int? =
        if (peek() != JsonReader.Token.NULL) nextInt() else nextNull()