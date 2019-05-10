package com.readrops.app.repositories;

import android.app.Application;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.services.nextcloudnews.Credentials;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsAPI;
import com.readrops.readropslibrary.utils.LibUtils;

import java.io.IOException;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class NextNewsRepository extends ARepository {

    public NextNewsRepository(Application application) {
        super(application);
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds) {
        return Observable.create(emitter -> {
            try {
                NextNewsAPI newsAPI = new NextNewsAPI();

                Credentials credentials = new Credentials("", LibUtils.NEXTCLOUD_PASSWORD, "");
                newsAPI.sync(credentials, NextNewsAPI.SyncType.INITIAL_SYNC, null);

                emitter.onComplete();
            } catch (IOException e) {
                e.printStackTrace();
                emitter.onError(e);
            }
        });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        return null;
    }

    @Override
    public void updateFeedWithFolder(FeedWithFolder feedWithFolder) {

    }

    @Override
    public Completable deleteFeed(int feedId) {
        return null;
    }

    @Override
    public Completable addFolder(Folder folder) {
        return null;
    }
}
