package com.readrops.app.database.entities.account;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.readrops.app.R;

public enum AccountType implements Parcelable {
    LOCAL(R.mipmap.ic_launcher, R.string.local_account, AccountConfig.LOCAL),
    NEXTCLOUD_NEWS(R.drawable.ic_nextcloud_news, R.string.nextcloud_news, AccountConfig.NEXTNEWS),
    FEEDLY(R.drawable.ic_feedly, R.string.feedly, null),
    FRESHRSS(R.drawable.ic_freshrss, R.string.freshrss, AccountConfig.FRESHRSS);

    private @DrawableRes int iconRes;
    private @StringRes int name;
    private AccountConfig accountConfig;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AccountType> CREATOR = new Creator<AccountType>() {
        @Override
        public AccountType createFromParcel(Parcel in) {
            return AccountType.values()[in.readInt()];
        }

        @Override
        public AccountType[] newArray(int size) {
            return new AccountType[size];
        }
    };

    public @DrawableRes int getIconRes() {
        return iconRes;
    }

    public @StringRes int getName() {
        return name;
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    AccountType(@DrawableRes int iconRes, @StringRes int name, AccountConfig accountConfig) {
        this.iconRes = iconRes;
        this.name = name;
        this.accountConfig = accountConfig;
    }
}
