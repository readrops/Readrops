package com.readrops.app.timelime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.readrops.app.R
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.theme.ShortSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.pojo.ItemWithFeed
import com.readrops.db.util.DateUtils
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun RegularTimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    TimelineItemContainer(
        isRead = itemWithFeed.isRead,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
        ) {
            TimelineItemHeader(
                feedName = itemWithFeed.feedName,
                feedIconUrl = itemWithFeed.feedIconUrl,
                feedColor = itemWithFeed.color,
                folderName = itemWithFeed.folder?.name,
                date = itemWithFeed.item.pubDate!!,
                duration = itemWithFeed.item.readTime,
                isStarred = itemWithFeed.isStarred,
                onFavorite = onFavorite,
                onShare = onShare
            )

            ShortSpacer()

            TimelineItemTitle(title = itemWithFeed.item.title!!)

            ShortSpacer()

            TimelineItemBadge(
                date = itemWithFeed.item.pubDate!!,
                duration = itemWithFeed.item.readTime,
                color = itemWithFeed.color
            )
        }
    }
}

@Composable
fun CompactTimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.background

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // if some alpha is applied to the card, the swipe to dismiss background appears behind it
                // so we draw a rect with the current screen background color behind the card but in front of the dismiss background
                drawRect(containerColor)
            }
            .alpha(if (itemWithFeed.isRead) readAlpha else 1f)
    ) {
        Column(
            modifier = Modifier.padding(
                start = MaterialTheme.spacing.shortSpacing,
                end = MaterialTheme.spacing.shortSpacing,
                top = MaterialTheme.spacing.shortSpacing
            )
        ) {
            TimelineItemHeader(
                feedName = itemWithFeed.feedName,
                feedIconUrl = itemWithFeed.feedIconUrl,
                feedColor = itemWithFeed.color,
                folderName = itemWithFeed.folder?.name,
                onFavorite = onFavorite,
                onShare = onShare,
                date = itemWithFeed.item.pubDate!!,
                duration = itemWithFeed.item.readTime,
                isStarred = itemWithFeed.isStarred,
                displayActions = false
            )

            ShortSpacer()

            TimelineItemTitle(title = itemWithFeed.item.title!!)

            ShortSpacer()

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.shortSpacing)
            )
        }
    }
}

@Composable
fun LargeTimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (itemWithFeed.item.cleanDescription == null && !itemWithFeed.item.hasImage) {
        RegularTimelineItem(
            itemWithFeed = itemWithFeed,
            onClick = onClick,
            onFavorite = onFavorite,
            onShare = onShare,
            modifier = modifier
        )
    } else {
        TimelineItemContainer(
            isRead = itemWithFeed.isRead,
            onClick = onClick,
            modifier = modifier
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.mediumSpacing)
                ) {
                    TimelineItemHeader(
                        feedName = itemWithFeed.feedName,
                        feedIconUrl = itemWithFeed.feedIconUrl,
                        feedColor = itemWithFeed.color,
                        folderName = itemWithFeed.folder?.name,
                        date = itemWithFeed.item.pubDate!!,
                        duration = itemWithFeed.item.readTime,
                        isStarred = itemWithFeed.isStarred,
                        onFavorite = onFavorite,
                        onShare = onShare
                    )

                    ShortSpacer()

                    TimelineItemBadge(
                        date = itemWithFeed.item.pubDate!!,
                        duration = itemWithFeed.item.readTime,
                        color = itemWithFeed.color
                    )

                    ShortSpacer()

                    TimelineItemTitle(title = itemWithFeed.item.title!!)

                    if (itemWithFeed.item.cleanDescription != null) {
                        ShortSpacer()

                        Text(
                            text = itemWithFeed.item.cleanDescription!!,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                if (itemWithFeed.item.hasImage) {
                    AsyncImage(
                        model = if (!LocalInspectionMode.current) {
                            itemWithFeed.item.imageLink
                        } else {
                            ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.ic_broken_image)
                                .build()
                        },
                        contentDescription = itemWithFeed.item.title!!,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(16f / 9f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

}

@Composable
fun TimelineItemContainer(
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = MaterialTheme.spacing.shortSpacing),
    content: @Composable () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.background

    Card(
        onClick = onClick,
        modifier = modifier
            .padding(padding)
            .fillMaxWidth()
            .drawBehind {
                // if some alpha is applied to the card, the swipe to dismiss background appears behind it
                // so we draw a rect with the current screen background color behind the card but in front of the dismiss background
                drawRoundRect(
                    color = containerColor,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .alpha(if (isRead) readAlpha else 1f)
    ) {
        content()
    }
}

@Composable
fun TimelineItemHeader(
    feedName: String,
    feedIconUrl: String?,
    feedColor: Int,
    folderName: String?,
    date: LocalDateTime,
    duration: Double,
    isStarred: Boolean,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    displayActions: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            FeedIcon(
                iconUrl = feedIconUrl,
                name = feedName
            )

            ShortSpacer()

            Column {
                Text(
                    text = feedName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (feedColor != 0) {
                        Color(feedColor)
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )

                if (!folderName.isNullOrEmpty()) {
                    Text(
                        text = folderName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ShortSpacer()

        if (displayActions) {
            Row {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    IconButton(
                        onClick = onFavorite
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isStarred) R.drawable.ic_star
                                else R.drawable.ic_star_outline
                            ),
                            contentDescription = null,
                        )
                    }
                }

                ShortSpacer()

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    IconButton(
                        onClick = onShare
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        } else {
            TimelineItemBadge(
                date = date,
                duration = duration,
                color = feedColor
            )
        }
    }
}


@Composable
fun TimelineItemTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun TimelineItemBadge(
    date: LocalDateTime,
    duration: Double,
    color: Int,
) {
    val textColor = if (color != 0) Color.White else MaterialTheme.colorScheme.onPrimary


    Surface(
        color = if (color != 0) Color(color) else MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.shortSpacing,
                vertical = MaterialTheme.spacing.veryShortSpacing
            )
        ) {
            Text(
                text = DateUtils.formattedDateByLocal(date),
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )

            Text(
                text = stringResource(id = R.string.interpoint),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.veryShortSpacing),
                color = textColor
            )

            Text(
                text = if (duration > 1) {
                    stringResource(id = R.string.read_time, duration.roundToInt())
                } else {
                    stringResource(id = R.string.read_time_lower_than_1)
                },
                style = MaterialTheme.typography.labelSmall,
                color = textColor
            )
        }
    }
}