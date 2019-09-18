package com.readrops.app.database.entities.account;

public class AccountConfig {

    public static final AccountConfig LOCAL = new AccountConfigBuilder()
            .setFeedUrlEditable(true)
            .setFolderCreation(true)
            .setNoFolderCase(false)
            .build();

    public static final AccountConfig NEXTNEWS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(true)
            .setNoFolderCase(false)
            .build();

    public static final AccountConfig FRESHRSS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(false)
            .setNoFolderCase(true)
            .build();

    private boolean feedUrlEditable;

    private boolean folderCreation;

    private boolean noFolderCase;

    public boolean isFeedUrlEditable() {
        return feedUrlEditable;
    }

    public boolean isFolderCreation() {
        return folderCreation;
    }

    public boolean isNoFolderCase() {
        return noFolderCase;
    }

    public AccountConfig(AccountConfigBuilder builder) {
        this.feedUrlEditable = builder.feedUrlEditable;
        this.folderCreation = builder.folderCreation;
        this.noFolderCase = builder.noFolderCase;
    }

    public static class AccountConfigBuilder {
        private boolean feedUrlEditable;
        private boolean folderCreation;
        private boolean noFolderCase;

        public AccountConfigBuilder setFeedUrlEditable(boolean feedUrlEditable) {
            this.feedUrlEditable = feedUrlEditable;
            return this;
        }

        public AccountConfigBuilder setFolderCreation(boolean folderCreation) {
            this.folderCreation = folderCreation;
            return this;
        }

        public AccountConfigBuilder setNoFolderCase(boolean noFolderCase) {
            this.noFolderCase = noFolderCase;
            return this;
        }

        public AccountConfig build() {
            return new AccountConfig(this);
        }
    }
}
