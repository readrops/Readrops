package com.readrops.app.feeds

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedItem(
    feed: Feed,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    displayDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(
                if (displayDivider) {
                    Modifier.padding(start = MaterialTheme.spacing.mediumSpacing)
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        if (displayDivider) {
            VerticalDivider()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(
                    top = MaterialTheme.spacing.shortSpacing,
                    bottom = MaterialTheme.spacing.shortSpacing,
                    end = MaterialTheme.spacing.mediumSpacing
                )
        ) {
            MediumSpacer()

            FeedIcon(
                iconUrl = feed.iconUrl,
                name = feed.name!!,
                size = 16.dp
            )

            ShortSpacer()

            Text(
                text = feed.name!!,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
