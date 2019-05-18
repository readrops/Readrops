package com.readrops.app.repositories;

import android.app.Application;
import android.util.TimingLogger;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.pojo.FeedWithFolder;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.FeedMatcher;
import com.readrops.app.utils.ItemMatcher;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.services.nextcloudnews.Credentials;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsAPI;
import com.readrops.readropslibrary.services.nextcloudnews.SyncData;
import com.readrops.readropslibrary.services.nextcloudnews.SyncResult;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolder;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;

import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class NextNewsRepository extends ARepository {

    private static final String TAG = NextNewsRepository.class.getSimpleName();

    public NextNewsRepository(Application application) {
        super(application);
    }

    @Override
    public Single<Boolean> login(Account account) {
        return Single.create(emitter -> {
            NextNewsAPI newsAPI = new NextNewsAPI();

            Credentials credentials = new Credentials(account.getLogin(), account.getPassword(),
                    account.getUrl());
            NextNewsUser user = newsAPI.login(credentials);

            if (user != null) {
                account.setDisplayedName(user.getDisplayName());
                account.setCurrentAccount(true);

                account.setId((int) database.accountDao().insert(account));
                emitter.onSuccess(true);
            } else
                emitter.onSuccess(false);
        });
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds, Account account) {
        return Observable.create(emitter -> {
            try {
                NextNewsAPI newsAPI = new NextNewsAPI();
                long lastModified = LocalDateTime.now().toDateTime().getMillis();
                NextNewsAPI.SyncType syncType;

                if (account.getLastModified() != 0)
                    syncType = NextNewsAPI.SyncType.CLASSIC_SYNC;
                else
                    syncType = NextNewsAPI.SyncType.INITIAL_SYNC;

                SyncData syncData = new SyncData();

                if (syncType == NextNewsAPI.SyncType.CLASSIC_SYNC) {
                    syncData.setLastModified(account.getLastModified() / 1000L);
                    syncData.setReadItems(database.itemDao().getReadChanges());
                    syncData.setUnreadItems(database.itemDao().getUnreadChanges());
                }

                Credentials credentials = new Credentials(account.getLogin(), account.getPassword(),
                        account.getUrl());
                SyncResult syncResult = newsAPI.sync(credentials, syncType, syncData);

                if (!syncResult.isError()) {
                    TimingLogger timings = new TimingLogger(TAG, "sync");
                    insertFolders(syncResult.getFolders());
                    timings.addSplit("insert folders");

                    insertFeeds(syncResult.getFeeds());
                    timings.addSplit("insert feeds");

                    insertItems(syncResult.getItems(), syncType == NextNewsAPI.SyncType.INITIAL_SYNC);
                    timings.addSplit("insert items");
                    timings.dumpToLog();

                    account.setLastModified(lastModified);
                    database.accountDao().updateLastModified(account.getId(), lastModified);
                    database.itemDao().resetReadChanges();

                    emitter.onComplete();
                } else
                    emitter.onError(new Throwable());

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

    private void insertFeeds(List<NextNewsFeed> feeds) {
        List<Feed> newFeeds = new ArrayList<>();

        for (NextNewsFeed nextNewsFeed : feeds) {

            if (!database.feedDao().remoteFeedExists(nextNewsFeed.getId())) {
                Feed feed = FeedMatcher.nextNewsFeedToFeed(nextNewsFeed);

                // if the Nextcloud feed has a folder, it is already inserted, so we have to get its local id
                if (nextNewsFeed.getFolderId() != 0) {
                    int folderId = database.folderDao().getRemoteFolderLocalId(nextNewsFeed.getFolderId());

                    if (folderId != 0)
                        feed.setFolderId(folderId);
                }

                newFeeds.add(feed);
            }
        }

        long[] ids = database.feedDao().insert(newFeeds);

        List<Feed> insertedFeeds = database.feedDao().selectFromIdList(ids);
        Observable.<Feed>create(emitter -> {
            for (Feed feed : insertedFeeds) {
                setFavIconUtils(feed);
                emitter.onNext(feed);
            }
        }).subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .doOnNext(feed1 -> database.feedDao().updateColors(feed1.getId(),
                        feed1.getTextColor(), feed1.getBackgroundColor()))
                .subscribe();
    }

    private void insertFolders(List<NextNewsFolder> folders) {
        List<Folder> newFolders = new ArrayList<>();

        for (NextNewsFolder nextNewsFolder : folders) {

            if (!database.folderDao().remoteFolderExists(nextNewsFolder.getId())) {
                Folder folder = new Folder(nextNewsFolder.getName());
                folder.setRemoteId(nextNewsFolder.getId());

                newFolders.add(folder);
            }
        }

        database.folderDao().insert(newFolders);
    }

    private void insertItems(List<NextNewsItem> items, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (NextNewsItem nextNewsItem : items) {

            if (!initialSync) {
                if (database.itemDao().remoteItemExists(nextNewsItem.getId()))
                    continue; // skip the current item if it exists in the db
            }

            try {
                Feed feed = database.feedDao().getFeedByRemoteId(nextNewsItem.getFeedId());
                Item item = ItemMatcher.nextNewsItemToItem(nextNewsItem, feed);

                item.setReadTime(Utils.readTimeFromString(item.getContent()));

                newItems.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(newItems, Item::compareTo);
        database.itemDao().insert(newItems);
    }
}
