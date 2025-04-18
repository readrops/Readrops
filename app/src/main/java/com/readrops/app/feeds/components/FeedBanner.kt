package com.readrops.app.feeds.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.AsyncImage
import com.readrops.app.R
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.VeryShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed

@Composable
fun FeedBanner(feed: Feed) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (feed.imageUrl != null) {
                AsyncImage(
                    model = feed.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                color = Color.Black.copy(alpha = 0.65f)
                            )
                        }
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(
                    top = MaterialTheme.spacing.largeSpacing,
                    start = MaterialTheme.spacing.largeSpacing,
                    end = MaterialTheme.spacing.largeSpacing,
                    bottom = MaterialTheme.spacing.mediumSpacing
                )
            ) {
                AsyncImage(
                    model = feed.iconUrl,
                    contentDescription = feed.name!!,
                    placeholder = painterResource(id = R.drawable.ic_rss_feed_grey),
                    error = painterResource(id = R.drawable.ic_rss_feed_grey),
                    modifier = Modifier.size(MaterialTheme.spacing.veryLargeSpacing)
                )

                MediumSpacer()

                Column {
                    Text(
                        text = feed.name!!,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (feed.imageUrl != null) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (feed.description != null) {
                        VeryShortSpacer()

                        Text(
                            text = feed.description!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (feed.imageUrl != null) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (feed.imageUrl == null) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
            )
        }
    }
}