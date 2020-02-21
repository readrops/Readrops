package com.readrops.app.utils

import android.graphics.Bitmap
import com.readrops.readropsdb.entities.Item

data class SyncResultNotifContent(val title: String?,
                                  val content: String?,
                                  val largeIcon: Bitmap?,
                                  val item: Item?)