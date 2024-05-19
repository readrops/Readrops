package com.readrops.db.entities.account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountConfig(
    val isFeedUrlReadOnly: Boolean, // Enable or disable feed url modification in Feed Tab
    val canCreateFolder: Boolean, // Enable or disable folder creation in Feed Tab
    val isNoFolderCase: Boolean, // Add a "No folder" option when modifying a feed's folder TODO add better name
    val useSeparateState: Boolean, // Let know if it uses ItemState table to synchronize read/star state
) : Parcelable {

    companion object {
        @JvmField
        val LOCAL = AccountConfig(
            isFeedUrlReadOnly = false,
            canCreateFolder = true,
            isNoFolderCase = false,
            useSeparateState = false,
        )

        @JvmField
        val NEXTCLOUD_NEWS = AccountConfig(
            isFeedUrlReadOnly = false,
            canCreateFolder = true,
            isNoFolderCase = false,
            useSeparateState = false,
        )

        @JvmField
        val FRESHRSS = AccountConfig(
            isFeedUrlReadOnly = true,
            canCreateFolder = false,
            isNoFolderCase = true,
            useSeparateState = true,
        )
    }
}