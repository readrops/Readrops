package com.readrops.app.repositories;

import android.content.Context;
import android.util.Log;
import android.util.TimingLogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.services.SyncType;
import com.readrops.api.services.freshrss.FreshRSSDataSource;
import com.readrops.api.services.freshrss.FreshRSSSyncData;
import com.readrops.app.addfeed.FeedInsertionResult;
import com.readrops.app.addfeed.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.ItemStateId;
import com.readrops.db.entities.StarredItem;
import com.readrops.db.entities.account.Account;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class FreshRSSRepository extends ARepository {

    private static final String TAG = FreshRSSRepository.class.getSimpleName();

    private final FreshRSSDataSource dataSource;

    public FreshRSSRepository(FreshRSSDataSource dataSource, Database database, @NonNull Context context, @Nullable Account account) {
        super(database, context, account);

        this.dataSource = dataSource;
    }

    @Override
    public Completable login(Account account, boolean insert) {
        setCredentials(account);

        return dataSource.login(account.getLogin(), account.getPassword())
                .flatMap(token -> {
                    account.setToken(token);
                    setCredentials(account);

                    return dataSource.getWriteToken();
                })
                .flatMap(writeToken -> {
                    account.setWriteToken(writeToken);

                    return dataSource.getUserInfo();
                })
                .flatMapCompletable(userInfo -> {
                    account.setDisplayedName(userInfo.getUserName());

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
    public Observable<Feed> sync(List<Feed> feeds) {
        FreshRSSSyncData syncData = new FreshRSSSyncData();
        SyncType syncType;

        if (account.getLastModified() != 0) {
            syncType = SyncType.CLASSIC_SYNC;
            syncData.setLastModified(account.getLastModified());
        } else
            syncType = SyncType.INITIAL_SYNC;

        long newLastModified = DateTime.now().getMillis() / 1000L;
        TimingLogger logger = new TimingLogger(TAG, "FreshRSS sync timer");

        return Single.<FreshRSSSyncData>create(emitter -> {
            syncData.setReadItemsIds(/*database.itemDao().getReadChanges(account.getId()*/Collections.emptyList());
            syncData.setUnreadItemsIds(/*database.itemDao().getUnreadChanges(account.getId())*/Collections.emptyList());

            syncData.setStarredItemsIds(/*database.itemDao().getFreshRSSStarChanges(account.getId())*/Collections.emptyList());
            syncData.setUnstarredItemsIds(/*database.itemDao().getFreshRSSUnstarChanges(account.getId())*/Collections.emptyList());

            emitter.onSuccess(syncData);
        }).flatMap(syncData1 -> dataSource.sync(syncType, syncData1, account.getWriteToken()))
                .flatMapObservable(syncResult -> {
                    logger.addSplit("server queries");

                    insertFolders(syncResult.getFolders());
                    logger.addSplit("folders insertion");
                    insertFeeds(syncResult.getFeeds());
                    logger.addSplit("feeds insertion");

                    insertItems(syncResult.getItems());
                    logger.addSplit("items insertion");

                    insertStarredItems(syncResult.getStarredItems());
                    logger.addSplit("starred items insertion");

                    insertItemsIds(syncResult.getUnreadIds(), syncResult.getStarredIds());
                    logger.addSplit("insert and update items ids");

                    account.setLastModified(newLastModified);
                    database.accountDao().updateLastModified(account.getId(), newLastModified);

                    logger.dumpToLog();

                    this.syncResult = syncResult;

                    return Observable.empty();
                });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        List<Completable> completableList = new ArrayList<>();
        List<FeedInsertionResult> insertionResults = new ArrayList<>();

        for (ParsingResult result : results) {
            completableList.add(dataSource.createFeed(account.getWriteToken(), result.getUrl())
                    .doOnComplete(() -> {
                        FeedInsertionResult feedInsertionResult = new FeedInsertionResult();
                        feedInsertionResult.setParsingResult(result);
                        insertionResults.add(feedInsertionResult);
                    }).onErrorResumeNext(throwable -> {
                        Log.d(TAG, throwable.getMessage());

                        FeedInsertionResult feedInsertionResult = new FeedInsertionResult();

                        feedInsertionResult.setInsertionError(FeedInsertionResult.FeedInsertionError.ERROR);
                        feedInsertionResult.setParsingResult(result);
                        insertionResults.add(feedInsertionResult);

                        return Completable.complete();
                    }));
        }

        return Completable.concat(completableList)
                .andThen(Single.just(insertionResults));
    }

    @Override
    public Completable updateFeed(Feed feed) {
        return Single.<Folder>create(emitter -> {
            Folder folder = feed.getFolderId() == null ? null : database.folderDao().select(feed.getFolderId());
            emitter.onSuccess(folder);

        }).flatMapCompletable(folder -> dataSource.updateFeed(account.getWriteToken(),
                feed.getUrl(), feed.getName(), folder == null ? null : folder.getRemoteId())
                .andThen(super.updateFeed(feed)));
    }

    @Override
    public Completable deleteFeed(Feed feed) {
        return dataSource.deleteFeed(account.getWriteToken(), feed.getUrl())
                .andThen(super.deleteFeed(feed));
    }

    @Override
    public Single<Long> addFolder(Folder folder) {
        return dataSource.createFolder(account.getWriteToken(), folder.getName())
                .andThen(super.addFolder(folder));
    }

    @Override
    public Completable updateFolder(Folder folder) {
        return dataSource.updateFolder(account.getWriteToken(), folder.getRemoteId(), folder.getName())
                .andThen(Completable.create(emitter -> {
                    folder.setRemoteId("user/-/label/" + folder.getName());
                    emitter.onComplete();
                }))
                .andThen(super.updateFolder(folder));
    }

    @Override
    public Completable deleteFolder(Folder folder) {
        return dataSource.deleteFolder(account.getWriteToken(), folder.getRemoteId())
                .andThen(super.deleteFolder(folder));
    }

    private void insertFeeds(List<Feed> freshRSSFeeds) {
        freshRSSFeeds.stream().forEach(feed -> feed.setAccountId(account.getId()));

        List<Long> insertedFeedsIds = database.feedDao().feedsUpsert(freshRSSFeeds, account);

        if (!insertedFeedsIds.isEmpty()) {
            setFeedsColors(database.feedDao().selectFromIdList(insertedFeedsIds));
        }

    }

    private void insertFolders(List<Folder> freshRSSFolders) {
        freshRSSFolders.stream().forEach(folder -> folder.setAccountId(account.getId()));

        database.folderDao().foldersUpsert(freshRSSFolders, account);
    }

    private void insertItems(List<Item> items) {
        List<Item> itemsToInsert = new ArrayList<>();
        Map<String, Integer> itemsFeedsIds = new HashMap<>();

        for (Item item : items) {
            Integer feedId;
            if (itemsFeedsIds.containsKey(item.getFeedRemoteId())) {
                feedId = itemsFeedsIds.get(item.getFeedRemoteId());
            } else {
                feedId = database.feedDao().getFeedIdByRemoteId(item.getFeedRemoteId(), account.getId());
                itemsFeedsIds.put(item.getFeedRemoteId(), feedId);
            }

            item.setFeedId(feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));
            itemsToInsert.add(item);
        }

        if (!itemsToInsert.isEmpty()) {
            Collections.sort(itemsToInsert, Item::compareTo);
            database.itemDao().insert(itemsToInsert);
        }
    }

    private void insertStarredItems(List<Item> items) {
        List<StarredItem> starredItems = items.stream().map(StarredItem::new).collect(Collectors.toList());

        List<StarredItem> itemsToInsert = new ArrayList<>();
        Map<String, Integer> itemsFeedsIds = new HashMap<>();

        for (StarredItem item : starredItems) {
            int feedId;

            if (itemsFeedsIds.containsKey(item.getFeedRemoteId())) {
                feedId = itemsFeedsIds.get(item.getFeedRemoteId());
            } else {
                feedId = database.feedDao().getFeedIdByRemoteId(item.getFeedRemoteId(), account.getId());
                itemsFeedsIds.put(item.getFeedRemoteId(), feedId);
            }

            item.setFeedId(feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));
            itemsToInsert.add(item);
        }

        if (!itemsToInsert.isEmpty()) {
            Collections.sort(itemsToInsert, Item::compareTo);

            database.starredItemDao().deleteStarredItems(account.getId());
            database.starredItemDao().insert(itemsToInsert);
        }
    }

    private void insertItemsIds(List<String> unreadIds, List<String> starredIds) {
        /*database.itemsIdsDao().deleteUnreadItemsIds(account.getId());
        database.itemsIdsDao().insertUnreadItemsIds(unreadIds.stream().map(id ->
                new UnreadItemsIds(0, id, account.getId())).collect(Collectors.toList()));

        database.itemDao().updateUnreadState(account.getId());
        database.itemDao().updateReadState(account.getId());*/

        database.itemsIdsDao().deleteItemsIds(account.getId());
        database.itemsIdsDao().insertItemsIds(unreadIds.stream().map(id ->
                new ItemStateId(0, true, starredIds.stream()
                        .anyMatch(starredId -> starredId.equals(id)), id, account.getId()))
                .collect(Collectors.toList()));

    }
}
