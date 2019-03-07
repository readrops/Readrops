package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.repositories.LocalFeedRepository;

import java.util.List;

import io.reactivex.Completable;

public class ManageFeedsViewModel extends AndroidViewModel {

    private Database db;
    private LiveData<List<FeedWithFolder>> feedsWithFolder;
    private LiveData<List<Folder>> folders;
    private LocalFeedRepository repository;

    public ManageFeedsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
        repository = new LocalFeedRepository(application);

        feedsWithFolder = db.feedDao().getAllFeedsWithFolder();
        folders = db.folderDao().getAllFolders();
    }

    public LiveData<List<FeedWithFolder>> getFeedsWithFolder() {
        return feedsWithFolder;
    }

    public void updateFeedWithFolder(FeedWithFolder feedWithFolder) {
        repository.updateFeedWithFolder(feedWithFolder);
    }

    public LiveData<List<Folder>> getFolders() {
        return folders;
    }

    public Completable addFolder(Folder folder) {
        return repository.addFolder(folder);
    }

    public Completable deleteFeed(int feedId) {
        return repository.deleteFeed(feedId);
    }
}
