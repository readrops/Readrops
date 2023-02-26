package com.readrops.app.compose.timelime

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen


class TimelineScreen : AndroidScreen() {

    @Composable
    override fun Content() {
        Column {
            TimelineItem()
        }
    }


}