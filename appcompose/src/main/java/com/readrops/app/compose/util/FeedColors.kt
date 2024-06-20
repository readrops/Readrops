package com.readrops.app.compose.util

import android.graphics.BitmapFactory
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object FeedColors : KoinComponent {

    suspend fun getFeedColor(feedUrl: String): Int {
        // use OkHttp directly instead of Coil as Coil doesn't respect OkHttp timeout
        val response = get<OkHttpClient>().newCall(
            Request.Builder()
                .url(feedUrl)
                .build()
        ).execute()

        val bitmap = BitmapFactory.decodeStream(response.body?.byteStream()) ?: return 0
        val palette = Palette.from(bitmap).generate()

        val dominantSwatch = palette.dominantSwatch
        return if (dominantSwatch != null && !isColorTooBright(dominantSwatch.rgb)
            && !isColorTooDark(dominantSwatch.rgb)
        ) {
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