package com.readrops.app.database.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.DrawableRes;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.readrops.app.R;

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
        accountType = getAccountTypeFromCode(in.readInt());
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
        return accountType.name() + "_login_" + id ;
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
        dest.writeInt(accountType.code);
        dest.writeString(displayedName);
        dest.writeLong(lastModified);
        dest.writeByte((byte) (currentAccount ? 1 : 0));
        dest.writeString(login);
        dest.writeString(password);
    }

    public enum AccountType {
        LOCAL(0),
        NEXTCLOUD_NEWS(1),
        FEEDLY(2),
        FRESHRSS(3);

        private int code;

        public int getCode() {
            return code;
        }

        AccountType(int code) {
            this.code =  code;
        }
    }

    public static AccountType getAccountTypeFromCode(int code) {
        if (code == AccountType.LOCAL.getCode())
            return AccountType.LOCAL;
        else if (code == AccountType.NEXTCLOUD_NEWS.getCode())
            return AccountType.NEXTCLOUD_NEWS;
        else if (code == AccountType.FEEDLY.getCode())
            return AccountType.FEEDLY;
        else if (code ==  AccountType.FRESHRSS.getCode())
            return AccountType.FRESHRSS;

        return null;
    }

    public static @DrawableRes int getLogoFromAccountType(AccountType accountType) {
        switch (accountType) {
            case LOCAL:
                return R.drawable.ic_readrops;
            case NEXTCLOUD_NEWS:
                return R.drawable.ic_nextcloud_news;
            default:
                return R.drawable.ic_readrops;
        }
    }
}
