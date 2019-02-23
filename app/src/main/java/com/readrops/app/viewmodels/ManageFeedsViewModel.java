package com.readrops.app.viewmodels;

import android.app.Application;
import android.app.ListActivity;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.readrops.app.database.Database;
import com.readrops.app.database.pojo.FeedWithFolder;

import java.util.List;

public class ManageFeedsViewModel extends AndroidViewModel {

    private Database db;
    private LiveData<List<FeedWithFolder>> feedsWithFolder;

    public ManageFeedsViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);

        feedsWithFolder = db.feedDao().getAllFeedsWithFolder();
    }

    public LiveData<List<FeedWithFolder>> getFeedsWithFolder() {
        return feedsWithFolder;
    }
}
