package com.readrops.api.utils

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Whitespace
import com.gitlab.mvysny.konsumexml.textRecursively

fun Konsumer.nonNullText(): String {
    val text = text(whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text.trim() else throw ParseException("$name text can't be null")
}

fun Konsumer.nullableText(): String? {
    val text = text(whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text.trim() else null
}

fun Konsumer.nullableTextRecursively(): String? {
    val text = textRecursively()
    return if (text.isNotEmpty()) text.trim() else null
}