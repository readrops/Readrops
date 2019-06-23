package com.readrops.app.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.readrops.app.database.Database;
import com.readrops.app.database.entities.Account;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.repositories.NextNewsRepository;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.localfeed.RSSQuery;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class AddFeedsViewModel extends AndroidViewModel {

    private ARepository repository;
    private Database database;

    public AddFeedsViewModel(@NonNull Application application) {
        super(application);

        database = Database.getInstance(application);
    }

    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        switch (account.getAccountType()) {
            case LOCAL:
                repository = new LocalFeedRepository(getApplication());
                break;
            case NEXTCLOUD_NEWS:
                repository = new NextNewsRepository(getApplication());
                break;
        }

        return repository.addFeeds(results, account);
    }

    public Single<List<ParsingResult>> parseUrl(String url) {
        return Single.create(emitter -> {
            RSSQuery rssApi = new RSSQuery();
            List<ParsingResult> results = new ArrayList<>();

            if (rssApi.isUrlFeedLink(url)) {
                ParsingResult parsingResult = new ParsingResult(url, null);
                results.add(parsingResult);

            } else
                results.addAll(HtmlParser.getFeedLink(url));

            emitter.onSuccess(results);
        });
    }

    public LiveData<List<Account>> getAccounts() {
        return database.accountDao().selectAll();
    }
}
