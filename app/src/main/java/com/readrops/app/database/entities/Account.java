package com.readrops.app.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.readrops.app.R;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsCredentials;

@Entity
public class Account implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String url;

    @ColumnInfo(name = "account_name")
    private String accountName;

    @ColumnInfo(name = "displayed_name")
    private String displayedName;

    @ColumnInfo(name = "account_type")
    private AccountType accountType;

    @ColumnInfo(name = "last_modified")
    private long lastModified;

    @ColumnInfo(name = "current_account")
    private boolean currentAccount;

    @Ignore
    private String login;

    @Ignore
    private String password;

    public Account() {
    }

    @Ignore
    public Account(String url, String accountName, AccountType accountType) {
        this.url = url;
        this.accountName = accountName;
        this.accountType = accountType;
    }

    protected Account(Parcel in) {
        id = in.readInt();
        url = in.readString();
        accountName = in.readString();
        accountType = AccountType.values()[in.readInt()];
        displayedName = in.readString();
        lastModified = in.readLong();
        currentAccount = in.readByte() != 0;
        login = in.readString();
        password = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDisplayedName() {
        return displayedName;
    }

    public void setDisplayedName(String displayedName) {
        this.displayedName = displayedName;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(boolean currentAccount) {
        this.currentAccount = currentAccount;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginKey() {
        return accountType.name() + "_login_" + id;
    }

    public String getPasswordKey() {
        return accountType.name() + "_password_" + id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(accountName);
        dest.writeInt(accountType.ordinal());
        dest.writeString(displayedName);
        dest.writeLong(lastModified);
        dest.writeByte((byte) (currentAccount ? 1 : 0));
        dest.writeString(login);
        dest.writeString(password);
    }

    public enum AccountType implements Parcelable {
        LOCAL(R.drawable.ic_readrops, R.string.local_account),
        NEXTCLOUD_NEWS(R.drawable.ic_nextcloud_news, R.string.nextcloud_news),
        FEEDLY(R.drawable.ic_feedly, R.string.feedly),
        FRESHRSS(R.drawable.ic_freshrss, R.string.freshrss);

        private @DrawableRes
        int iconRes;
        private @StringRes
        int name;

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

        public @DrawableRes
        int getIconRes() {
            return iconRes;
        }

        public @StringRes
        int getName() {
            return name;
        }

        AccountType(@DrawableRes int iconRes, @StringRes int name) {
            this.iconRes = iconRes;
            this.name = name;
        }
    }

    public NextNewsCredentials toNextNewsCredentials() {
        return new NextNewsCredentials(login, password, url);
    }

    public boolean isLocal() {
        return accountType == AccountType.LOCAL;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        else if (!(obj instanceof Account))
            return false;
        else
            return this.id == ((Account) obj).getId();
    }
}
