package com.readrops.app.repositories;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.app.utils.matchers.FeedMatcher;
import com.readrops.app.utils.matchers.ItemMatcher;
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

public class NextNewsRepository extends ARepository<NextNewsAPI> {

    private static final String TAG = NextNewsRepository.class.getSimpleName();

    public NextNewsRepository(@NonNull Application application, @Nullable Account account) {
        super(application, account);
    }

    @Override
    protected NextNewsAPI createAPI() {
        if (account != null)
            return new NextNewsAPI(account.toCredentials());

        return null;
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        return Single.<NextNewsUser>create(emitter -> {
            if (api == null)
                api = new NextNewsAPI(account.toCredentials());
            else
                api.setCredentials(account.toCredentials());

            NextNewsUser user = api.login();

            emitter.onSuccess(user);
        }).flatMap(user -> {
            if (user != null) {
                account.setDisplayedName(user.getDisplayName());
                account.setCurrentAccount(true);

                if (insert) {
                    return database.accountDao().insert(account)
                            .flatMap(id -> {
                                account.setId(id.intValue());
                                return Single.just(true);
                            });
                }

                return Single.just(true);
            } else
                return Single.just(false);
        });
    }

    @Override
    public Observable<Feed> sync(List<Feed> feeds) {
        return Observable.create(emitter -> {
            try {
                long lastModified = LocalDateTime.now().toDateTime().getMillis();
                SyncType syncType;

                if (account.getLastModified() != 0)
                    syncType = SyncType.CLASSIC_SYNC;
                else
                    syncType = SyncType.INITIAL_SYNC;

                NextNewsSyncData syncData = new NextNewsSyncData();

                if (syncType == SyncType.CLASSIC_SYNC) {
                    syncData.setLastModified(account.getLastModified() / 1000L);
                    syncData.setReadItems(database.itemDao().getReadChanges(account.getId()));
                    syncData.setUnreadItems(database.itemDao().getUnreadChanges(account.getId()));
                }

                TimingLogger timings = new TimingLogger(TAG, "nextcloud news " + syncType.name().toLowerCase());
                NextNewsSyncResult syncResult = api.sync(syncType, syncData);
                timings.addSplit("server queries");

                if (!syncResult.isError()) {

                    insertFolders(syncResult.getFolders());
                    timings.addSplit("insert folders");

                    insertFeeds(syncResult.getFeeds(), false);
                    timings.addSplit("insert feeds");

                    insertItems(syncResult.getItems(), syncType == SyncType.INITIAL_SYNC);
                    timings.addSplit("insert items");
                    timings.dumpToLog();

                    account.setLastModified(lastModified);
                    database.accountDao().updateLastModified(account.getId(), lastModified);
                    database.itemDao().resetReadChanges(account.getId());

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
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        return Single.create(emitter -> {
            List<FeedInsertionResult> feedInsertionResults = new ArrayList<>();

            for (ParsingResult result : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    NextNewsFeeds nextNewsFeeds = api.createFeed(result.getUrl(), 0);

                    if (nextNewsFeeds != null) {
                        List<Feed> newFeeds = insertFeeds(nextNewsFeeds.getFeeds(), true);
                        // there is always only one object in the list, see nextcloud news api doc
                        insertionResult.setFeed(newFeeds.get(0));
                    } else
                        insertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.UNKNOWN_ERROR);

                    insertionResult.setParsingResult(result);
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
    public Completable updateFeed(Feed feed) {
        return Completable.create(emitter -> {
            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());
            NextNewsRenameFeed newsRenameFeed = new NextNewsRenameFeed(Integer.parseInt(feed.getRemoteId()), feed.getName());

            NextNewsFeed newsFeed;
            if (folder != null)
                newsFeed = new NextNewsFeed(Integer.parseInt(feed.getRemoteId()), Integer.parseInt(folder.getRemoteId()));
            else
                newsFeed = new NextNewsFeed(Integer.parseInt(feed.getRemoteId()), 0); // 0 for no folder

            try {
                if (api.renameFeed(newsRenameFeed) && api.changeFeedFolder(newsFeed)) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error when updating feed"));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).andThen(super.updateFeed(feed));
    }

    @Override
    public Completable deleteFeed(Feed feed) {
        return Completable.create(emitter -> {
            try {
                if (api.deleteFeed(Integer.parseInt(feed.getRemoteId()))) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.deleteFeed(feed));
    }

    @Override
    public Single<Long> addFolder(Folder folder) {
        return Single.<Folder>create(emitter -> {
            try {
                int folderRemoteId = folder.getRemoteId() == null ? 0 : Integer.parseInt(folder.getRemoteId());
                NextNewsFolders folders = api.createFolder(new NextNewsFolder(folderRemoteId, folder.getName()));

                if (folders != null) {
                    NextNewsFolder nextNewsFolder = folders.getFolders().get(0); // always only one item returned by the server, see doc

                    folder.setName(nextNewsFolder.getName());
                    folder.setRemoteId(String.valueOf(nextNewsFolder.getId()));
                    emitter.onSuccess(folder);
                } else
                    emitter.onError(new Exception("Unknown error"));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).flatMap(folder1 -> database.folderDao().insert(folder));
    }

    @Override
    public Completable updateFolder(Folder folder) {
        return Completable.create(emitter -> {
            try {
                if (api.renameFolder(new NextNewsFolder(Integer.parseInt(folder.getRemoteId()), folder.getName()))) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.updateFolder(folder));
    }

    @Override
    public Completable deleteFolder(Folder folder) {
        return Completable.create(emitter -> {
            try {
                if (api.deleteFolder(new NextNewsFolder(Integer.parseInt(folder.getRemoteId()), folder.getName()))) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.deleteFolder(folder));
    }

    private List<Feed> insertFeeds(List<NextNewsFeed> nextNewsFeeds, boolean newFeeds) {
        List<Feed> feeds = new ArrayList<>();

        for (NextNewsFeed nextNewsFeed : nextNewsFeeds) {
            feeds.add(FeedMatcher.nextNewsFeedToFeed(nextNewsFeed, account));
        }

        List<Long> insertedFeedsIds;
        if (newFeeds) {
            insertedFeedsIds = database.feedDao().insert(feeds);
        } else {
            insertedFeedsIds = database.feedDao().feedsUpsert(feeds, account);
        }

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFeedsColors(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<NextNewsFolder> nextNewsFolders) {
        List<Folder> folders = new ArrayList<>();

        for (NextNewsFolder nextNewsFolder : nextNewsFolders) {
            Folder folder = new Folder(nextNewsFolder.getName());
            folder.setAccountId(account.getId());
            folder.setRemoteId(String.valueOf(nextNewsFolder.getId()));

            folders.add(folder);
        }

        database.folderDao().foldersUpsert(folders, account);
    }

    private void insertItems(List<NextNewsItem> items, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (NextNewsItem nextNewsItem : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(String.valueOf(nextNewsItem.getFeedId()), account.getId());

            if (!initialSync && feedId > 0 && database.itemDao().remoteItemExists(String.valueOf(nextNewsItem.getId()), feedId)) {
                database.itemDao().setReadState(String.valueOf(nextNewsItem.getId()), !nextNewsItem.isUnread());
                continue;
            }

            Item item = ItemMatcher.nextNewsItemToItem(nextNewsItem, feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            newItems.add(item);
        }

        if (!newItems.isEmpty()) {
            Collections.sort(newItems, Item::compareTo);
            database.itemDao().insert(newItems);
        }
    }
}
