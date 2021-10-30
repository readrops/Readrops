package com.readrops.app.repositories

import com.readrops.db.entities.Feed

interface FeedUpdate {

    fun onNext(feed: Feed)

}