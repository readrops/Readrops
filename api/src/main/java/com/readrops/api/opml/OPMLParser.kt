package com.readrops.api.opml

import com.readrops.api.opml.model.Body
import com.readrops.api.opml.model.Head
import com.readrops.api.opml.model.OPML
import com.readrops.api.opml.model.Outline
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import io.reactivex.Completable
import io.reactivex.Single
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister
import java.io.InputStream
import java.io.OutputStream

object OPMLParser {

    val TAG: String = OPMLParser.javaClass.simpleName

    @JvmStatic
    fun read(stream: InputStream): Single<Map<Folder?, List<Feed>>> {
        return Single.create { emitter ->
            try {
                val serializer: Serializer = Persister()

                val opml: OPML = serializer.read(OPML::class.java, stream)

                emitter.onSuccess(opmlToFoldersAndFeeds(opml))
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    @JvmStatic
    fun write(foldersAndFeeds: Map<Folder?, List<Feed>>, outputStream: OutputStream): Completable {
        return Completable.create { emitter ->
            val serializer: Serializer = Persister()
            serializer.write(foldersAndFeedsToOPML(foldersAndFeeds), outputStream)

            emitter.onComplete()
        }
    }

    private fun opmlToFoldersAndFeeds(opml: OPML): Map<Folder?, List<Feed>> {
        if (opml.version != "2.0")
            throw ParseException("Only 2.0 OPML specification is supported")

        val foldersAndFeeds: MutableMap<Folder?, List<Feed>> = HashMap()
        val body = opml.body!!

        body.outlines?.forEach { outline ->
            val outlineParsing = parseOutline(outline)
            associateOrphanFeedsToFolder(foldersAndFeeds, outlineParsing, null)

            foldersAndFeeds.putAll(outlineParsing)
        }

        return foldersAndFeeds
    }

    /**
     * Parse outline and its children recursively
     * @param outline node to parse
     */
    private fun parseOutline(outline: Outline): MutableMap<Folder?, List<Feed>> {
        val foldersAndFeeds: MutableMap<Folder?, List<Feed>> = HashMap()

        // The outline is a folder/category
        if ((outline.outlines != null && !outline.outlines?.isEmpty()!!) || outline.xmlUrl.isNullOrEmpty()) {
            // if the outline doesn't have text or title value but contains sub outlines,
            // those sub outlines will be considered as not belonging to any folder and join the others at the top level of the hierarchy
            val folder = if (outline.name != null) Folder(outline.name) else null

            outline.outlines?.forEach {
                val recursiveFeedsFolders = parseOutline(it)

                // Treat feeds without folder, so belonging to the current folder
                associateOrphanFeedsToFolder(foldersAndFeeds, recursiveFeedsFolders, folder)
                foldersAndFeeds.putAll(recursiveFeedsFolders.toMap())
            }

            // empty outline
            if (!foldersAndFeeds.containsKey(folder)) foldersAndFeeds[folder] = listOf()

        } else { // the outline is a feed
            if (!outline.xmlUrl.isNullOrEmpty()) {
                val feed = Feed().apply {
                    name = outline.name
                    url = outline.xmlUrl
                    siteUrl = outline.htmlUrl
                }
                // parsed feed is linked to null to be assigned to the previous level folder
                foldersAndFeeds[null] = listOf(feed)
            }
        }

        return foldersAndFeeds
    }

    private fun foldersAndFeedsToOPML(foldersAndFeeds: Map<Folder?, List<Feed>>): OPML {
        val outlines = arrayListOf<Outline>()

        for (folderAndFeeds in foldersAndFeeds) {
            if (folderAndFeeds.key != null) {
                val outline = Outline(folderAndFeeds.key?.name)

                val feedOutlines = arrayListOf<Outline>()
                for (feed in folderAndFeeds.value) {
                    val feedOutline = Outline(feed.name, feed.url, feed.siteUrl)

                    feedOutlines += feedOutline
                }

                outline.outlines = feedOutlines
                outlines += outline
            } else {
                for (feed in folderAndFeeds.value) {
                    outlines += Outline(feed.name, feed.url, feed.siteUrl)
                }
            }
        }

        val head = Head("Subscriptions")
        val body = Body(outlines)

        return OPML("2.0", head, body)
    }

    /**
     * Associate parsed feeds without folder to the previous level folder.
     * @param foldersAndFeeds final result
     * @param parsingResult current level parsing
     * @param folder the folder feeds will be associated to
     *
     */
    private fun associateOrphanFeedsToFolder(
            foldersAndFeeds: MutableMap<Folder?, List<Feed>>,
            parsingResult: MutableMap<Folder?, List<Feed>>, folder: Folder?,
    ) {
        val feeds = parsingResult[null]
        if (feeds != null && feeds.isNotEmpty()) {
            if (foldersAndFeeds[folder] == null) foldersAndFeeds[folder] = feeds
            else foldersAndFeeds[folder] = foldersAndFeeds[folder]?.plus(feeds)!!

            parsingResult.remove(null)
        }
    }
}