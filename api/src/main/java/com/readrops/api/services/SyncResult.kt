package com.readrops.api.services

import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item

data class SyncResult(
    var items: List<Item> = mutableListOf(),
    var starredItems: List<Item> = mutableListOf(),
    var feeds: List<Feed> = listOf(),
    var folders: List<Folder> = listOf(),
    var unreadIds: List<String>? = null,
    var readIds: List<String>? = null,
    var starredIds: List<String>? = null,
    var isError: Boolean = false
)
