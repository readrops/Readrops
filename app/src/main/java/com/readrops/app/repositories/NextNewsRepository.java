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
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsAPI;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsSyncData;
import com.readrops.readropslibrary.services.nextcloudnews.NextNewsSyncResult;
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
    public Single<Boolean> login(Account account, boolean insert) {
        return Single.create(emitter -> {
            NextNewsAPI newsAPI = new NextNewsAPI(account.toNextNewsCredentials());
            NextNewsUser user = newsAPI.login();

            if (user != null) {
                account.setDisplayedName(user.getDisplayName());
                account.setCurrentAccount(true);

                if (insert)
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
                NextNewsAPI newsAPI = new NextNewsAPI(account.toNextNewsCredentials());
                long lastModified = LocalDateTime.now().toDateTime().getMillis();
                SyncType syncType;

                if (account.getLastModified() != 0)
                    syncType = SyncType.CLASSIC_SYNC;
                else
                    syncType = SyncType.INITIAL_SYNC;

                NextNewsSyncData syncData = new NextNewsSyncData();

                if (syncType == SyncType.CLASSIC_SYNC) {
                    syncData.setLastModified(account.getLastModified() / 1000L);
                    syncData.setReadItems(database.itemDao().getReadChanges());
                    syncData.setUnreadItems(database.itemDao().getUnreadChanges());
                }

                TimingLogger timings = new TimingLogger(TAG, "nextcloud news " + syncType.name().toLowerCase());
                NextNewsSyncResult syncResult = newsAPI.sync(syncType, syncData);
                timings.addSplit("server queries");

                if (!syncResult.isError()) {

                    insertFolders(syncResult.getFolders(), account);
                    timings.addSplit("insert folders");

                    insertFeeds(syncResult.getFeeds(), account);
                    timings.addSplit("insert feeds");

                    insertItems(syncResult.getItems(), account, syncType == SyncType.INITIAL_SYNC);
                    timings.addSplit("insert items");
                    timings.dumpToLog();

                    account.setLastModified(lastModified);
                    database.accountDao().updateLastModified(account.getId(), lastModified);
                    database.itemDao().resetReadChanges();

                    emitter.onComplete();
                } else
                    emitter.onError(new Throwable());

            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(e);
            }
        });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        return Single.create(emitter -> {
            List<FeedInsertionResult> feedInsertionResults = new ArrayList<>();
            NextNewsAPI newsAPI = new NextNewsAPI(account.toNextNewsCredentials());

            for (ParsingResult result : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    NextNewsFeeds nextNewsFeeds = newsAPI.createFeed(result.getUrl(), 0);

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
            NextNewsAPI api = new NextNewsAPI(account.toNextNewsCredentials());

            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());

            NextNewsRenameFeed newsRenameFeed = new NextNewsRenameFeed(Integer.parseInt(feed.getRemoteId()), feed.getName());

            NextNewsFeed newsFeed;
            if (folder != null)
                newsFeed = new NextNewsFeed(Integer.parseInt(feed.getRemoteId()), Integer.parseInt(folder.getRemoteId()));
            else
                newsFeed = new NextNewsFeed(Integer.parseInt(feed.getRemoteId()), 0); // 0 for no folder

            try {
                if (api.renameFeed(newsRenameFeed) && api.changeFeedFolder(newsFeed)) {
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
            NextNewsAPI api = new NextNewsAPI(account.toNextNewsCredentials());

            try {
                if (api.deleteFeed(Integer.parseInt(feed.getRemoteId()))) {
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
            NextNewsAPI api = new NextNewsAPI(account.toNextNewsCredentials());

            try {
                NextNewsFolders folders = api.createFolder(new NextNewsFolder(Integer.parseInt(folder.getRemoteId()), folder.getName()));

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
            NextNewsAPI api = new NextNewsAPI(account.toNextNewsCredentials());

            try {
                if (api.renameFolder(new NextNewsFolder(Integer.parseInt(folder.getRemoteId()), folder.getName()))) {
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
            NextNewsAPI api = new NextNewsAPI(account.toNextNewsCredentials());

            try {
                if (api.deleteFolder(new NextNewsFolder(Integer.parseInt(folder.getRemoteId()), folder.getName()))) {
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

    private List<Feed> insertFeeds(List<NextNewsFeed> nextNewsFeeds, Account account) {
        List<Feed> feeds = new ArrayList<>();

        for (NextNewsFeed nextNewsFeed : nextNewsFeeds) {
            feeds.add(FeedMatcher.nextNewsFeedToFeed(nextNewsFeed, account));
        }

        List<Long> insertedFeedsIds = database.feedDao().feedsUpsert(feeds, account);

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFaviconUtils(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<NextNewsFolder> nextNewsFolders, Account account) {
        List<Folder> folders = new ArrayList<>();

        for (NextNewsFolder nextNewsFolder : nextNewsFolders) {
            Folder folder = new Folder(nextNewsFolder.getName());
            folder.setAccountId(account.getId());
            folder.setRemoteId(String.valueOf(nextNewsFolder.getId()));

            folders.add(folder);
        }

        database.folderDao().foldersUpsert(folders, account);
    }

    private void insertItems(List<NextNewsItem> items, Account account, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (NextNewsItem nextNewsItem : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(String.valueOf(nextNewsItem.getFeedId()), account.getId());

            if (!initialSync && feedId > 0) {
                if (database.itemDao().remoteItemExists(String.valueOf(nextNewsItem.getId()), feedId))
                    break;
            }

            Item item = ItemMatcher.nextNewsItemToItem(nextNewsItem, feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            newItems.add(item);
        }

        Collections.sort(newItems, Item::compareTo);
        database.itemDao().insert(newItems);
    }
}
