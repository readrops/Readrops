package com.readrops.app.compose.repositories

import com.readrops.api.services.Credentials
import com.readrops.api.services.SyncResult
import com.readrops.api.services.nextcloudnews.NewNextcloudNewsDataSource
import com.readrops.api.utils.AuthInterceptor
import com.readrops.db.Database
import com.readrops.db.entities.Feed
import com.readrops.db.entities.account.Account
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class NextcloudNewsRepository(
    database: Database,
    account: Account,
    private val dataSource: NewNextcloudNewsDataSource
) : BaseRepository(database, account), KoinComponent {

    override suspend fun login(account: Account) {
        val authInterceptor = get<AuthInterceptor>()
        authInterceptor.credentials = Credentials.toCredentials(account)

        val displayName = dataSource.login(get(), account)
        account.displayedName = displayName
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