package com.readrops.db.util

import androidx.room.TypeConverter
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
}