package com.readrops.api.opml

import android.content.Context
import android.net.Uri
import com.readrops.api.opml.model.Body
import com.readrops.api.opml.model.Head
import com.readrops.api.opml.model.OPML
import com.readrops.api.opml.model.Outline
import com.readrops.api.utils.LibUtils
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import io.reactivex.Completable
import io.reactivex.Single
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.OutputStream

object OPMLParser {

    @JvmStatic
    fun read(uri: Uri, context: Context): Single<Map<Folder, List<Feed>>> {
        return Single.create { emitter ->
            val fileString = LibUtils.fileToString(uri, context)
            val serializer: Serializer = Persister()

            val opml: OPML = serializer.read(OPML::class.java, fileString)

            emitter.onSuccess(opmltoFoldersAndFeeds(opml))
        }
    }

    @JvmStatic
    fun write(foldersAndFeeds: Map<Folder, List<Feed>>, outputStream: OutputStream): Completable {
        return Completable.create { emitter ->
            val serializer: Serializer = Persister()
            serializer.write(foldersAndFeedsToOPML(foldersAndFeeds), outputStream)

            emitter.onComplete()
        }
    }

    private fun opmltoFoldersAndFeeds(opml: OPML): Map<Folder, List<Feed>> {
        val foldersAndFeeds: MutableMap<Folder, List<Feed>> = HashMap()
        val body = opml.body!!

        body.outlines?.forEach { outline ->
            val folder = Folder(outline.title)

            val feeds = arrayListOf<Feed>()
            outline.outlines?.forEach { feedOutline ->
                val feed = Feed().apply {
                    name = feedOutline.title
                    url = feedOutline.xmlUrl
                    siteUrl = feedOutline.htmlUrl
                }

                feeds.add(feed)
            }

            foldersAndFeeds[folder] = feeds
        }

        return foldersAndFeeds
    }

    private fun foldersAndFeedsToOPML(foldersAndFeeds: Map<Folder, List<Feed>>): OPML {
        val outlines = arrayListOf<Outline>()
        for (folderAndFeeds in foldersAndFeeds) {
            val outline = Outline(folderAndFeeds.key.name)

            val feedOutlines = arrayListOf<Outline>()
            folderAndFeeds.value.forEach { feed ->
                val feedOutline = Outline(feed.name, feed.url, feed.siteUrl)

                feedOutlines.add(feedOutline)
            }

            outline.outlines = feedOutlines
            outlines.add(outline)
        }

        val head = Head("Subscriptions")
        val body = Body(outlines)

        return OPML("2.0", head, body)
    }

}