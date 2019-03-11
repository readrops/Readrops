package com.readrops.readropslibrary;

import com.readrops.readropslibrary.localfeed.AFeed;
import com.readrops.readropslibrary.localfeed.RSSQuery;

public interface QueryCallback {

    void onSyncSuccess(AFeed feed, RSSQuery.RSSType type);

    void onSyncFailure(Exception e);

}
