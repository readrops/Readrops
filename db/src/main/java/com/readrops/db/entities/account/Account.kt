package com.readrops.db.entities.account

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var url: String? = null,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "displayed_name") var displayedName: String? = null,
    @ColumnInfo(name = "type") var type: AccountType? = null,
    @ColumnInfo(name = "last_modified") var lastModified: Long = 0,
    @ColumnInfo(name = "current_account") var isCurrentAccount: Boolean = false,
    var token: String? = null,
    @ColumnInfo(name = "write_token") var writeToken: String? = null,
    @ColumnInfo(name = "notifications_enabled") var isNotificationsEnabled: Boolean = false,
    @Ignore var login: String? = null,
    @Ignore var password: String? = null,
) : Serializable {

    val config: AccountConfig
        get() = type!!.config

    val isLocal
        get() = type == AccountType.LOCAL

    fun `is`(accountType: AccountType) = this.type == accountType

    val loginKey
        get() = type!!.name + "_login_" + id

    val passwordKey
        get() = type!!.name + "_password_" + id
}