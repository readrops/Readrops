package com.readrops.app.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.repositories.NextNewsRepository;

import java.util.List;

import io.reactivex.Completable;

public class ManageFeedsFoldersViewModel extends AndroidViewModel {

    private Database db;
    private LiveData<List<FeedWithFolder>> feedsWithFolder;
    private LiveData<List<Folder>> folders;
    private ARepository repository;

    private Account account;

    public ManageFeedsFoldersViewModel(@NonNull Application application) {
        super(application);

        db = Database.getInstance(application);
    }

    private void setup() {
        switch (account.getAccountType()) {
            case LOCAL:
                repository = new LocalFeedRepository(getApplication());
                break;
            case NEXTCLOUD_NEWS:
                repository = new NextNewsRepository(getApplication());
                break;
        }

        feedsWithFolder = db.feedDao().getAllFeedsWithFolder(account.getId());
        folders = db.folderDao().getAllFolders(account.getId());
    }

    public LiveData<List<FeedWithFolder>> getFeedsWithFolder() {
        return feedsWithFolder;
    }

    public Completable updateFeedWithFolder(Feed feed) {
        return repository.updateFeed(feed, account);
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

    public Completable addFolder(Folder folder) {
        return repository.addFolder(folder, account);
    }

    public Completable updateFolder(Folder folder) {
        return repository.updateFolder(folder, account);
    }

    public Completable deleteFolder(Folder folder) {
        return repository.deleteFolder(folder, account);
    }

    public Completable deleteFeed(Feed feed) {
        return repository.deleteFeed(feed, account);
    }
}
