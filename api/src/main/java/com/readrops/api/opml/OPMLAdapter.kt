package com.readrops.api.opml

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.localfeed.XmlAdapter
import com.readrops.api.utils.exceptions.ParseException
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder

class OPMLAdapter : XmlAdapter<Map<Folder?, List<Feed>>> {

    override fun fromXml(konsumer: Konsumer): Map<Folder?, List<Feed>> = try {
        var opml: Map<Folder?, List<Feed>>? = null

        konsumer.child("opml") {
            val version = attributes.getValueOrNull("version")

            if (version != "2.0")
                throw ParseException("Only 2.0 OPML is supported")

            allChildrenAutoIgnore(Names.of("body")) {
                opml = parseOutline(this)
            }
        }

        opml!!
    } catch (e: Exception) {
        throw ParseException(e)
    }

    /**
     * Parse outline and its children recursively
     * @param konsumer
     */
    private fun parseOutline(konsumer: Konsumer): MutableMap<Folder?, MutableList<Feed>> = with(konsumer) {
        val opml = mutableMapOf<Folder?, MutableList<Feed>>()

        children(Names.of("outline")) {
            val title = attributes.getValueOrNull("title")
                    ?: attributes.getValueOrNull("text")

            val xmlUrl = attributes.getValueOrNull("xmlUrl")
            val htmlUrl = attributes.getValueOrNull("htmlUrl")

            val recursiveOpml = parseOutline(this)

            // The outline is a folder/category
            if (recursiveOpml.containsKey(null) || xmlUrl.isNullOrEmpty()) {
                // if the outline doesn't have text or title value but contains sub outlines,
                // those sub outlines will be considered as not belonging to any folder and join the others at the top level of the hierarchy
                val folder = if (title != null) Folder(name = title) else null

                val feeds = recursiveOpml[null] ?: mutableListOf()
                opml += mapOf(folder to feeds)

                recursiveOpml.remove(null)
                opml += recursiveOpml
            } else { // the outline is a feed
                val feed = Feed().apply {
                    name = title
                    url = xmlUrl
                    siteUrl = htmlUrl
                }

                // parsed feed is linked to null to be assigned to the previous level folder
                if (opml.containsKey(null)) {
                    opml[null]?.plusAssign(feed)
                } else
                    opml += mapOf(null to mutableListOf(feed))
            }
        }

        return opml
    }
}