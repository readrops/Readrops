package com.readrops.app.repositories;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.api.services.Credentials;
import com.readrops.api.services.SyncResult;
import com.readrops.api.utils.AuthInterceptor;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.feedscolors.FeedColorsKt;
import com.readrops.app.utils.feedscolors.FeedsColorsIntentService;
import com.readrops.db.Database;
import com.readrops.db.entities.Feed;
import com.readrops.db.entities.Folder;
import com.readrops.db.entities.Item;
import com.readrops.db.entities.account.Account;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.readrops.app.utils.ReadropsKeys.FEEDS;

public abstract class ARepository {

    protected Context context;
    protected Database database;
    protected Account account;

    protected SyncResult syncResult;

    protected ARepository(Database database, @NonNull Context context, @Nullable Account account) {
        this.context = context;
        this.database = database;
        this.account = account;

        setCredentials(account);
    }

    protected void setCredentials(@Nullable Account account) {
        KoinJavaComponent.get(AuthInterceptor.class)
                .setCredentials(account != null && !account.isLocal() ? Credentials.toCredentials(account) : null);
    }

    // TODO : replace Single by Completable
    public abstract Single<Boolean> login(Account account, boolean insert);

    public abstract Observable<Feed> sync(List<Feed> feeds);

    public abstract Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results);

    public Completable insertOPMLFoldersAndFeeds(Map<Folder, List<Feed>> foldersAndFeeds) {
        List<Completable> completableList = new ArrayList<>();

        for (Map.Entry<Folder, List<Feed>> entry : foldersAndFeeds.entrySet()) {
            Folder folder = entry.getKey();
            folder.setAccountId(account.getId());

            Completable completable = Single.<Integer>create(emitter -> {
                Folder dbFolder = database.folderDao().getFolderByName(folder.getName(), account.getId());

                if (dbFolder != null)
                    emitter.onSuccess(dbFolder.getId());
                else
                    emitter.onSuccess((int) database.folderDao().compatInsert(folder));
            }).flatMap(folderId -> {
                List<Feed> feeds = entry.getValue();
                for (Feed feed : feeds) {
                    feed.setFolderId(folderId);
                }

                List<ParsingResult> parsingResults = ParsingResult.toParsingResults(feeds);
                return addFeeds(parsingResults);
            }).flatMapCompletable(feedInsertionResults -> Completable.complete());

            completableList.add(completable);
        }

        return Completable.concat(completableList);
    }

    public Completable updateFeed(Feed feed) {
        return Completable.create(emitter -> {
            database.feedDao().updateFeedFields(feed.getId(), feed.getName(), feed.getUrl(), feed.getFolderId());
            emitter.onComplete();
        });
    }

    public Completable deleteFeed(Feed feed) {
        return database.feedDao().delete(feed);
    }

    public Single<Long> addFolder(Folder folder) {
        return database.folderDao().insert(folder);
    }

    public Completable updateFolder(Folder folder) {
        return database.folderDao().update(folder);
    }

    public Completable deleteFolder(Folder folder) {
        return database.folderDao().delete(folder);
    }

    public Completable setItemReadState(Item item, boolean read) {
        return setItemReadState(item.getId(), read, !item.isReadChanged());
    }

    public Completable setItemReadState(int itemId, boolean read, boolean readChanged) {
        return database.itemDao().setReadState(itemId, read, readChanged);
    }

    public Completable setAllItemsReadState(boolean read) {
        return database.itemDao().setAllItemsReadState(read ? 1 : 0, account.getId());
    }

    public Completable setAllFeedItemsReadState(int feedId, boolean read) {
        return database.itemDao().setAllFeedItemsReadState(feedId, read ? 1 : 0);
    }

    public Single<Integer> getFeedCount(int accountId) {
        return database.feedDao().getFeedCount(accountId);
    }

    public Single<Map<Folder, List<Feed>>> getFoldersWithFeeds() {
        return Single.create(emitter -> {
            List<Folder> folders = database.folderDao().getFolders(account.getId());
            Map<Folder, List<Feed>> foldersWithFeeds = new TreeMap<>(Comparator.nullsLast(Folder::compareTo));

            for (Folder folder : folders) {
                List<Feed> feeds = database.feedDao().getFeedsByFolder(folder.getId());

                for (Feed feed : feeds) {
                    int unreadCount = database.itemDao().getUnreadCount(feed.getId());
                    feed.setUnreadCount(unreadCount);
                }

                foldersWithFeeds.put(folder, feeds);
            }

            // feeds without folder
            List<Feed> feedsWithoutFolder = database.feedDao().getFeedsWithoutFolder(account.getId());
            for (Feed feed : feedsWithoutFolder) {
                feed.setUnreadCount(database.itemDao().getUnreadCount(feed.getId()));
            }

            foldersWithFeeds.put(null, feedsWithoutFolder);

            emitter.onSuccess(foldersWithFeeds);
        });
    }

    protected void setFeedColors(Feed feed) {
        FeedColorsKt.setFeedColors(feed);
        database.feedDao().updateColors(feed.getId(),
                feed.getTextColor(), feed.getBackgroundColor());
    }

    protected void setFeedsColors(List<Feed> feeds) {
        Intent intent = new Intent(context, FeedsColorsIntentService.class);
        intent.putParcelableArrayListExtra(FEEDS, new ArrayList<>(feeds));

        context.startService(intent);
    }

    public SyncResult getSyncResult() {
        return syncResult;
    }
}
