package com.readrops.app.repositories;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.util.TimingLogger;

import com.readrops.app.database.entities.Account;
import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.FeedMatcher;
import com.readrops.app.utils.ItemMatcher;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsAPI;
import com.readrops.readropslibrary.services.nextcloudnews.SyncData;
import com.readrops.readropslibrary.services.nextcloudnews.SyncResult;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeed;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFeeds;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolder;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsFolders;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsItem;
import com.readrops.readropslibrary.services.nextcloudnews.json.NextNewsRenameFeed;
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

public class NextNewsRepository extends ARepository {

    private static final String TAG = NextNewsRepository.class.getSimpleName();

    public NextNewsRepository(Application application) {
        super(application);
    }

    @Override
    public Single<Boolean> login(Account account) {
        return Single.create(emitter -> {
            NextNewsAPI newsAPI = new NextNewsAPI();
            NextNewsUser user = newsAPI.login(account.toCredentials());

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

                SyncResult syncResult = newsAPI.sync(account.toCredentials(), syncType, syncData);

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
                    NextNewsFeeds nextNewsFeeds = newsAPI.createFeed(account.toCredentials(), result.getUrl(), 0);

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
    public Completable updateFeed(Feed feed, Account account) {
        return Completable.create(emitter -> {
            NextNewsAPI api = new NextNewsAPI();

            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());

            NextNewsRenameFeed newsRenameFeed = new NextNewsRenameFeed(feed.getRemoteId(), feed.getName());

            NextNewsFeed newsFeed;
            if (folder != null)
                newsFeed = new NextNewsFeed(feed.getRemoteId(), folder.getRemoteId());
            else
                newsFeed = new NextNewsFeed(feed.getRemoteId(), 0); // 0 for no folder

            try {
                if (api.renameFeed(account.toCredentials(), newsRenameFeed) &&
                        api.changeFeedFolder(account.toCredentials(), newsFeed)) {
                    if (folder != null)
                        database.feedDao().updateFeedFields(feed.getId(), feed.getName(), feed.getUrl(), folder.getId());
                    else
                        database.feedDao().updateFeedFields(feed.getId(), feed.getName(), feed.getUrl(), null);
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        });
    }

    @Override
    public Completable deleteFeed(Feed feed, Account account) {
        return Completable.create(emitter -> {
            NextNewsAPI api = new NextNewsAPI();

            try {
                if (api.deleteFeed(account.toCredentials(), feed.getRemoteId())) {
                    database.feedDao().delete(feed.getId());
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        });
    }

    @Override
    public Completable addFolder(Folder folder, Account account) {
        return Completable.create(emitter -> {
            NextNewsAPI api = new NextNewsAPI();

            try {
                NextNewsFolders folders = api.createFolder(account.toCredentials(), new NextNewsFolder(folder.getRemoteId(), folder.getName()));

                if (folders != null)
                    insertFolders(folders.getFolders(), account);
                else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        });
    }

    @Override
    public Completable updateFolder(Folder folder, Account account) {
        return Completable.create(emitter -> {
            NextNewsAPI api = new NextNewsAPI();

            try {
                if (api.renameFolder(account.toCredentials(), new NextNewsFolder(folder.getRemoteId(), folder.getName()))) {
                    database.folderDao().update(folder);
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        });
    }

    @Override
    public Completable deleteFolder(Folder folder, Account account) {
        return Completable.create(emitter -> {
            NextNewsAPI api = new NextNewsAPI();

            try {
                if (api.deleteFolder(account.toCredentials(), new NextNewsFolder(folder.getRemoteId(), folder.getName()))) {
                    database.folderDao().delete(folder);
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        });
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
        setFaviconUtils(insertedFeeds);

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
