package com.readrops.db.entities.account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountConfig(
        val isFeedUrlEditable: Boolean, // Enables or disables feed url modification in management screen
        val isFolderCreation: Boolean, // Enables or disables folder creation in management screen
        val isNoFolderCase: Boolean, // Add a "No folder" option when modifying a feed's folder
        val useSeparateState: Boolean, // Let knows if it uses ItemState table to synchronize state
) : Parcelable {

    companion object {
        @JvmField
        val LOCAL = AccountConfig(
                isFeedUrlEditable = true,
                isFolderCreation = true,
                isNoFolderCase = false,
                useSeparateState = false,
        )

        @JvmField
        val NEXTCLOUD_NEWS = AccountConfig(
                isFeedUrlEditable = false,
                isFolderCreation = true,
                isNoFolderCase = false,
                useSeparateState = false,
        )

        @JvmField
        val FRESHRSS = AccountConfig(
                isFeedUrlEditable = false,
                isFolderCreation = false,
                isNoFolderCase = true,
                useSeparateState = true,
        )

        @JvmField
        val FEVER = AccountConfig(
                isFeedUrlEditable = false,
                isFolderCreation = false,
                isNoFolderCase = true,
                useSeparateState = true,
        )
    }
}