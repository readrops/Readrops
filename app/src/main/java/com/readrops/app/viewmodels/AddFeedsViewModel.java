package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.utils.HtmlParser;
import com.readrops.app.utils.ParsingResult;
import com.readrops.readropslibrary.localfeed.RSSNetwork;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;

public class AddFeedsViewModel extends AndroidViewModel {

    private LocalFeedRepository repository;

    public AddFeedsViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);
    }

    public Completable addFeeds(List<ParsingResult> results) {
        return repository.addFeeds(results);
    }

    public Single<List<ParsingResult>> parseUrl(String url) {
        return Single.create(emitter -> {
            RSSNetwork rssApi = new RSSNetwork();
            List<ParsingResult> results = new ArrayList<>();

            if (rssApi.isUrlFeedLink(url)) {
                ParsingResult parsingResult = new ParsingResult(url, null);
                results.add(parsingResult);

            } else
                results.addAll(HtmlParser.getFeedLink(url));

            emitter.onSuccess(results);
        });
    }
}
