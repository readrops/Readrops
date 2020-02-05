package com.readrops.readropslibrary.services

import com.readrops.readropsdb.entities.Feed
import com.readrops.readropsdb.entities.Folder
import com.readrops.readropsdb.entities.Item

class SyncResult {

    var items: List<Item> = listOf()

    var feeds: List<Feed> = listOf()

    var folders: List<Folder> = listOf()

    var isError: Boolean = false
}
