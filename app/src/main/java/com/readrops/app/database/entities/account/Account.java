package com.readrops.app.database.entities.account;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.readrops.readropslibrary.services.Credentials;
import com.readrops.readropslibrary.services.freshrss.FreshRSSCredentials;
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

    private String token;
    private String writeToken; // TODO : see if there is a better solution to store specific service account fields

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
        token = in.readString();
        writeToken = in.readString();
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getWriteToken() {
        return writeToken;
    }

    public void setWriteToken(String writeToken) {
        this.writeToken = writeToken;
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
        dest.writeString(token);
        dest.writeString(writeToken);
    }

    public Credentials toCredentials() {
        switch (accountType) {
            case NEXTCLOUD_NEWS:
                return new NextNewsCredentials(login, password, url);
            case FRESHRSS:
                return new FreshRSSCredentials(token, url);
            default:
                return null;
        }
    }

    public boolean isLocal() {
        return accountType == AccountType.LOCAL;
    }

    public boolean is(AccountType accountType) {
        return this.accountType == accountType;
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
