package com.readrops.app.compose.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object FeedColors : KoinComponent {

    suspend fun getFeedColor(feedUrl: String): Int {
        val context = get<Context>() // TODO maybe call imageLoader directly ? may require some DI changes

        val result = context.imageLoader
            .execute(
                ImageRequest.Builder(context)
                    .data(feedUrl)
                    .allowHardware(false)
                    .build()
            ).drawable as BitmapDrawable

        val palette = Palette.from(result.bitmap).generate()

        val dominantSwatch = palette.dominantSwatch
        return if (dominantSwatch != null && !isColorTooBright(dominantSwatch.rgb)
            && !isColorTooDark(dominantSwatch.rgb)) {
            dominantSwatch.rgb
        } else 0
    }

    private fun isColorTooBright(@ColorInt color: Int): Boolean {
        return getColorLuma(color) > 210
    }

    private fun isColorTooDark(@ColorInt color: Int): Boolean {
        return getColorLuma(color) < 40
    }

    private fun getColorLuma(@ColorInt color: Int): Double {
        val r = color shr 16 and 0xff
        val g = color shr 8 and 0xff
        val b = color shr 0 and 0xff
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    fun isColorDark(color: Int) = getColorLuma(color) < 130

}