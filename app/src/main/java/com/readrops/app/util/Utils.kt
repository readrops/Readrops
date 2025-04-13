package com.readrops.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.ColorInt
import com.readrops.db.pojo.ItemWithFeed
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

    fun normalizeUrl(url: String): String {
        return buildString {
            if (!url.contains("https://") && !url.contains("http://")) {
                append("https://$url")
            } else {
                append(url)
            }

            if (!url.endsWith("/")) {
                append("/")
            }
        }
    }

    fun shareItem(
        itemWithFeed: ItemWithFeed,
        context: Context,
        useCustomShareIntentTpl: Boolean,
        customShareIntentTpl: String
    ) {
        val intentContent =
            if(!useCustomShareIntentTpl || customShareIntentTpl.isBlank()) itemWithFeed.item.link
            else ShareIntentTextRenderer(itemWithFeed).render(customShareIntentTpl)
        Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, intentContent)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }.also {
            context.startActivity(Intent.createChooser(it, null))
        }
    }
}