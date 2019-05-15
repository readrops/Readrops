package com.readrops.app.database;

import androidx.room.TypeConverter;

import com.readrops.app.database.entities.Account;

import org.joda.time.LocalDateTime;

import static com.readrops.app.database.entities.Account.AccountType.FEEDLY;
import static com.readrops.app.database.entities.Account.AccountType.FRESHRSS;
import static com.readrops.app.database.entities.Account.AccountType.NEXTCLOUD_NEWS;

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
    public Account.AccountType fromAccountTypeCode(int code) {
        if (code == NEXTCLOUD_NEWS.getCode())
            return NEXTCLOUD_NEWS;
        else if (code == FEEDLY.getCode())
            return FEEDLY;
        else if (code == FRESHRSS.getCode())
            return FRESHRSS;

        return null;
    }

    @TypeConverter
    public int getAccountTypeCode(Account.AccountType accountType) {
        return accountType.getCode();
    }


}
