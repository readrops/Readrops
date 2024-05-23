package com.readrops.api.services

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item

data class SyncResult(
    var items: List<Item> = mutableListOf(),
    var starredItems: List<Item> = mutableListOf(),
    var feeds: List<Feed> = listOf(),
    var newFeedIds: List<Long> = listOf(),
    var folders: List<Folder> = listOf(),
    var unreadIds: List<String> = listOf(),
    var readIds: List<String> = listOf(),
    var starredIds: List<String> = listOf(),
    var isError: Boolean = false
)
