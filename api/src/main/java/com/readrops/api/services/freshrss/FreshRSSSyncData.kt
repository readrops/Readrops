package com.readrops.api.services.freshrss

data class FreshRSSSyncData(
    var lastModified: Long = 0,
    var readItemsIds: List<String> = listOf(),
    var unreadItemsIds: List<String> = listOf(),
    var starredItemsIds: List<String> = listOf(),
    var unstarredItemsIds: List<String> = listOf(),
)