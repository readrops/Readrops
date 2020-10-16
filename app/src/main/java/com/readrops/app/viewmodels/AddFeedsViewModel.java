package com.readrops.app.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.readrops.api.localfeed.LocalRSSDataSource;
import com.readrops.app.repositories.ARepository;
import com.readrops.app.utils.FeedInsertionResult;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.db.Database;
import com.readrops.db.entities.account.Account;

import org.koin.core.parameter.DefinitionParametersKt;
import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

public class AddFeedsViewModel extends ViewModel {

    private final Database database;
    private final LocalRSSDataSource localRSSDataSource;

    public AddFeedsViewModel(@NonNull Database database, @NonNull LocalRSSDataSource localRSSDataSource) {
        this.database = database;
        this.localRSSDataSource = localRSSDataSource;
    }

    public Single<List<FeedInsertionResult>> addFeeds(List<ParsingResult> results, Account account) {
        ARepository repository = KoinJavaComponent.get(ARepository.class, null,
                () -> DefinitionParametersKt.parametersOf(account));

        return repository.addFeeds(results);
    }

    public Single<List<ParsingResult>> parseUrl(String url) {
        return Single.create(emitter -> {
            List<ParsingResult> results = new ArrayList<>();

            if (localRSSDataSource.isUrlRSSResource(url)) {
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
