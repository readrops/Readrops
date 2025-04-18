package com.readrops.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object FeedColors : KoinComponent {

    suspend fun getFeedColor(feedUrl: String): Int {
        // use OkHttp directly instead of Coil as Coil doesn't respect OkHttp timeout
        // TODO retry with Coil3?
        val response = get<OkHttpClient>().newCall(
            Request.Builder()
                .url(feedUrl)
                .build()
        ).execute()

        val bitmap = BitmapFactory.decodeStream(response.body?.byteStream()) ?: return 0

        return getFeedColor(bitmap)
    }

    suspend fun getFeedColor(bitmap: Bitmap): Int = withContext(Dispatchers.Default) {
        val palette = Palette.from(bitmap).generate()

        palette.dominantSwatch?.rgb ?: 0
    }
}