package com.readrops.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.account.Account;
import com.readrops.db.pojo.FeedWithFolder;
import com.readrops.db.pojo.FolderWithFeedCount;
import com.readrops.app.repositories.ARepository;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

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
        try {
            repository = ARepository.repositoryFactory(account, getApplication());

            feedsWithFolder = db.feedDao().getAllFeedsWithFolder(account.getId());
            folders = db.folderDao().getAllFolders(account.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return db.folderDao().getFoldersWithFeedCount(account.getId());
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
        return db.feedDao().getFeedCount(account.getId());
    }
}
