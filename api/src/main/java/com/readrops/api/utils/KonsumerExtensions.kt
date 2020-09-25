package com.readrops.api.utils

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Whitespace
import com.gitlab.mvysny.konsumexml.textRecursively

fun Konsumer.nonNullText(failOnElement: Boolean = true): String {
    val text = text(failOnElement = failOnElement, whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text else throw ParseException("$name text can't be null")
}

fun Konsumer.nullableText(failOnElement: Boolean = true): String? {
    val text = text(failOnElement = failOnElement, whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text else null
}

fun Konsumer.nullableTextRecursively(): String? {
    val text = textRecursively()
    return if (text.isNotEmpty()) text else null
}