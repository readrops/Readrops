package com.readrops.db.util

import androidx.room.TypeConverter
import com.readrops.db.entities.account.AccountType
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long): LocalDateTime {
        return DateUtils.fromEpochSeconds(value / 1000L)
    }

    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(DateUtils.defaultOffset).toEpochMilli()
    }

    @TypeConverter
    fun fromAccountTypeCode(ordinal: Int): AccountType {
        return AccountType.entries[ordinal]
    }

    @TypeConverter
    fun getAccountTypeCode(accountType: AccountType): Int {
        return accountType.ordinal
    }
}