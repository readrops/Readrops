package com.readrops.app;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import com.readrops.app.database.entities.Item;
import com.readrops.readropslibrary.ParsingResult;

public class BasedRepository extends ARepository {

    protected BasedRepository(Application application) {
        super(application);
    }

    public LiveData<Item> getItemById(int id) {
        return database.itemDao().getItemById(id);
    }


    @Override
    public void sync() {

    }

    @Override
    public void addFeed(ParsingResult result) {

    }

    @Override
    public void deleteFeed(Item item) {

    }

    @Override
    public void moveFeed(Item item) {

    }


}
