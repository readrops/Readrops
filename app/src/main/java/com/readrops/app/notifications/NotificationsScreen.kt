package com.readrops.app.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.more.preferences.PreferencesScreen
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.ThreeDotsMenu
import com.readrops.app.util.components.dialog.TwoChoicesDialog
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

        val state by screenModel.state.collectAsStateWithLifecycle()

        val topAppBarScrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        if (state.showBackgroundSyncDialog) {
            TwoChoicesDialog(
                title = stringResource(id = R.string.auto_synchro_disabled),
                text = stringResource(id = R.string.enable_auto_synchro_text),
                icon = painterResource(id = R.drawable.ic_sync),
                confirmText = stringResource(id = R.string.open),
                dismissText = stringResource(id = R.string.cancel),
                onDismiss = { screenModel.setBackgroundSyncDialogState(false) },
                onConfirm = {
                    screenModel.setBackgroundSyncDialogState(false)
                    navigator.push(PreferencesScreen())
                }
            )
        }

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
                        ThreeDotsMenu(
                            items = mapOf(
                                1 to if (state.allFeedNotificationsEnabled) {
                                    stringResource(id = R.string.disable_all)
                                } else {
                                    stringResource(id = R.string.enable_all)
                                }
                            ),
                            onItemClick = {
                                screenModel.setAllFeedsNotificationsState(!state.allFeedNotificationsEnabled)
                            }
                        )
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
                            onCheckedChange = {
                                screenModel.setAccountNotificationsState(it)

                                if (it) {
                                    screenModel.setBackgroundSyncDialogState(true)
                                }
                            }
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

