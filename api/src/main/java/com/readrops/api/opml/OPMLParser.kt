package com.readrops.api.opml

import com.gitlab.mvysny.konsumexml.konsumeXml
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import org.redundent.kotlin.xml.xml
import java.io.InputStream
import java.io.OutputStream

object OPMLParser {

    suspend fun read(stream: InputStream): Map<Folder?, List<Feed>> {
        try {
            val adapter = OPMLAdapter()
            val opml = adapter.fromXml(stream.konsumeXml())

            stream.close()
            return opml
        } catch (e: Exception) {
            throw ParseException(e.message)
        }
    }

    suspend fun write(foldersAndFeeds: Map<Folder?, List<Feed>>, outputStream: OutputStream) {
        val opml = xml("opml") {
            attribute("version", "2.0")

            "head" {
                -"Subscriptions"
            }

            "body" {
                for (folderAndFeeds in foldersAndFeeds) {
                    if (folderAndFeeds.key != null) { // feeds with folder
                        "outline" {
                            folderAndFeeds.key?.name?.let {
                                attribute("title", it)
                                attribute("text", it)
                            }

                            for (feed in folderAndFeeds.value) {
                                "outline" {
                                    feed.name?.let { attribute("title", it) }
                                    attribute("xmlUrl", feed.url!!)
                                    feed.siteUrl?.let { attribute("htmlUrl", it) }
                                }
                            }
                        }
                    } else {
                        for (feed in folderAndFeeds.value) { // feeds without folder
                            "outline" {
                                feed.name?.let { attribute("title", it) }
                                attribute("xmlUrl", feed.url!!)
                                feed.siteUrl?.let { attribute("htmlUrl", it) }
                            }
                        }
                    }
                }
            }
        }

        outputStream.write(opml.toString().toByteArray())
        outputStream.flush()
        outputStream.close()
    }
}