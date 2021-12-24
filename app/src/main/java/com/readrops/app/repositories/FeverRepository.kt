package com.readrops.app.repositories

import android.content.Context
import com.readrops.api.services.fever.FeverDataSource
import com.readrops.app.addfeed.FeedInsertionResult
import com.readrops.app.addfeed.ParsingResult
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable

class FeverRepository(
        val feverDataSource: FeverDataSource,
        database: Database,
        context: Context,
        account: Account?,
) :
        ARepository(database, context, account) {

    override fun login(account: Account, insert: Boolean): Completable {
        return rxCompletable {
            feverDataSource.login(account.login!!, account.password!!)
        }
    }

    override fun sync(feeds: MutableList<Feed>?, update: FeedUpdate?): Completable {
        return rxCompletable {

        }
    }

    override fun addFeeds(results: MutableList<ParsingResult>?): Single<MutableList<FeedInsertionResult>> {
        TODO("Not yet implemented")
    }
}