package com.readrops.app.feeds

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.readrops.app.R
import com.readrops.app.util.components.ThreeDotsMenu
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder

@Composable
fun FolderExpandableItem(
    folder: Folder,
    feeds: List<Feed>,
    isExpanded: Boolean = false,
    onFeedClick: (Feed) -> Unit,
    onFeedLongClick: (Feed) -> Unit,
    onUpdateFolder: () -> Unit,
    onDeleteFolder: () -> Unit
) {
    var isFolderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(isExpanded) {
        isFolderExpanded = isExpanded
    }

    Column(
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .clickable { isFolderExpanded = isFolderExpanded.not() }
                .padding(
                    start = MaterialTheme.spacing.mediumSpacing,
                    top = MaterialTheme.spacing.veryShortSpacing,
                    bottom = MaterialTheme.spacing.veryShortSpacing
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder_grey),
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = folder.name
                    )

                    MediumSpacer()

                    Text(
                        text = folder.name!!,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                ThreeDotsMenu(
                    items = mapOf(
                        1 to stringResource(id = R.string.update),
                        2 to stringResource(id = R.string.delete)
                    ),
                    onItemClick = { index ->
                        when (index) {
                            1 -> onUpdateFolder()
                            else -> onDeleteFolder()
                        }
                    }
                )
            }
        }

        Column {
            if (isFolderExpanded) {
                for (feed in feeds) {
                    FeedItem(
                        feed = feed,
                        onClick = { onFeedClick(feed) },
                        onLongClick = { onFeedLongClick(feed) },
                    )
                }
            }
        }
    }
}