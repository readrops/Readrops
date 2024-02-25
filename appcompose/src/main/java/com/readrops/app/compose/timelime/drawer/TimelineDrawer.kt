package com.readrops.app.compose.timelime.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.app.compose.R
import com.readrops.app.compose.timelime.TimelineState
import com.readrops.app.compose.util.theme.spacing
import com.readrops.db.entities.Feed
import com.readrops.db.entities.Folder
import com.readrops.db.filters.FilterType

@Composable
fun TimelineDrawer(
    state: TimelineState,
    onClickDefaultItem: (FilterType) -> Unit,
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
            selectedItem = state.filters.filterType,
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
                                text = folder.name!!,
                                maxLines =  1,
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
                        selected = state.filters.filterFolderId == folder.id,
                        onClick = { onFolderClick(folder) },
                        feeds = folderEntry.value,
                        selectedFeed = state.filters.filterFeedId,
                        onFeedClick = { onFeedClick(it) },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                } else {
                    val feeds = folderEntry.value

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
                                    placeholder = painterResource(id = R.drawable.ic_folder_grey),
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            badge = { Text(feed.unreadCount.toString()) },
                            selected = feed.id == state.filters.filterFeedId,
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
    selectedItem: FilterType,
    onClick: (FilterType) -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(text = stringResource(R.string.articles)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_timeline),
                contentDescription = null
            )
        },
        selected = selectedItem == FilterType.NO_FILTER,
        onClick = { onClick(FilterType.NO_FILTER) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("New articles") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_new),
                contentDescription = null
            )
        },
        selected = selectedItem == FilterType.NEW,
        onClick = { onClick(FilterType.NEW) },
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
        selected = selectedItem == FilterType.STARS_FILTER,
        onClick = { onClick(FilterType.STARS_FILTER) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun DrawerDivider() {
    Divider(
        thickness = 2.dp,
        modifier = Modifier.padding(
            vertical = MaterialTheme.spacing.drawerSpacing,
            horizontal = 28.dp // M3 guidelines
        )
    )
}