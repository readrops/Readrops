package com.readrops.api.services.fever

import com.readrops.api.services.fever.adapters.Favicon
import com.readrops.api.services.fever.adapters.FeverFeeds
import com.readrops.db.entities.Folder
import com.readrops.db.entities.Item

data class FeverSyncResult(
        val feverFeeds: FeverFeeds,
        val folders: List<Folder>,
        val items: List<Item>,
        val unreadIds: List<String>,
        val starredIds: List<String>,
        val favicons: List<Favicon>,
)