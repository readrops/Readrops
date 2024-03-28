package com.readrops.app.compose.timelime.drawer

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.app.compose.R
import com.readrops.app.compose.util.theme.DrawerSpacing
import com.readrops.db.entities.Feed

@Composable
fun DrawerFolderItem(
    label: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    badge: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    feeds: List<Feed>,
    selectedFeed: Int,
    onFeedClick: (Feed) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NavigationDrawerItemDefaults.colors()

    var isExpanded by remember { mutableStateOf(feeds.any { it.id == selectedFeed }) }
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "drawer item arrow rotation"
    )

    Column(
        modifier = Modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing,
            )
        )
    ) {
        Surface(
            selected = selected,
            onClick = onClick,
            color = colors.containerColor(selected = selected).value,
            shape = CircleShape,
            modifier = modifier
                .height(56.dp)
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearOutSlowInEasing,
                    )
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 24.dp)
            ) {
                val iconColor = colors.iconColor(selected).value
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)

                DrawerSpacing()

                Box(Modifier.weight(1f)) {
                    val labelColor = colors.textColor(selected).value
                    CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
                }

                DrawerSpacing()

                val badgeColor = colors.badgeColor(selected).value
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)

                DrawerSpacing()

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable { isExpanded = isExpanded.not() }
                        .rotate(rotationState),
                )
            }
        }

        if (isExpanded && feeds.isNotEmpty()) {
            for (feed in feeds) {
                DrawerFeedItem(
                    label = {
                        Text(
                            text = feed.name!!,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        AsyncImage(
                            model = feed.iconUrl,
                            contentDescription = feed.name,
                            error = painterResource(id = R.drawable.ic_rss_feed_grey),
                            placeholder = painterResource(id = R.drawable.ic_folder_grey),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    badge = { Text(feed.unreadCount.toString()) },
                    selected = feed.id == selectedFeed,
                    onClick = { onFeedClick(feed) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}