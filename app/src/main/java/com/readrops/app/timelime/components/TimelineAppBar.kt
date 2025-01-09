package com.readrops.app.timelime.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.readrops.app.R
import com.readrops.app.timelime.TimelineState
import com.readrops.db.filters.MainFilter
import com.readrops.db.filters.SubFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineAppBar(
    state: TimelineState,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    onOpenFilterSheet:  () -> Unit,
    onRefreshTimeline: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = when (state.filters.mainFilter) {
                        MainFilter.STARS -> stringResource(R.string.favorites)
                        MainFilter.ALL -> stringResource(R.string.articles)
                        MainFilter.NEW -> stringResource(R.string.new_articles)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (state.showSubtitle) {
                    Text(
                        text = when (state.filters.subFilter) {
                            SubFilter.FEED -> state.filterFeedName
                            SubFilter.FOLDER -> state.filterFolderName
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(
                onClick = onOpenFilterSheet
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter_list),
                    contentDescription = null
                )
            }

            IconButton(
                onClick = onRefreshTimeline
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sync),
                    contentDescription = null
                )
            }
        },
        scrollBehavior = topAppBarScrollBehavior
    )

}