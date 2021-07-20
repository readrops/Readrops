package com.readrops.api.services.nextcloudnews

import com.readrops.db.pojo.StarItem

data class NextNewsSyncData(
        var lastModified: Long = 0,
        var unreadItems: List<String> = listOf(),
        var readItems: List<String> = listOf(),
        var starredItems: List<StarItem> = listOf(),
        var unstarredItems: List<StarItem> = listOf(),
)