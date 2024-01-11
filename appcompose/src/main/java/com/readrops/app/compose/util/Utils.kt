package com.readrops.app.compose.util

object Utils {

    private const val AVERAGE_WORDS_PER_MINUTE = 250

    fun readTimeFromString(value: String): Double {
        val nbWords = value.split("\\s+").size
        return nbWords.toDouble() / AVERAGE_WORDS_PER_MINUTE
    }
}