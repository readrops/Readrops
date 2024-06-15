package com.readrops.app.repositories;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.services.SyncResult;
import com.readrops.api.services.SyncType;
import com.readrops.api.services.nextcloudnews.NextNewsDataSource;
import com.readrops.api.services.nextcloudnews.NextcloudNewsSyncData;
import com.readrops.api.utils.exceptions.UnknownFormatException;
import com.readrops.app.addfeed.FeedInsertionResult;
import com.readrops.app.addfeed.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.account.Account;
import com.readrops.db.pojo.ItemReadStarState;

import org.joda.time.LocalDateTime;
import org.koin.java.KoinJavaComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;

public class NextNewsRepository extends ARepository {

    private static final String TAG = NextNewsRepository.class.getSimpleName();

    private final NextNewsDataSource dataSource;

    public NextNewsRepository(NextNewsDataSource dataSource, Database database, @NonNull Context context, @Nullable Account account) {
        super(database, context, account);

        this.dataSource = dataSource;
    }

    @Override
    public Completable login(Account account, boolean insert) {
        setCredentials(account);
        return Single.<String>create(emitter -> {
            OkHttpClient httpClient = KoinJavaComponent.get(OkHttpClient.class);

            String displayName = dataSource.login(httpClient, account);
            emitter.onSuccess(displayName);
        }).flatMapCompletable(displayName -> {
            account.setDisplayedName(displayName);
            account.setCurrentAccount(true);

            if (insert) {
                return database.accountDao().insert(account)
                        .flatMapCompletable(id -> {
                            account.setId(id.intValue());
                            return Completable.complete();
                        });
            }

            return Completable.complete();
        });
    }

    @Override
    public Completable sync(@Nullable List<Feed> feeds, @Nullable FeedUpdate update) {
        setCredentials(account);
        return Completable.create(emitter -> {
            try {
                long lastModified = LocalDateTime.now().toDateTime().getMillis();
                SyncType syncType;

                if (account.getLastModified() != 0) {
                    syncType = SyncType.CLASSIC_SYNC;
                } else {
                    syncType = SyncType.INITIAL_SYNC;
                }

                NextcloudNewsSyncData syncData = new NextcloudNewsSyncData();

                /*if (syncType == SyncType.CLASSIC_SYNC) {
                    syncData.setLastModified(account.getLastModified() / 1000L);

                    List<ItemReadStarState> itemStateChanges = database
                            .itemStateChangesDao()
                            .getNextcloudNewsStateChanges(account.getId());

                    syncData.setReadIds(itemStateChanges.stream()
                            .filter(it -> it.getReadChange() && it.getRead())
                            .map(ItemReadStarState::getRemoteId)
                            .collect(Collectors.toList()));

                    syncData.setUnreadIds(itemStateChanges.stream()
                            .filter(it -> it.getReadChange() && !it.getRead())
                            .map(ItemReadStarState::getRemoteId)
                            .collect(Collectors.toList()));

                    List<String> starredItemsIds = itemStateChanges.stream()
                            .filter(it -> it.getStarChange() && it.getStarred())
                            .map(ItemReadStarState::getRemoteId)
                            .collect(Collectors.toList());

                    if (!starredItemsIds.isEmpty()) {
                        syncData.setStarredIds(database.itemDao().getStarChanges(starredItemsIds, account.getId()));
                    }

                    List<String> unstarredItemsIds = itemStateChanges.stream()
                            .filter(it -> it.getStarChange() && !it.getStarred())
                            .map(ItemReadStarState::getRemoteId)
                            .collect(Collectors.toList());

                    if (!unstarredItemsIds.isEmpty()) {
                        syncData.setUnstarredIds(database.itemDao().getStarChanges(unstarredItemsIds, account.getId()));
                    }

                }*/

                TimingLogger timings = new TimingLogger(TAG, "nextcloud news " + syncType.name().toLowerCase());
                SyncResult result = dataSource.sync(syncType, syncData);
                timings.addSplit("server queries");

                if (!result.isError()) {
                    syncResult = new SyncResult();

                    insertFolders(result.getFolders());
                    timings.addSplit("insert folders");

                    insertFeeds(result.getFeeds(), false);
                    timings.addSplit("insert feeds");

                    boolean initialSync = syncType == SyncType.INITIAL_SYNC;
                    insertItems(result.getItems(), initialSync);
                    timings.addSplit("insert items");

                    insertItems(result.getStarredItems(), initialSync);
                    timings.dumpToLog();

                    account.setLastModified(lastModified);
                    database.accountDao().updateLastModified(account.getId(), lastModified);

                    database.itemStateChangesDao().resetStateChanges(account.getId());

                    emitter.onComplete();
                } else {
                    emitter.onError(new Throwable());
                }

            } catch (Exception e) {
                Log.d(TAG, "sync: " + e.getMessage());
                emitter.onError(e);
            }
        });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        setCredentials(account);
        return Single.create(emitter -> {
            List<FeedInsertionResult> feedInsertionResults = new ArrayList<>();

            for (ParsingResult result : results) {
                FeedInsertionResult insertionResult = new FeedInsertionResult();

                try {
                    List<Feed> nextNewsFeeds = dataSource.createFeed(result.getUrl(), 0);

                    if (nextNewsFeeds != null) {
                        List<Feed> newFeeds = insertFeeds(nextNewsFeeds, true);
                        // there is always only one object in the list, see nextcloud news dataSource doc
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
        setCredentials(account);
        return Completable.create(emitter -> {
            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());

            if (folder != null)
                feed.setRemoteFolderId(folder.getRemoteId());
            else
                feed.setRemoteFolderId(String.valueOf(0)); // 0 for no folder

            try {
                if (dataSource.renameFeed(feed) && dataSource.changeFeedFolder(feed)) {
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
        setCredentials(account);
        return Completable.create(emitter -> {
            try {
                if (dataSource.deleteFeed(Integer.parseInt(feed.getRemoteId()))) {
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
        setCredentials(account);
        return Single.<Folder>create(emitter -> {
            try {
                List<Folder> folders = dataSource.createFolder(folder);

                if (folders != null) {
                    Folder nextNewsFolder = folders.get(0); // always only one item returned by the server, see doc
                    folder.setRemoteId(nextNewsFolder.getRemoteId());

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
        setCredentials(account);
        return Completable.create(emitter -> {
            try {
                if (dataSource.renameFolder(folder)) {
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
        setCredentials(account);
        return Completable.create(emitter -> {
            try {
                if (dataSource.deleteFolder(folder)) {
                    emitter.onComplete();
                } else
                    emitter.onError(new Exception("Unknown error"));

            } catch (Exception e) {
                emitter.onError(e);
            }

            emitter.onComplete();
        }).andThen(super.deleteFolder(folder));
    }

    private List<Feed> insertFeeds(List<Feed> nextNewsFeeds, boolean newFeeds) {
        for (Feed nextNewsFeed : nextNewsFeeds) {
            nextNewsFeed.setAccountId(account.getId());
        }

        List<Long> insertedFeedsIds;
        if (newFeeds) {
            insertedFeedsIds = database.feedDao().insert(nextNewsFeeds);
        } else {
            insertedFeedsIds = database.feedDao().feedsUpsert(nextNewsFeeds, account);
        }

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFeedsColors(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<Folder> nextNewsFolders) {
        for (Folder folder : nextNewsFolders) {
            folder.setAccountId(account.getId());
        }

        database.folderDao().foldersUpsert(nextNewsFolders, account);
    }

    private void insertItems(List<Item> items, boolean initialSync) {
        List<Item> itemsToInsert = new ArrayList<>();

        for (Item item : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(item.getFeedRemoteId(), account.getId());

            //if the item already exists, update only its read state
            if (!initialSync && feedId > 0 && database.itemDao().remoteItemExists(String.valueOf(item.getRemoteId()), feedId)) {
                database.itemDao().setReadAndStarState(item.getRemoteId(), item.isRead(), item.isStarred());
                continue;
            }

            item.setFeedId(feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            itemsToInsert.add(item);
        }

        if (!itemsToInsert.isEmpty()) {
            syncResult.setItems(itemsToInsert);

            Collections.sort(itemsToInsert, Item::compareTo);
            database.itemDao().insert(itemsToInsert);
        }
    }
}
