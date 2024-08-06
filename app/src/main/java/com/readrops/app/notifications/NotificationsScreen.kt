package com.readrops.app.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.readrops.app.R
import com.readrops.app.more.preferences.PreferencesScreen
import com.readrops.app.more.preferences.components.BasePreference
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.CenteredProgressIndicator
import com.readrops.app.util.components.Placeholder
import com.readrops.app.util.components.ThreeDotsMenu
import com.readrops.app.util.components.dialog.TwoChoicesDialog
import com.readrops.app.util.theme.LargeSpacer
import com.readrops.app.util.theme.MediumSpacer
import com.readrops.app.util.theme.spacing
import com.readrops.db.entities.account.Account
import org.koin.core.parameter.parametersOf

class NotificationsScreen(val account: Account) : AndroidScreen() {

    @SuppressLint("InlinedApi")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val screenModel = getScreenModel<NotificationsScreenModel> { parametersOf(account) }

        val state by screenModel.state.collectAsStateWithLifecycle()

        val topAppBarScrollBehavior =
            TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
                screenModel.refreshNotificationManager()
            }

        LaunchedEffect(permissionState.status) {
            if (permissionState.status.isGranted) {
                screenModel.refreshNotificationManager()
            }
        }

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
                        if (state.feedsWithFolderState is FeedsWithFolderState.Loaded) {
                            val loadedState =
                                state.feedsWithFolderState as FeedsWithFolderState.Loaded

                            if (loadedState.feedsWithFolder.isNotEmpty()) {
                                ThreeDotsMenu(
                                    items = mapOf(
                                        1 to if (loadedState.allFeedNotificationsEnabled) {
                                            stringResource(id = R.string.disable_all)
                                        } else {
                                            stringResource(id = R.string.enable_all)
                                        }
                                    ),
                                    onItemClick = {
                                        screenModel.setAllFeedsNotificationsState(!loadedState.allFeedNotificationsEnabled)
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                    && !state.areNotificationsEnabled
                ) {
                    item {
                        BasePreference(
                            title = stringResource(R.string.grant_access_notifications),
                            subtitle = stringResource(R.string.system_notifications_disabled),
                            onClick = {
                                if (!permissionState.status.shouldShowRationale) {
                                    val intent = Intent().apply {
                                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }

                                    launcher.launch(intent)
                                } else {
                                    permissionState.launchPermissionRequest()
                                }
                            }
                        )
                    }
                }

                item {
                    BasePreference(
                        title = stringResource(R.string.show_notifications_background_sync),
                        subtitle = stringResource(R.string.background_sync_new_articles),
                        onClick = {
                            screenModel.setAccountNotificationsState(!state.areAccountNotificationsEnabled)

                            if (state.areAccountNotificationsEnabled.not()) {
                                screenModel.setBackgroundSyncDialogState(true)
                            }
                        },
                        rightComponent = {
                            Switch(
                                checked = state.areAccountNotificationsEnabled,
                                onCheckedChange = {
                                    screenModel.setAccountNotificationsState(it)

                                    if (it) {
                                        screenModel.setBackgroundSyncDialogState(true)
                                    }
                                }
                            )
                        }
                    )

                    MediumSpacer()

                    Text(
                        text = stringResource(id = R.string.feeds),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.mediumSpacing)
                    )
                }

                when (state.feedsWithFolderState) {
                    is FeedsWithFolderState.Loaded -> {
                        val feedsWithFolder =
                            (state.feedsWithFolderState as FeedsWithFolderState.Loaded).feedsWithFolder

                        if (feedsWithFolder.isNotEmpty()) {
                            items(
                                items = (state.feedsWithFolderState as FeedsWithFolderState.Loaded).feedsWithFolder,
                                key = { it.feed.id }
                            ) { feedWithFolder ->
                                NotificationItem(
                                    feedName = feedWithFolder.feed.name!!,
                                    iconUrl = feedWithFolder.feed.iconUrl,
                                    folderName = feedWithFolder.folderName,
                                    checked = feedWithFolder.feed.isNotificationEnabled,
                                    enabled = state.areAccountNotificationsEnabled,
                                    onCheckChange = {
                                        if (state.areAccountNotificationsEnabled) {
                                            screenModel.setFeedNotificationsState(
                                                feedWithFolder.feed.id,
                                                it
                                            )
                                        }
                                    }
                                )
                            }
                        } else {
                            item {
                                LargeSpacer()

                                Placeholder(
                                    text = stringResource(id = R.string.no_feed),
                                    painter = painterResource(id = R.drawable.ic_rss_feed_grey),
                                )
                            }
                        }
                    }

                    FeedsWithFolderState.Loading -> {
                        item {
                            repeat(4) {
                                LargeSpacer()
                            }

                            CenteredProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

