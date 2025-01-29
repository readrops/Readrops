package com.readrops.api.localfeed

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.readrops.api.utils.extensions.nullableTextRecursively
import com.readrops.db.entities.Item

object RSSMedia {

    fun parseMediaContent(konsumer: Konsumer, item: Item) = with(konsumer) {
        val url = attributes.getValueOrNull("url")

        if (url != null && isUrlImage(url) && item.imageLink == null) {
            item.imageLink = url
        }

        konsumer.skipContents() // ignore media content sub elements
    }

    fun parseMediaGroup(konsumer: Konsumer, item: Item) = with(konsumer) {
        allChildrenAutoIgnore(Names.of("content", "thumbnail", "description")) {
            when (tagName) {
                "media:content" -> parseMediaContent(this, item)
                "media:thumbnail"-> parseMediaContent(this, item)
                "media:description" -> {
                    // Youtube case, might be useful for others
                    val description = nullableTextRecursively()
                    if (item.text == null) {
                        item.content = description
                    }
                }
                else -> skipContents()
            }
        }
    }

    private fun isUrlImage(url: String): Boolean = with(url) {
        return endsWith(".jpg") || endsWith(".jpeg") || endsWith(".png")
    }
}