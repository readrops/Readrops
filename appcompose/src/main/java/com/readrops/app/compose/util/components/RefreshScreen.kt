package com.readrops.app.compose.util.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.readrops.app.compose.util.theme.VeryShortSpacer

@Composable
fun RefreshScreen(
    currentFeed: String,
    feedCount: Int,
    feedMax: Int
) {
    CenteredColumn {
        LinearProgressIndicator(
            progress = { feedCount.toFloat() / feedMax.toFloat() }
        )

        VeryShortSpacer()

        Text(
            text = "$currentFeed ($feedCount/$feedMax)"
        )
    }
}