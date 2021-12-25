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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable

class FeverRepository(
    private val feverDataSource: FeverDataSource,
    private val dispatcher: CoroutineDispatcher,
    database: Database,
    context: Context,
    account: Account?,
) : ARepository(database, context, account) {

    override fun login(account: Account, insert: Boolean): Completable {
        return rxCompletable(context = dispatcher) {
            try {
                feverDataSource.login(account.login!!, account.password!!)
                account.displayedName = account.accountType!!.name

                database.accountDao().insert(account)
                    .doOnSuccess { account.id = it.toInt() }
                    .await()
            } catch (e: Exception) {
                error(e.message!!)
            }
        }
    }

    override fun sync(feeds: List<Feed>?, update: FeedUpdate?): Completable {
        return rxCompletable(context = dispatcher) {

        }
    }

    override fun addFeeds(results: List<ParsingResult>?): Single<List<FeedInsertionResult>> {
        TODO("Not yet implemented")
    }
}