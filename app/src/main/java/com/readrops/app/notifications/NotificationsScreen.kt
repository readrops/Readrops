package com.readrops.app.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.account.Account
import org.koin.core.parameter.parametersOf

class NotificationsScreen(val account: Account) : AndroidScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<NotificationsScreenModel> { parametersOf(account) }

        var isDropDownMenuExpanded by remember { mutableStateOf(false) }

        val state by screenModel.state.collectAsStateWithLifecycle()

        val topAppBarScrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.notifications)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { isDropDownMenuExpanded = isDropDownMenuExpanded.not() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                )
                            }

                            DropdownMenu(
                                expanded = isDropDownMenuExpanded,
                                onDismissRequest = { isDropDownMenuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (state.allFeedNotificationsEnabled) {
                                                stringResource(id = R.string.disable_all)
                                            } else {
                                                stringResource(id = R.string.enable_all)
                                            }
                                        )
                                    },
                                    onClick = {
                                        isDropDownMenuExpanded = false
                                        screenModel.setAllFeedsNotificationsState(enabled = !state.allFeedNotificationsEnabled)
                                    }
                                )
                            }
                        }
                    },
                    scrollBehavior = topAppBarScrollBehavior
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.mediumSpacing)

                    ) {
                        Text(
                            text = stringResource(id = R.string.enable_notifications)
                        )

                        Switch(
                            checked = state.isNotificationsEnabled,
                            onCheckedChange = { screenModel.setAccountNotificationsState(it) }
                        )
                    }

                    MediumSpacer()

                    Text(
                        text = stringResource(id = R.string.feeds),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                    )
                }

                items(
                    items = state.feedsWithFolder,
                    key = { it.feed.id }
                ) { feedWithFolder ->
                    NotificationItem(
                        feedName = feedWithFolder.feed.name!!,
                        iconUrl = feedWithFolder.feed.iconUrl,
                        folderName = feedWithFolder.folderName,
                        checked = feedWithFolder.feed.isNotificationEnabled,
                        enabled = state.isNotificationsEnabled,
                        onCheckChange = {
                            if (state.isNotificationsEnabled) {
                                screenModel.setFeedNotificationsState(feedWithFolder.feed.id, it)
                            }
                        }
                    )
                }
            }
        }

    }
}

