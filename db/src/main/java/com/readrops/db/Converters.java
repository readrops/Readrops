package com.readrops.readropsdb;

import androidx.room.TypeConverter;

import com.readrops.readropsdb.entities.account.AccountType;

import org.joda.time.LocalDateTime;

public class Converters {

    @TypeConverter
    public LocalDateTime fromTimeStamp(Long value) {
        return new LocalDateTime(value);
    }

    @TypeConverter
    public long fromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.toDateTime().getMillis();
    }

    @TypeConverter
    public AccountType fromAccountTypeCode(int ordinal) {
        return AccountType.values()[ordinal];
    }

    @TypeConverter
    public int getAccountTypeCode(AccountType accountType) {
        return accountType.ordinal();
    }

}
