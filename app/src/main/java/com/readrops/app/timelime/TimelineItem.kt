package com.readrops.app.timelime

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Folder
import com.readrops.db.entities.OpenIn
import com.readrops.db.pojo.ItemWithFeed
import java.time.LocalDateTime

enum class TimelineItemSize {
    COMPACT,
    REGULAR,
    LARGE
}

const val readAlpha = 0.6f

@Composable
fun TimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onSetReadState: () -> Unit,
    modifier: Modifier = Modifier,
    size: TimelineItemSize = TimelineItemSize.LARGE
) {
    val swipeState = rememberSwipeToDismissBoxState()

    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSetReadState()
            swipeState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                label = "Swipe to dismiss background color"
            )

            val iconColor by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onPrimary
                    else -> Color.Transparent
                },
                label = "Swipe to dismiss icon color"
            )

            Box(
                modifier = Modifier.padding(
                    horizontal = if (size == TimelineItemSize.COMPACT) {
                        0.dp
                    } else {
                        MaterialTheme.spacing.shortSpacing
                    }
                )
            ) {
                Box(
                    contentAlignment = Alignment.CenterEnd,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (size == TimelineItemSize.COMPACT) {
                                Modifier
                            } else {
                                Modifier.clip(CardDefaults.shape)
                            }
                        )
                        .background(color)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (itemWithFeed.item.isRead) {
                                R.drawable.ic_remove_done
                            } else {
                                R.drawable.ic_done_all
                            }
                        ),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .padding(end = MaterialTheme.spacing.mediumSpacing)
                    )
                }
            }
        }
    ) {
        when (size) {
            TimelineItemSize.COMPACT -> {
                CompactTimelineItem(
                    itemWithFeed = itemWithFeed,
                    onClick = onClick,
                    onFavorite = onFavorite,
                    onShare = onShare,
                    modifier = modifier
                )
            }

            TimelineItemSize.REGULAR -> {
                RegularTimelineItem(
                    itemWithFeed = itemWithFeed,
                    onClick = onClick,
                    onFavorite = onFavorite,
                    onShare = onShare,
                    modifier = modifier
                )
            }

            TimelineItemSize.LARGE -> {
                LargeTimelineItem(
                    itemWithFeed = itemWithFeed,
                    onClick = onClick,
                    onFavorite = onFavorite,
                    onShare = onShare,
                    modifier = modifier
                )
            }
        }
    }
}

private val itemWithFeed = ItemWithFeed(
    item = com.readrops.db.entities.Item(
        title = "This is a not so long item title",
        pubDate = LocalDateTime.now(),
        cleanDescription = """Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                        Donec a tortor neque. Nam ultrices, diam ac congue finibus, tortor sem congue urna,
                         at finibus elit libero at mi. Etiam hendrerit sapien eu porta feugiat. Duis porttitor"""
            .replace("\n", "")
            .trimMargin(),
        imageLink = ""
    ),
    feedName = "feed name",
    color = 0,
    feedId = 0,
    feedIconUrl = "",
    websiteUrl = "",
    folder = Folder(name = "Folder name"),
    openIn = OpenIn.LOCAL_VIEW
)

@DefaultPreview
@Composable
private fun RegularTimelineItemPreview() {
    ReadropsTheme {
        RegularTimelineItem(
            itemWithFeed = itemWithFeed,
            onClick = {},
            onFavorite = {},
            onShare = {},
        )
    }
}

@DefaultPreview
@Composable
private fun CompactTimelineItemPreview() {
    ReadropsTheme {
        CompactTimelineItem(
            itemWithFeed = itemWithFeed,
            onClick = {},
            onFavorite = {},
            onShare = {},
        )
    }
}

@DefaultPreview
@Composable
private fun LargeTimelineItemPreview() {
    ReadropsTheme {
        LargeTimelineItem(
            itemWithFeed = itemWithFeed,
            onClick = {},
            onFavorite = {},
            onShare = {},
        )
    }
}