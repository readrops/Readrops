package com.readrops.db.entities.account

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Account(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var url: String? = null,
        @ColumnInfo(name = "account_name") var accountName: String? = null,
        @ColumnInfo(name = "displayed_name") var displayedName: String? = null,
        @ColumnInfo(name = "account_type") var accountType: AccountType? = null,
        @ColumnInfo(name = "last_modified") var lastModified: Long = 0,
        @ColumnInfo(name = "current_account") var isCurrentAccount: Boolean = false,
        var token: String? = null,
        var writeToken: String? = null, // TODO : see if there is a better solution to store specific service account fields
        @ColumnInfo(name = "notifications_enabled") var isNotificationsEnabled: Boolean = false,
        @Ignore var login: String? = null,
        @Ignore var password: String? = null,
) : Parcelable {

    constructor(accountUrl: String?, accountName: String, accountType: AccountType):
            this(url = accountUrl, accountName =  accountName, accountType = accountType)

    val config: AccountConfig
        get() =  accountType!!.accountConfig!!

    val isLocal
        get() = accountType == AccountType.LOCAL

    fun `is`(accountType: AccountType) = this.accountType == accountType

    val loginKey
        get() = accountType!!.name + "_login_" + id

    val passwordKey
        get() = accountType!!.name + "_password_" + id
}