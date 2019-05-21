package com.readrops.app.views;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;

import com.readrops.app.database.entities.Account;

public class AccountType implements Parcelable {

    private String name;

    private @DrawableRes int logoId;

    private Account.AccountType accountType;

    public AccountType(String name, int logoId, Account.AccountType accountType) {
        this.name = name;
        this.logoId = logoId;
        this.accountType = accountType;
    }

    protected AccountType(Parcel in) {
        name = in.readString();
        logoId = in.readInt();
        accountType = Account.AccountType.values()[in.readInt()];
    }

    public static final Creator<AccountType> CREATOR = new Creator<AccountType>() {
        @Override
        public AccountType createFromParcel(Parcel in) {
            return new AccountType(in);
        }

        @Override
        public AccountType[] newArray(int size) {
            return new AccountType[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @DrawableRes
    int getLogoId() {
        return logoId;
    }

    public void setLogoId(@DrawableRes int logoId) {
        this.logoId = logoId;
    }

    public Account.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(Account.AccountType accountType) {
        this.accountType = accountType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(logoId);
        dest.writeInt(accountType.getCode());
    }
}
