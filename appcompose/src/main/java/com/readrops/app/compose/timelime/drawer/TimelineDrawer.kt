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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.readrops.app.compose.R
import com.readrops.app.compose.timelime.DrawerState
import com.readrops.app.compose.util.theme.spacing

enum class DrawerDefaultItemsSelection {
    ARTICLES,
    NEW,
    FAVORITES,
    READ_LATER
}

@Composable
fun TimelineDrawer(
    state: DrawerState,
    onClickDefaultItem: (DrawerDefaultItemsSelection) -> Unit,
    onFolderClick: (Int) -> Unit,
    onFeedClick: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.size(MaterialTheme.spacing.drawerSpacing))

        DrawerDefaultItems(
            selectedItem = state.selection,
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
                        selected = state.selectedFolderId == folder.id,
                        onClick = { onFolderClick(folder.id) },
                        feeds = folderEntry.value,
                        selectedFeed = state.selectedFeedId,
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
                            selected = feed.id == state.selectedFeedId,
                            onClick = { onFeedClick(feed.id) },
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
    selectedItem: DrawerDefaultItemsSelection,
    onClick: (DrawerDefaultItemsSelection) -> Unit,
) {
    NavigationDrawerItem(
        label = { Text("Articles") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_timeline),
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.ARTICLES,
        onClick = { onClick(DrawerDefaultItemsSelection.ARTICLES) },
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
        selected = selectedItem == DrawerDefaultItemsSelection.NEW,
        onClick = { onClick(DrawerDefaultItemsSelection.NEW) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("Favorites") },
        icon = {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.FAVORITES,
        onClick = { onClick(DrawerDefaultItemsSelection.FAVORITES) },
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )

    NavigationDrawerItem(
        label = { Text("To read later") },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_read_later),
                contentDescription = null
            )
        },
        selected = selectedItem == DrawerDefaultItemsSelection.READ_LATER,
        onClick = { onClick(DrawerDefaultItemsSelection.READ_LATER) },
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