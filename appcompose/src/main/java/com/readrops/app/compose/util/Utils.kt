package com.readrops.app.compose.util

import android.graphics.Color
import androidx.annotation.ColorInt
import java.util.Locale

object Utils {

    private const val AVERAGE_WORDS_PER_MINUTE = 250

    fun readTimeFromString(value: String): Double {
        val nbWords = value.split(Regex("\\s+")).size
        return nbWords.toDouble() / AVERAGE_WORDS_PER_MINUTE
    }

    fun getCssColor(@ColorInt color: Int): String {
        return String.format(
            Locale.US, "rgba(%d,%d,%d,%.2f)",
            Color.red(color),
            Color.green(color),
            Color.blue(color),
            Color.alpha(color) / 255.0
        )
    }
}