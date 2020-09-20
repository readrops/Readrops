package com.readrops.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.readrops.api.localfeed.LocalRSSDataSource;
import com.readrops.api.utils.HttpManager;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.db.Database;
import com.readrops.db.entities.account.Account;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class AddFeedsViewModel extends AndroidViewModel {

    private static final String TAG = AddFeedsViewModel.class.getSimpleName();

    private ARepository repository;
    private Database database;

    public AddFeedsViewModel(@NonNull Application application) {
        super(application);

        database = Database.getInstance(application);
    }

    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        try {
            repository = ARepository.repositoryFactory(account, getApplication());

            return repository.addFeeds(results);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return null;
    }

    public Single<List<ParsingResult>> parseUrl(String url) {
        return Single.create(emitter -> {
            LocalRSSDataSource dataSource = new LocalRSSDataSource(HttpManager.getInstance().getOkHttpClient());
            List<ParsingResult> results = new ArrayList<>();

            if (dataSource.isUrlRSSResource(url)) {
                ParsingResult parsingResult = new ParsingResult(url, null);
                results.add(parsingResult);
            } else {
                results.addAll(HtmlParser.getFeedLink(url));
            }

            emitter.onSuccess(results);
        });
    }

    public LiveData<List<Account>> getAccounts() {
        return database.accountDao().selectAllAsync();
    }
}
