package com.readrops.app.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Account {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String url;

    @ColumnInfo(name = "account_name")
    private String accountName;

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

    public enum AccountType {
        NEXTCLOUD_NEWS(0),
        FEEDLY(1),
        FRESHRSS(2);

        private int code;

        public int getCode() {
            return code;
        }

        AccountType(int code) {
            this.code =  code;
        }
    }
}
