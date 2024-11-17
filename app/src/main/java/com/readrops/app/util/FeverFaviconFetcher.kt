package com.readrops.app.util

import android.util.Patterns
import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem

data class FeedKey(val faviconUrl: String?)

/**
 * Custom Coil Fetcher to load Feed favicons from either an http source or a file source
 */
class FeverFaviconFetcher(
    private val data: FeedKey,
    private val diskCache: DiskCache,
    private val okHttpClient: OkHttpClient
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        return when {
            data.faviconUrl == null -> null
            Patterns.WEB_URL.matcher(data.faviconUrl).matches() -> httpLoader()
            else -> fileLoader()
        }
    }

    private fun fileLoader(): FetchResult? {
        val diskCacheKey = data.faviconUrl!!
        val snapshot = diskCache.openSnapshot(diskCacheKey)

        return if (snapshot != null) {
            SourceFetchResult(
                source = snapshot.toImageSource(),
                mimeType = MIME_TYPE,
                dataSource = DataSource.DISK
            )
        } else {
            null
        }
    }

    private fun httpLoader(): FetchResult? {
        val diskCacheKey = data.faviconUrl!!
        val snapshot = diskCache.openSnapshot(diskCacheKey)

        return if (snapshot != null) {
            SourceFetchResult(
                source = snapshot.toImageSource(),
                mimeType = MIME_TYPE,
                dataSource = DataSource.NETWORK
            )
        } else {
            val request = Request.Builder()
                .url(diskCacheKey)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.code == 304 || response.body == null) {
                return null
            }

            val httpSnapshot = diskCache.openEditor(diskCacheKey)!!.run {
                diskCache.fileSystem.write(data) {
                    write(response.body!!.bytes())
                }

                commitAndOpenSnapshot()
            }

            return if (httpSnapshot != null) {
                SourceFetchResult(
                    source = httpSnapshot.toImageSource(),
                    mimeType = MIME_TYPE,
                    dataSource = DataSource.NETWORK
                )
            } else {
                null
            }
        }
    }

    private fun DiskCache.Snapshot.toImageSource(): ImageSource {
        return ImageSource(
            file = data,
            fileSystem = FileSystem.SYSTEM,
            diskCacheKey = this@FeverFaviconFetcher.data.faviconUrl,
            closeable = this
        )
    }

    class Factory(private val okHttpClient: OkHttpClient) : Fetcher.Factory<FeedKey> {

        override fun create(data: FeedKey, options: Options, imageLoader: ImageLoader): Fetcher {
            return FeverFaviconFetcher(data, imageLoader.diskCache!!, okHttpClient)
        }
    }

    companion object {
        private const val MIME_TYPE = "image/*"
    }
}