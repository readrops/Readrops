package com.readrops.app.timelime.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.readrops.app.R
import com.readrops.app.timelime.TimelineState
import com.readrops.app.util.components.FeedIcon
import com.readrops.app.util.extensions.isTabletUi
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.filters.MainFilter


@Composable
fun TimelineDrawer(
    state: TimelineState,
    drawerState: DrawerState,
    onClickDefaultItem: (MainFilter) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onFeedClick: (Feed) -> Unit,
    content: @Composable () -> Unit,
) {
    if (isTabletUi()) {
        PermanentNavigationDrawer(
            drawerContent = {
                TimelineDrawerContent(
                    state = state,
                    onClickDefaultItem = onClickDefaultItem,
                    onFolderClick = onFolderClick,
                    onFeedClick = onFeedClick
                )
            },
            content = content
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                TimelineDrawerContent(
                    state = state,
                    onClickDefaultItem = onClickDefaultItem,
                    onFolderClick = onFolderClick,
                    onFeedClick = onFeedClick
                )
            },
            content = content
        )
    }
}

@Composable
fun TimelineDrawerContent(
    state: TimelineState,
    onClickDefaultItem: (MainFilter) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onFeedClick: (Feed) -> Unit,
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.size(MaterialTheme.spacing.drawerSpacing))

        DrawerDefaultItems(
            selectedItem = state.filters.mainFilter,
            unreadNewItemsCount = state.unreadNewItemsCount,
            onClick = { onClickDefaultItem(it) }
        )

        DrawerDivider()

        Column {
            for (folderEntry in state.foldersAndFeeds) {
                val folder = folderEntry.key

                if (folder != null) {
                    DrawerFolderItem(
                        label = {
                            Text(
                                text = folder.name.orEmpty(),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                painterResource(id = R.drawable.ic_folder_grey),
                                contentDescription = null
                            )
                        },
                        badge = {
                            Text(folderEntry.value.sumOf { it.unreadCount }.toString())
                        },
                        selected = state.filters.folderId == folder.id,
                        onClick = { onFolderClick(folder) },
                        feeds = folderEntry.value,
                        selectedFeed = state.filters.feedId,
                        onFeedClick = { onFeedClick(it) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else {
                    val feeds = folderEntry.value

                    for (feed in feeds) {
                        DrawerFeedItem(
                            label = {
                                Text(
                                    text = feed.name.orEmpty(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                FeedIcon(
                                    iconUrl = feed.iconUrl,
                                    name = feed.name.orEmpty()
                                )
                            },
                            badge = { Text(feed.unreadCount.toString()) },
                            selected = feed.id == state.filters.feedId,
                            onClick = { onFeedClick(feed) },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerDefaultItems(
    selectedItem: MainFilter,
    unreadNewItemsCount: Int,
    onClick: (MainFilter) -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.articles)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_timeline),
                contentDescription = null
            )
        },
        selected = selectedItem == MainFilter.ALL,
        onClick = { onClick(MainFilter.ALL) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = {
            Text(
                "${stringResource(id = R.string.new_articles)} (${
                    stringResource(
                        id = R.string.unread,
                        unreadNewItemsCount
                    )
                })"
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_new),
                contentDescription = null
            )
        },
        selected = selectedItem == MainFilter.NEW,
        onClick = { onClick(MainFilter.NEW) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.favorites)) },
        icon = {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null
            )
        },
        selected = selectedItem == MainFilter.STARS,
        onClick = { onClick(MainFilter.STARS) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun DrawerDivider() {
    HorizontalDivider(
        thickness = 2.dp,
        modifier = Modifier.padding(
            vertical = MaterialTheme.spacing.drawerSpacing,
            horizontal = 28.dp // M3 guidelines
        )
    )
}