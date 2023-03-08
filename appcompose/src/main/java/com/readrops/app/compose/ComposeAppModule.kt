package com.readrops.app.compose

import com.readrops.app.compose.timelime.TimelineViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val composeAppModule = module {

    viewModel { TimelineViewModel(get()) }
}