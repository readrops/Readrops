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
import com.readrops.db.entities.ItemState;
import com.readrops.db.entities.account.Account;
import com.readrops.db.pojo.ItemReadStarState;

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
            List<ItemReadStarState> itemStateChanges = database
                    .itemStateChangesDao()
                    .getItemStateChanges(account.getId());

            syncData.setReadItemsIds(itemStateChanges.stream()
                    .filter(it -> it.getReadChange() && it.getRead())
                    .map(ItemReadStarState::getRemoteId)
                    .collect(Collectors.toList()));

            syncData.setUnreadItemsIds(itemStateChanges.stream()
                    .filter(it -> it.getReadChange() && !it.getRead())
                    .map(ItemReadStarState::getRemoteId)
                    .collect(Collectors.toList()));

            syncData.setStarredItemsIds(itemStateChanges.stream()
                    .filter(it -> it.getStarChange() && it.getStarred())
                    .map(ItemReadStarState::getRemoteId)
                    .collect(Collectors.toList()));

            syncData.setUnstarredItemsIds(itemStateChanges.stream()
                    .filter(it -> it.getStarChange() && !it.getStarred())
                    .map(ItemReadStarState::getRemoteId)
                    .collect(Collectors.toList()));

            emitter.onSuccess(syncData);
        }).flatMap(syncData1 -> dataSource.sync(syncType, syncData1, account.getWriteToken()))
                .flatMapObservable(syncResult -> {
                    logger.addSplit("server queries");

                    insertFolders(syncResult.getFolders());
                    logger.addSplit("folders insertion");
                    insertFeeds(syncResult.getFeeds());
                    logger.addSplit("feeds insertion");

                    insertItems(syncResult.getItems(), false);
                    logger.addSplit("items insertion");

                    insertItems(syncResult.getStarredItems(), true);
                    logger.addSplit("starred items insertion");

                    insertItemsIds(syncResult.getUnreadIds(), syncResult.getStarredIds());
                    logger.addSplit("insert and update items ids");

                    account.setLastModified(newLastModified);
                    database.accountDao().updateLastModified(account.getId(), newLastModified);

                    database.itemStateChangesDao().resetStateChanges(account.getId());

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

    private void insertItems(List<Item> items, boolean starredItems) {
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

            // workaround to avoid inserting starred items coming from the main item call
            // as the API exclusion filter doesn't seem to work
            if (!starredItems) {
                if (!item.isStarred()) {
                    itemsToInsert.add(item);
                }
            } else {
                itemsToInsert.add(item);
            }
        }

        if (!itemsToInsert.isEmpty()) {
            Collections.sort(itemsToInsert, Item::compareTo);
            database.itemDao().insert(itemsToInsert);
        }
    }

    private void insertItemsIds(List<String> unreadIds, List<String> starredIds) {
        database.itemStateDao().deleteItemsStates(account.getId());

        database.itemStateDao().insertItemStates(unreadIds.stream().map(id -> {
                    boolean starred = starredIds.stream().filter(starredId -> starredId.equals(id)).count() == 1;
                    if (starred) {
                        starredIds.remove(id);
                    }

                    return new ItemState(0, false, starred, id, account.getId());
                }
        ).collect(Collectors.toList()));

        // insert starred items ids which are read
        if (!starredIds.isEmpty()) {
            database.itemStateDao().insertItemStates(starredIds.stream().map(id ->
                    new ItemState(0, true, true, id, account.getId()))
                    .collect(Collectors.toList()));
        }
    }
}
