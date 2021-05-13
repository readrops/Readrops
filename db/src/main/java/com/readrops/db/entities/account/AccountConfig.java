package com.readrops.db.entities.account;

public class AccountConfig {

    public static final AccountConfig LOCAL = new AccountConfigBuilder()
            .setFeedUrlEditable(true)
            .setFolderCreation(true)
            .setNoFolderCase(false)
            .setUseSeparateState(false)
            .build();

    public static final AccountConfig NEXTCLOUD_NEWS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(true)
            .setNoFolderCase(false)
            .setUseSeparateState(false)
            .build();

    public static final AccountConfig FRESHRSS = new AccountConfigBuilder()
            .setFeedUrlEditable(false)
            .setFolderCreation(false)
            .setNoFolderCase(true)
            .setUseSeparateState(true)
            .build();

    private final boolean feedUrlEditable;

    private final boolean folderCreation;

    private final boolean noFolderCase;

    /*
    Let knows if it uses ItemState table to synchronize state
     */
    private final boolean useSeparateState;

    public boolean isFeedUrlEditable() {
        return feedUrlEditable;
    }

    public boolean isFolderCreation() {
        return folderCreation;
    }

    public boolean isNoFolderCase() {
        return noFolderCase;
    }

    public boolean useSeparateState() {
        return useSeparateState;
    }

    public AccountConfig(AccountConfigBuilder builder) {
        this.feedUrlEditable = builder.feedUrlEditable;
        this.folderCreation = builder.folderCreation;
        this.noFolderCase = builder.noFolderCase;
        this.useSeparateState = builder.useSeparateState;
    }

    public static class AccountConfigBuilder {
        private boolean feedUrlEditable;
        private boolean folderCreation;
        private boolean noFolderCase;
        private boolean useSeparateState;

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

        public AccountConfigBuilder setUseSeparateState(boolean useSeparateState) {
            this.useSeparateState = useSeparateState;
            return this;
        }

        public AccountConfig build() {
            return new AccountConfig(this);
        }
    }
}
