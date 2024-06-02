package com.readrops.app.compose

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.readrops.api.services.Credentials
import com.readrops.app.compose.account.AccountScreenModel
import com.readrops.app.compose.account.credentials.AccountCredentialsScreenModel
import com.readrops.app.compose.account.credentials.AccountCredentialsScreenMode
import com.readrops.app.compose.account.selection.AccountSelectionScreenModel
import com.readrops.app.compose.feeds.FeedScreenModel
import com.readrops.app.compose.item.ItemScreenModel
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.repositories.FreshRSSRepository
import com.readrops.app.compose.repositories.GetFoldersWithFeeds
import com.readrops.app.compose.repositories.LocalRSSRepository
import com.readrops.app.compose.timelime.TimelineScreenModel
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val composeAppModule = module {

    factory { TimelineScreenModel(get(), get()) }

    factory { FeedScreenModel(get(), get(), get(), androidContext()) }

    factory { AccountSelectionScreenModel(get()) }

    factory { AccountScreenModel(get()) }

    factory { (itemId: Int) -> ItemScreenModel(get(), itemId) }

    factory { (accountType: Account, mode: AccountCredentialsScreenMode) ->
        AccountCredentialsScreenModel(accountType, mode, get())
    }

    single { GetFoldersWithFeeds(get()) }

    // repositories

    factory<BaseRepository> { (account: Account) ->
        when (account.accountType) {
            AccountType.LOCAL -> LocalRSSRepository(get(), get(), account)
            AccountType.FRESHRSS -> FreshRSSRepository(
                get(), account,
                get(parameters = { parametersOf(Credentials.toCredentials(account)) })
            )
            else -> throw IllegalArgumentException("Unknown account type")
        }
    }

    single {
        val masterKey = MasterKey.Builder(androidContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            androidContext(),
            "account_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}