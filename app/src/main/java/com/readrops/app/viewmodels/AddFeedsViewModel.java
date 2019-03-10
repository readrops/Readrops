package com.readrops.app.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.readrops.app.repositories.LocalFeedRepository;
import com.readrops.app.utils.ParsingResult;

import java.util.List;

import io.reactivex.Completable;

public class AddFeedsViewModel extends AndroidViewModel {

    private LocalFeedRepository repository;

    public AddFeedsViewModel(@NonNull Application application) {
        super(application);

        repository = new LocalFeedRepository(application);
    }

    public Completable addFeeds(List<ParsingResult> results) {
        return repository.addFeeds(results);
    }
}
