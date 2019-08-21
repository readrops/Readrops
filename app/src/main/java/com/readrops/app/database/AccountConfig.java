package com.readrops.app.database;

public class AccountConfig {

    public static final AccountConfig LOCAL = new AccountConfigBuilder()
            .setFeedUrlEditable(true)
            .setFolderCreation(true)
            .build();

    public static final AccountConfig NEXTNEWS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(true)
            .build();

    public static final AccountConfig FRESHRSS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(false)
            .build();

    private boolean feedUrlEditable;

    public boolean isFeedUrlEditable() {
        return feedUrlEditable;
    }

    public boolean isFolderCreation() {
        return folderCreation;
    }

    private boolean folderCreation;

    public AccountConfig(boolean feedUrlEditable, boolean folderCreation) {
        this.feedUrlEditable = feedUrlEditable;
        this.folderCreation = folderCreation;
    }

    public static class AccountConfigBuilder {
        private boolean feedUrlEditable;
        private boolean folderCreation;

        public AccountConfigBuilder setFeedUrlEditable(boolean feedUrlEditable) {
            this.feedUrlEditable = feedUrlEditable;
            return this;
        }

        public AccountConfigBuilder setFolderCreation(boolean folderCreation) {
            this.folderCreation = folderCreation;
            return this;
        }

        public AccountConfig build() {
            return new AccountConfig(feedUrlEditable, folderCreation);
        }
    }
}
