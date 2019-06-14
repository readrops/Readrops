package com.readrops.app.database;

import androidx.room.TypeConverter;

import com.readrops.app.database.entities.Account;

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
    public Account.AccountType fromAccountTypeCode(int ordinal) {
        return Account.AccountType.values()[ordinal];
    }

    @TypeConverter
    public int getAccountTypeCode(Account.AccountType accountType) {
        return accountType.ordinal();
    }

}
