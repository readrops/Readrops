package com.readrops.app.compose

import com.readrops.app.compose.feeds.FeedsViewModel
import com.readrops.app.compose.repositories.BaseRepository
import com.readrops.app.compose.repositories.LocalRSSRepository
import com.readrops.app.compose.timelime.TimelineViewModel
import com.readrops.db.entities.account.Account
import com.readrops.db.entities.account.AccountType
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val composeAppModule = module {

    viewModel { TimelineViewModel(get()) }

    viewModel { FeedsViewModel(get()) }

    // repositories

    single<BaseRepository> { LocalRSSRepository(get(), get(), Account(id = 1, isCurrentAccount = true, accountType = AccountType.LOCAL)) }
}