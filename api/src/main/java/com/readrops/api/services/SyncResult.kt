package com.readrops.api.services

import com.readrops.readropsdb.entities.Feed
import com.readrops.readropsdb.entities.Folder
import com.readrops.readropsdb.entities.Item

class SyncResult {

    var items: List<Item> = mutableListOf()

    var feeds: List<Feed> = listOf()

    var folders: List<Folder> = listOf()

    var isError: Boolean = false
}
