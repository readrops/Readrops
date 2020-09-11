package com.readrops.api.utils

import com.gitlab.mvysny.konsumexml.Konsumer

fun Konsumer.nonNullText(failOnElement: Boolean = true): String {
    val text = text(failOnElement = failOnElement)
    return if (text.isNotEmpty()) text else throw ParseException("Xml field $name can't be null")
}

fun Konsumer.nullableText(failOnElement: Boolean = true): String? {
    val text = text(failOnElement = failOnElement)
    return if (text.isNotEmpty()) text else null
}