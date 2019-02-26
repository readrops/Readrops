package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.repositories.LocalFeedRepository;

import java.util.List;

public class ManageFeedsViewModel extends AndroidViewModel {

    private Database db;
    private LiveData<List<FeedWithFolder>> feedsWithFolder;
    private LocalFeedRepository repository;

    public ManageFeedsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
        repository = new LocalFeedRepository(application);

        feedsWithFolder = db.feedDao().getAllFeedsWithFolder();
    }

    public LiveData<List<FeedWithFolder>> getFeedsWithFolder() {
        return feedsWithFolder;
    }

    public void updateFeedWithFolder(FeedWithFolder feedWithFolder) {
        repository.updateFeedWithFolder(feedWithFolder);
    }
}
