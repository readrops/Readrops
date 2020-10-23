package com.readrops.app

import androidx.preference.PreferenceManager
import com.readrops.app.account.AccountViewModel
import com.readrops.app.addfeed.AddFeedsViewModel
import com.readrops.app.feedsfolders.ManageFeedsFoldersViewModel
import com.readrops.app.item.ItemViewModel
import com.readrops.app.itemslist.MainViewModel
import com.readrops.app.notifications.NotificationPermissionViewModel
import com.readrops.app.repositories.FreshRSSRepository
import com.readrops.app.repositories.LocalFeedRepository
import com.readrops.app.repositories.NextNewsRepository
import com.readrops.app.utils.GlideApp
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory { (account: Account) ->
        when (account.accountType) {
            AccountType.LOCAL -> LocalFeedRepository(get(), get(), androidContext(), account)
            AccountType.NEXTCLOUD_NEWS -> NextNewsRepository(get(), get(), androidContext(), account)
            AccountType.FRESHRSS -> FreshRSSRepository(get(), get(), androidContext(), account)
            else -> throw IllegalArgumentException("Account type not supported")
        }
    }

    viewModel {
        MainViewModel(get())
    }

    viewModel {
        AddFeedsViewModel(get(), get())
    }

    viewModel {
        ItemViewModel(get())
    }

    viewModel {
        ManageFeedsFoldersViewModel(get())
    }

    viewModel {
        NotificationPermissionViewModel(get())
    }

    viewModel {
        AccountViewModel(get())
    }

    single { GlideApp.with(androidApplication()) }

    single { PreferenceManager.getDefaultSharedPreferences(androidContext()) }
}