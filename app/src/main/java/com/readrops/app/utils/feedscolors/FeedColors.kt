package com.readrops.app.utils.feedscolors

import androidx.palette.graphics.Palette
import com.readrops.db.entities.Feed
import com.readrops.app.utils.HtmlParser
import com.readrops.app.utils.Utils

fun setFeedColors(feed: Feed) {
    getFaviconLink(feed)

    if (feed.iconUrl != null) {
        val bitmap = Utils.getImageFromUrl(feed.iconUrl) ?: return
        val palette = Palette.from(bitmap).generate()

        val dominantSwatch = palette.dominantSwatch
        if (dominantSwatch != null)
            feed.textColor = dominantSwatch.rgb

        val mutedSwatch = palette.mutedSwatch
        if (mutedSwatch != null)
            feed.backgroundColor = mutedSwatch.rgb
    }
}

fun getFaviconLink(feed: Feed) {
    feed.iconUrl = if (feed.iconUrl != null)
        feed.iconUrl
    else
        HtmlParser.getFaviconLink(feed.siteUrl)
}



