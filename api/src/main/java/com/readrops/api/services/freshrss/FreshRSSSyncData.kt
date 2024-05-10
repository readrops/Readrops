package com.readrops.api.services.freshrss

data class FreshRSSSyncData(
    var lastModified: Long = 0,
    var readIds: List<String> = listOf(),
    var unreadIds: List<String> = listOf(),
    var starredIds: List<String> = listOf(),
    var unstarredIds: List<String> = listOf(),
)