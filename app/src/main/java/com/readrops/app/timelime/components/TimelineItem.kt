package com.readrops.app.timelime.components

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

enum class SwipeAction {
    READ,
    FAVORITE,
    DISABLED
}

const val readAlpha = 0.6f


@Composable
fun TimelineItem(
    itemWithFeed: ItemWithFeed,
    swipeToLeft: SwipeAction,
    swipeToRight: SwipeAction,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onSetReadState: () -> Unit,
    modifier: Modifier = Modifier,
    size: TimelineItemSize = TimelineItemSize.LARGE
) {

    fun handleSwipeAction(swipeAction: SwipeAction) {
        when (swipeAction) {
            SwipeAction.READ -> onSetReadState()
            SwipeAction.FAVORITE -> onFavorite()
            else -> {}
        }
    }

    val swipeState = rememberSwipeToDismissBoxState()

    LaunchedEffect(swipeState.currentValue) {
        if (swipeState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            handleSwipeAction(swipeToLeft)
            swipeState.dismiss(SwipeToDismissBoxValue.Settled)
        } else if (swipeState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            handleSwipeAction(swipeToRight)
            swipeState.dismiss(SwipeToDismissBoxValue.Settled)
        }
    }

    fun getSwipeIcon(swipeAction: SwipeAction): Int {
        return if (swipeAction == SwipeAction.READ) {
            if (itemWithFeed.isRead) {
                R.drawable.ic_remove_done
            } else {
                R.drawable.ic_done_all
            }
        } else {
            if (itemWithFeed.isStarred) {
                R.drawable.ic_star_outline
            } else {
                R.drawable.ic_star
            }
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = swipeToRight != SwipeAction.DISABLED,
        enableDismissFromEndToStart = swipeToLeft != SwipeAction.DISABLED,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                label = "Swipe to dismiss background color"
            )

            val iconColor by animateColorAsState(
                targetValue = when (swipeState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimary
                    else -> Color.Transparent
                },
                label = "Swipe to dismiss icon color"
            )

            val icon = when (swipeState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> getSwipeIcon(swipeToLeft)
                SwipeToDismissBoxValue.StartToEnd -> getSwipeIcon(swipeToRight)
                else -> null
            }

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
                    contentAlignment = if (swipeState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    },
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
                    if (icon != null) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                        )
                    }
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

val itemWithFeed = ItemWithFeed(
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
    openIn = OpenIn.LOCAL_VIEW,
    openInAsk = true
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