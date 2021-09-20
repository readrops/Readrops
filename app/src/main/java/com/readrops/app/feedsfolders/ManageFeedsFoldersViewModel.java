package com.readrops.app.feedsfolders;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.readrops.app.repositories.ARepository;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;
import com.readrops.db.pojo.FeedWithFolder;
import com.readrops.db.pojo.FolderWithFeedCount;

import org.koin.core.parameter.ParametersHolderKt;
import org.koin.java.KoinJavaComponent;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public class ManageFeedsFoldersViewModel extends ViewModel {

    private final Database database;
    private LiveData<List<FeedWithFolder>> feedsWithFolder;
    private LiveData<List<Folder>> folders;
    private ARepository repository;

    private Account account;

    public ManageFeedsFoldersViewModel(@NonNull Database database) {
        this.database = database;
    }

    private void setup() {
        repository = KoinJavaComponent.get(ARepository.class, null,
                () -> ParametersHolderKt.parametersOf(account));

        feedsWithFolder = database.feedDao().getAllFeedsWithFolder(account.getId());
        folders = database.folderDao().getAllFolders(account.getId());
    }

    public LiveData<List<FeedWithFolder>> getFeedsWithFolder() {
        return feedsWithFolder;
    }

    public Completable updateFeedWithFolder(Feed feed) {
        return repository.updateFeed(feed);
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
        setup();
    }

    public LiveData<List<Folder>> getFolders() {
        return folders;
    }

    public LiveData<List<FolderWithFeedCount>> getFoldersWithFeedCount() {
        return database.folderDao().getFoldersWithFeedCount(account.getId());
    }

    public Single<Long> addFolder(Folder folder) {
        return repository.addFolder(folder);
    }

    public Completable updateFolder(Folder folder) {
        return repository.updateFolder(folder);
    }

    public Completable deleteFolder(Folder folder) {
        return repository.deleteFolder(folder);
    }

    public Completable deleteFeed(Feed feed) {
        return repository.deleteFeed(feed);
    }

    public Single<Integer> getFeedCountByAccount() {
        return database.feedDao().getFeedCount(account.getId());
    }
}
