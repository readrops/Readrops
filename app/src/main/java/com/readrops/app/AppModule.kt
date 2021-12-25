package com.readrops.app

import androidx.preference.PreferenceManager
import com.chimerapps.niddler.core.AndroidNiddler
import com.chimerapps.niddler.core.Niddler
import com.readrops.api.services.Credentials
import com.readrops.app.account.AccountViewModel
import com.readrops.app.addfeed.AddFeedsViewModel
import com.readrops.app.feedsfolders.ManageFeedsFoldersViewModel
import com.readrops.app.item.ItemViewModel
import com.readrops.app.itemslist.MainViewModel
import com.readrops.app.notifications.NotificationPermissionViewModel
import com.readrops.app.repositories.FeverRepository
import com.readrops.app.repositories.FreshRSSRepository
import com.readrops.app.repositories.LocalFeedRepository
import com.readrops.app.repositories.NextNewsRepository
import com.readrops.app.utils.GlideApp
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {

    factory { (account: Account) ->
        when (account.accountType) {
            AccountType.LOCAL -> LocalFeedRepository(get(), get(), androidContext(), account)
            AccountType.NEXTCLOUD_NEWS -> NextNewsRepository(get(parameters = { parametersOf(Credentials.toCredentials(account)) }),
                    get(), androidContext(), account)
            AccountType.FRESHRSS -> FreshRSSRepository(get(parameters = { parametersOf(Credentials.toCredentials(account)) }),
                    get(), androidContext(), account)
            AccountType.FEVER -> FeverRepository(get(parameters = { parametersOf(Credentials.toCredentials(account)) }),
                Dispatchers.IO, get(), get(), account)
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

    single<Niddler> {
        val niddler = AndroidNiddler.Builder()
                .setNiddlerInformation(AndroidNiddler.fromApplication(get()))
                .setPort(0)
                .setMaxStackTraceSize(10)
                .build()

        niddler.attachToApplication(get())

        niddler.apply { start() }
    }
}