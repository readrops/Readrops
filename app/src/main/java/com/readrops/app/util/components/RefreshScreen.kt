package com.readrops.app.util.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing

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
    currentFeed: String?,
    feedCount: Int,
    feedMax: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
    ) {
        LinearProgressIndicator(
            progress = { feedCount.toFloat() / feedMax.toFloat() }
        )

        VeryShortSpacer()

        Text(
            text = "${currentFeed.orEmpty()} ($feedCount/$feedMax)",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}