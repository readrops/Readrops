package com.readrops.app.repositories;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.ParsingResult;
import com.readrops.app.utils.feedscolors.FeedColorsKt;
import com.readrops.app.utils.feedscolors.FeedsColorsIntentService;
import com.readrops.readropsdb.Database;
import com.readrops.readropsdb.entities.Feed;
import com.readrops.readropsdb.entities.Folder;
import com.readrops.readropsdb.entities.Item;
import com.readrops.readropsdb.entities.account.Account;
import com.readrops.readropsdb.entities.account.AccountType;
import com.readrops.readropslibrary.services.SyncResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.readrops.app.utils.ReadropsKeys.FEEDS;

public abstract class ARepository<T> {

    protected Context context;
    protected Database database;
    protected Account account;

    protected T api;

    protected SyncResult syncResult;

    protected ARepository(@NonNull Context context, @Nullable Account account) {
        this.context = context;
        this.database = Database.getInstance(context);
        this.account = account;

        api = createAPI();
    }

    protected abstract T createAPI();

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
            Map<Folder, List<Feed>> foldersWithFeeds = new TreeMap<>(Folder::compareTo);

            for (Folder folder : folders) {
                List<Feed> feeds = database.feedDao().getFeedsByFolder(folder.getId());

                for (Feed feed : feeds) {
                    int unreadCount = database.itemDao().getUnreadCount(feed.getId());
                    feed.setUnreadCount(unreadCount);
                }

                foldersWithFeeds.put(folder, feeds);
            }

            Folder noFolder = new Folder("no folder");

            List<Feed> feedsWithoutFolder = database.feedDao().getFeedsWithoutFolder(account.getId());
            for (Feed feed : feedsWithoutFolder) {
                feed.setUnreadCount(database.itemDao().getUnreadCount(feed.getId()));
            }

            foldersWithFeeds.put(noFolder, feedsWithoutFolder);

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

    public static ARepository repositoryFactory(Account account, AccountType accountType, Context context) throws Exception {
        switch (accountType) {
            case LOCAL:
                return new LocalFeedRepository(context, account);
            case NEXTCLOUD_NEWS:
                return new NextNewsRepository(context, account);
            case FRESHRSS:
                return new FreshRSSRepository(context, account);
            default:
                throw new Exception("account type not supported");
        }
    }

    public static ARepository repositoryFactory(Account account, Context context) throws Exception {
        return ARepository.repositoryFactory(account, account.getAccountType(), context);
    }

    public SyncResult getSyncResult() {
        return syncResult;
    }
}
