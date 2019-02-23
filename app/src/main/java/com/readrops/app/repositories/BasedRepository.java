package com.readrops.app.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.readrops.app.database.pojo.ItemWithFeed;
import com.readrops.app.database.entities.Feed;
import com.readrops.readropslibrary.ParsingResult;

public class BasedRepository extends ARepository {

    protected BasedRepository(Application application) {
        super(application);
    }

    public LiveData<ItemWithFeed> getItemById(int id) {
        return database.itemDao().getItemById(id);
    }


    @Override
    public void sync() {

    }

    @Override
    public void addFeed(ParsingResult result) {

    }

    @Override
    public void deleteFeed(Feed feed) {

    }
}
