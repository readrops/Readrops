package com.readrops.app.compose.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncResult
import com.readrops.api.services.freshrss.NewFreshRSSDataSource
import com.readrops.api.utils.AuthInterceptor
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import org.koin.core.component.KoinComponent

class FreshRSSRepository(
    database: Database,
    account: Account,
    private val dataSource: NewFreshRSSDataSource,
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        val authInterceptor = getKoin().get<AuthInterceptor>()
        authInterceptor.credentials = Credentials.toCredentials(account)

        val authToken = dataSource.login(account.login!!, account.password!!)
        account.token = authToken
        // we got the authToken, time to provide it to make real calls
        authInterceptor.credentials = Credentials.toCredentials(account)

        val userInfo = dataSource.getUserInfo()
        account.displayedName = userInfo.userName
    }

    override suspend fun synchronize(
        selectedFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): Pair<SyncResult, ErrorResult> {
        TODO("Not yet implemented")
    }

    override suspend fun synchronize(): SyncResult {
        TODO("Not yet implemented")
    }

    override suspend fun insertNewFeeds(
        newFeeds: List<Feed>,
        onUpdate: (Feed) -> Unit
    ): ErrorResult {
        TODO("Not yet implemented")
    }
}