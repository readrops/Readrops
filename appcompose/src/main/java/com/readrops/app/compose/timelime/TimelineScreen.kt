package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen
import org.koin.androidx.compose.getViewModel


class TimelineScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        val viewModel = getViewModel<TimelineViewModel>()

        Column {
            TimelineItem()
        }
    }
}