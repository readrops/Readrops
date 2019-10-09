package com.readrops.app.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.database.entities.Feed;
import com.readrops.app.database.entities.Folder;
import com.readrops.app.database.entities.Item;
import com.readrops.app.database.entities.account.Account;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.FeedMatcher;
import com.readrops.app.utils.ItemMatcher;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.Utils;
import com.readrops.readropslibrary.services.SyncType;
import com.readrops.readropslibrary.services.freshrss.FreshRSSAPI;
import com.readrops.readropslibrary.services.freshrss.FreshRSSCredentials;
import com.readrops.readropslibrary.services.freshrss.FreshRSSSyncData;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFeed;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSFolder;
import com.readrops.readropslibrary.services.freshrss.json.FreshRSSItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class FreshRSSRepository extends ARepository<FreshRSSAPI> {

    private static final String TAG = FreshRSSRepository.class.getSimpleName();
    
    public FreshRSSRepository(@NonNull Application application, @Nullable Account account) {
        super(application, account);

        if (account != null)
            api = new FreshRSSAPI(account.toCredentials());
    }

    @Override
    public Single<Boolean> login(Account account, boolean insert) {
        if (api == null)
            api = new FreshRSSAPI(account.toCredentials());
        else
            api.setCredentials(account.toCredentials());

        return api.login(account.getLogin(), account.getPassword())
                .flatMap(token -> {
                    account.setToken(token);
                    api.setCredentials(new FreshRSSCredentials(token, account.getUrl()));

                    return api.getWriteToken();
                })
                .flatMap(writeToken -> {
                    account.setWriteToken(writeToken);

                    return api.getUserInfo();
                })
                .flatMap(userInfo -> {
                    account.setDisplayedName(userInfo.getUserName());

                    if (insert)
                        account.setId((int) database.accountDao().insert(account));

                    return Single.just(true);
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

        return Single.<FreshRSSSyncData>create(emitter -> {
            syncData.setReadItemsIds(database.itemDao().getReadChanges(account.getId()));
            syncData.setUnreadItemsIds(database.itemDao().getUnreadChanges(account.getId()));

            emitter.onSuccess(syncData);
        }).flatMap(syncData1 -> api.sync(syncType, syncData1, account.getWriteToken()))
                .flatMapObservable(syncResult -> {
                    insertFolders(syncResult.getFolders(), account);
                    insertFeeds(syncResult.getFeeds(), account);
                    insertItems(syncResult.getItems(), account, syncType == SyncType.INITIAL_SYNC);

                    account.setLastModified(syncResult.getLastUpdated());
                    database.accountDao().updateLastModified(account.getId(), syncResult.getLastUpdated());

                    database.itemDao().resetReadChanges(account.getId());

                    return Observable.empty();
                });
    }

    @Override
    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results) {
        List<Completable> completableList = new ArrayList<>();
        List<FeedInsertionResult> insertionResults = new ArrayList<>();

        for (ParsingResult result : results) {
            completableList.add(api.createFeed(account.getWriteToken(), result.getUrl())
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

        }).flatMapCompletable(folder -> api.updateFeed(account.getWriteToken(),
                feed.getUrl(), feed.getName(), folder == null ? null : folder.getRemoteId())
                .andThen(super.updateFeed(feed)));
    }

    @Override
    public Completable deleteFeed(Feed feed) {
        return api.deleteFeed(account.getWriteToken(), feed.getUrl())
                .andThen(super.deleteFeed(feed));
    }

    @Override
    public Completable addFolder(Folder folder) {
        return api.createFolder(account.getWriteToken(), folder.getName());
    }

    @Override
    public Completable updateFolder(Folder folder) {
        return api.updateFolder(account.getWriteToken(), folder.getRemoteId(), folder.getName())
                .andThen(Completable.create(emitter -> {
                    folder.setRemoteId("user/-/label/" + folder.getName());
                    emitter.onComplete();
                }))
                .andThen(super.updateFolder(folder));
    }

    @Override
    public Completable deleteFolder(Folder folder) {
        return api.deleteFolder(account.getWriteToken(), folder.getRemoteId())
                .andThen(super.deleteFolder(folder));
    }

    private List<Feed> insertFeeds(List<FreshRSSFeed> freshRSSFeeds, Account account) {
        List<Feed> feeds = new ArrayList<>();

        for (FreshRSSFeed freshRSSFeed : freshRSSFeeds) {
            feeds.add(FeedMatcher.freshRSSFeedToFeed(freshRSSFeed, account));
        }

        List<Long> insertedFeedsIds = database.feedDao().feedsUpsert(feeds, account);

        List<Feed> insertedFeeds = new ArrayList<>();
        if (!insertedFeedsIds.isEmpty()) {
            insertedFeeds.addAll(database.feedDao().selectFromIdList(insertedFeedsIds));
            setFaviconUtils(insertedFeeds);
        }

        return insertedFeeds;
    }

    private void insertFolders(List<FreshRSSFolder> freshRSSFolders, Account account) {
        List<Folder> folders = new ArrayList<>();

        for (FreshRSSFolder freshRSSFolder : freshRSSFolders) {
            if (freshRSSFolder.getType() != null && freshRSSFolder.getType().equals("folder")) {
                List<Object> tokens = Collections.list(new StringTokenizer(freshRSSFolder.getId(), "/"));

                Folder folder = new Folder((String) tokens.get(tokens.size() - 1));
                folder.setRemoteId(freshRSSFolder.getId());
                folder.setAccountId(account.getId());

                folders.add(folder);
            }
        }

        database.folderDao().foldersUpsert(folders, account);
    }

    private void insertItems(List<FreshRSSItem> items, Account account, boolean initialSync) {
        List<Item> newItems = new ArrayList<>();

        for (FreshRSSItem freshRSSItem : items) {
            int feedId = database.feedDao().getFeedIdByRemoteId(String.valueOf(freshRSSItem.getOrigin().getStreamId()), account.getId());

            if (!initialSync && feedId > 0) {
                if (database.itemDao().remoteItemExists(freshRSSItem.getId(), feedId))
                    break;
            }

            Item item = ItemMatcher.freshRSSItemtoItem(freshRSSItem, feedId);
            item.setReadTime(Utils.readTimeFromString(item.getContent()));

            newItems.add(item);
        }

        Collections.sort(newItems, Item::compareTo);
        database.itemDao().insert(newItems);
    }
}
