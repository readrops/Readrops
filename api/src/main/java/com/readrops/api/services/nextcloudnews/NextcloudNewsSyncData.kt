package com.readrops.api.services.nextcloudnews

import com.readrops.db.pojo.StarItem

data class NextcloudNewsSyncData(
    val lastModified: Long = 0,
    val readIds: List<Int> = listOf(),
    val unreadIds: List<Int> = listOf(),
    val starredIds: List<StarItem> = listOf(),
    val unstarredIds: List<StarItem> = listOf(),
)