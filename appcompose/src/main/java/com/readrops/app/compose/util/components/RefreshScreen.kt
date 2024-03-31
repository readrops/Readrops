package com.readrops.app.compose.util.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.readrops.app.compose.util.theme.VeryShortSpacer

@Composable
fun RefreshScreen(
    currentFeed: String,
    feedCount: Int,
    feedMax: Int
) {
    CenteredColumn {
        RefreshIndicator(
            currentFeed = currentFeed,
            feedCount = feedCount,
            feedMax = feedMax
        )
    }
}

@Composable
fun RefreshIndicator(
    currentFeed: String,
    feedCount: Int,
    feedMax: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LinearProgressIndicator(
            progress = { feedCount.toFloat() / feedMax.toFloat() }
        )

        VeryShortSpacer()

        Text(
            text = "$currentFeed ($feedCount/$feedMax)",
            maxLines = 1
        )
    }
}