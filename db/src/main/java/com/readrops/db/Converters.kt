package com.readrops.db

import androidx.room.TypeConverter
import com.readrops.db.entities.account.AccountType
import org.joda.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long): LocalDateTime {
        return LocalDateTime(value)
    }

    @TypeConverter
    fun fromLocalDateTime(localDateTime: LocalDateTime): Long {
        return localDateTime.toDateTime().millis
    }

    @TypeConverter
    fun fromAccountTypeCode(ordinal: Int): AccountType {
        return AccountType.values()[ordinal]
    }

    @TypeConverter
    fun getAccountTypeCode(accountType: AccountType): Int {
        return accountType.ordinal
    }
}