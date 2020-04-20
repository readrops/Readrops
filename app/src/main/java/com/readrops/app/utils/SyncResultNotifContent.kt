package com.readrops.app.utils

import android.graphics.Bitmap
import com.readrops.readropsdb.entities.Item

class SyncResultNotifContent {
    var title: String? = null
    var content: String? = null
    var largeIcon: Bitmap? = null
    var item: Item? = null
    var accountId: Int? = null
}