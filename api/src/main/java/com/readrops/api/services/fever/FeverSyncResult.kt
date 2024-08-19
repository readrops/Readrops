package com.readrops.api.services.fever

import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item

data class FeverSyncResult(
    var feverFeeds: FeverFeeds = FeverFeeds(),
    var folders: List<Folder> = listOf(),
    var items: List<Item> = listOf(),
    var unreadIds: List<String> = listOf(),
    var starredIds: List<String> = listOf(),
    var favicons: List<Favicon> = listOf(),
    var sinceId: Long = 0,
)