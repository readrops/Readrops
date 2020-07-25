package com.readrops.app.utils.feedscolors

import androidx.palette.graphics.Palette
import com.readrops.app.utils.HtmlParser
import com.readrops.app.utils.Utils
import com.readrops.db.entities.Feed

fun setFeedColors(feed: Feed) {
    getFaviconLink(feed)

    if (feed.iconUrl != null) {
        val bitmap = Utils.getImageFromUrl(feed.iconUrl) ?: return
        val palette = Palette.from(bitmap).generate()

        val dominantSwatch = palette.dominantSwatch
        feed.textColor = if (dominantSwatch != null && !Utils.isColorTooBright(dominantSwatch.rgb)
                && !Utils.isColorTooDark(dominantSwatch.rgb)) {
            dominantSwatch.rgb
        } else 0


        val mutedSwatch = palette.mutedSwatch
        feed.backgroundColor = if (mutedSwatch != null && !Utils.isColorTooBright(mutedSwatch.rgb)
                && !Utils.isColorTooDark(mutedSwatch.rgb)) {
            mutedSwatch.rgb
        } else 0
    }
}

fun getFaviconLink(feed: Feed) {
    feed.iconUrl = if (feed.iconUrl != null)
        feed.iconUrl
    else
        HtmlParser.getFaviconLink(feed.siteUrl)
}



