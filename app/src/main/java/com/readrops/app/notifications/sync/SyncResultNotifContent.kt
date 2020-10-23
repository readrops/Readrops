package com.readrops.app.notifications.sync

import android.graphics.Bitmap
import com.readrops.db.entities.Item

class SyncResultNotifContent {
    var title: String? = null
    var content: String? = null
    var largeIcon: Bitmap? = null
    var item: Item? = null
    var accountId: Int? = null
}