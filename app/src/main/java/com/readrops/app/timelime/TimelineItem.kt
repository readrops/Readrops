package com.readrops.app.timelime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.readrops.app.util.DefaultPreview
import com.readrops.app.util.theme.ReadropsTheme
import com.readrops.db.entities.Folder
import com.readrops.db.pojo.ItemWithFeed
import org.joda.time.LocalDateTime

enum class TimelineItemSize {
    COMPACT,
    REGULAR,
    LARGE
}

@Composable
fun TimelineItem(
    itemWithFeed: ItemWithFeed,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    size: TimelineItemSize = TimelineItemSize.LARGE,
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
    folder = Folder(name = "Folder name")
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