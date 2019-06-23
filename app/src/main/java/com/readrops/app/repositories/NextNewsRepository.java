package com.readrops.app.repositories;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
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
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolder;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsUser;
import com.readrops.readropslibrary.utils.UnknownFormatException;

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
                    insertFolders(syncResult.getFolders(), account);
                    timings.addSplit("insert folders");

                    insertFeeds(syncResult.getFeeds(), account);
                    timings.addSplit("insert feeds");

                    insertItems(syncResult.getItems(), account, syncType == NextNewsAPI.SyncType.INITIAL_SYNC);
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
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        return Single.create(emitter -> {
            List<FeedInsertionResult> feedInsertionResults = new ArrayList<>();
            NextNewsAPI newsAPI = new NextNewsAPI();

            for (ParsingResult result : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    Credentials credentials = new Credentials(account.getLogin(), account.getPassword(), account.getUrl());
                    NextNewsFeeds nextNewsFeeds = newsAPI.createFeed(credentials, result.getUrl(), 0);

                    if (nextNewsFeeds != null) {
                        List<Feed> newFeeds = insertFeeds(nextNewsFeeds.getFeeds(), account);

                        // there is always only one object in the list, see nextcloud news api doc
                        insertionResult.setFeed(newFeeds.get(0));
                    } else
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);

                } catch (Exception e) {
                    if (e instanceof IOException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.NETWORK_ERROR);
                    else if (e instanceof UnknownFormatException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.FORMAT_ERROR);
                    else if (e instanceof SQLiteConstraintException)
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.DB_ERROR);
                    else
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);
                }

                feedInsertionResults.add(insertionResult);
            }

            emitter.onSuccess(feedInsertionResults);
        });
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

    private List<Feed> insertFeeds(List<NextNewsFeed> feeds, Account account) {
        List<Feed> newFeeds = new ArrayList<>();

        for (NextNewsFeed nextNewsFeed : feeds) {

            if (!database.feedDao().remoteFeedExists(nextNewsFeed.getId(), account.getId())) {
                Feed feed = FeedMatcher.nextNewsFeedToFeed(nextNewsFeed, account);

                // if the Nextcloud feed has a folder, it is already inserted, so we have to get its local id
                if (nextNewsFeed.getFolderId() != 0) {
                    int folderId = database.folderDao().getRemoteFolderLocalId(nextNewsFeed.getFolderId(), account.getId());

                    if (folderId != 0)
                        feed.setFolderId(folderId);
                } else
                    feed.setFolderId(null);

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

        return insertedFeeds;
    }

    private void insertFolders(List<NextNewsFolder> folders, Account account) {
        List<Folder> newFolders = new ArrayList<>();

        for (NextNewsFolder nextNewsFolder : folders) {

            if (!database.folderDao().remoteFolderExists(nextNewsFolder.getId(), account.getId())) {
                Folder folder = new Folder(nextNewsFolder.getName());
                folder.setRemoteId(nextNewsFolder.getId());
                folder.setAccountId(account.getId());

                newFolders.add(folder);
            }
        }

        database.folderDao().insert(newFolders);
    }

    private void insertItems(List<NextNewsItem> items, Account account, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (NextNewsItem nextNewsItem : items) {

            Feed feed = database.feedDao().getFeedByRemoteId(nextNewsItem.getFeedId(), account.getId());

            if (!initialSync && feed != null) {

                if (database.itemDao().remoteItemExists(nextNewsItem.getId(), feed.getId()))
                    break;
            }

            Item item = ItemMatcher.nextNewsItemToItem(nextNewsItem, feed);

            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            newItems.add(item);
        }

        Collections.sort(newItems, Item::compareTo);
        database.itemDao().insert(newItems);
    }
}
