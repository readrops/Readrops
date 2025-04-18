package com.readrops.app.more.preferences

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.more.preferences.components.BasePreference
import com.readrops.app.more.preferences.components.CustomShareIntentTextWidget
import com.readrops.app.more.preferences.components.ListPreferenceWidget
import com.readrops.app.more.preferences.components.PreferenceHeader
import com.readrops.app.more.preferences.components.SwitchPreferenceWidget
import com.readrops.app.sync.SyncWorker
import com.readrops.app.timelime.components.SwipeAction
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.CenteredProgressIndicator
import kotlinx.coroutines.launch


class PreferencesScreen : AndroidScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val screenModel = koinScreenModel<PreferencesScreenModel>()

        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val state by screenModel.state.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.preferences)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.pop() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues)
            ) {
                when (state) {
                    is PreferencesScreenState.Loading -> {
                        CenteredProgressIndicator()
                    }

                    else -> {
                        val loadedState = (state as PreferencesScreenState.Loaded)

                        // a lazyColumn might be necessary in the future
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            PreferenceHeader(text = stringResource(id = R.string.global))

                            ListPreferenceWidget(
                                preference = loadedState.themePref.second,
                                selectedKey = loadedState.themePref.first,
                                entries = mapOf(
                                    "light" to stringResource(id = R.string.light),
                                    "dark" to stringResource(id = R.string.dark),
                                    "system" to stringResource(id = R.string.system)
                                ),
                                title = stringResource(id = R.string.theme),
                                onValueChange = {}
                            )

                            ListPreferenceWidget(
                                preference = loadedState.themeColorScheme.second,
                                selectedKey = loadedState.themeColorScheme.first,
                                entries = mapOf(
                                    "readrops" to stringResource(id = R.string.theme_readrops),
                                    "blackwhite" to stringResource(id = R.string.theme_blackwhite),
                                ),
                                title = stringResource(id = R.string.theme_color_scheme),
                                onValueChange = {}
                            )

                            ListPreferenceWidget(
                                preference = loadedState.backgroundSyncPref.second,
                                selectedKey = loadedState.backgroundSyncPref.first,
                                entries = mapOf(
                                    "manual" to stringResource(id = R.string.manual),
                                    "0.30" to stringResource(id = R.string.min_30),
                                    "1" to stringResource(id = R.string.hour_1),
                                    "2" to stringResource(id = R.string.hour_2),
                                    "3" to stringResource(id = R.string.hour_3),
                                    "6" to stringResource(id = R.string.hour_6),
                                    "12" to stringResource(id = R.string.hour_12),
                                    "24" to stringResource(id = R.string.every_day)
                                ),
                                title = stringResource(id = R.string.auto_synchro),
                                onValueChange = { SyncWorker.startPeriodically(context, it) }
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                BasePreference(
                                    title = stringResource(R.string.disable_battery_optimization),
                                    subtitle = stringResource(R.string.disable_battery_optimization_subtitle),
                                    onClick = {
                                        val powerManager =
                                            context.getSystemService("power") as PowerManager
                                        val packageName = context.packageName

                                        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                                            @SuppressLint("BatteryLife")
                                            val intent = Intent().apply {
                                                action =
                                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                                data = Uri.parse("package:$packageName")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }

                                            context.startActivity(intent)
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(context.getString(R.string.battery_optimization_already_disabled))
                                            }
                                        }
                                    }
                                )
                            }

                            PreferenceHeader(text = stringResource(id = R.string.timeline))

                            SwitchPreferenceWidget(
                                preference = loadedState.syncAtLaunchPref.second,
                                isChecked = loadedState.syncAtLaunchPref.first,
                                title = stringResource(R.string.synchronize_at_launch)
                            )

                            ListPreferenceWidget(
                                preference = loadedState.mainFilterPref.second,
                                selectedKey = loadedState.mainFilterPref.first,
                                entries = mapOf(
                                    "ALL" to stringResource(R.string.articles),
                                    "NEW" to stringResource(R.string.new_articles),
                                    "STARS" to stringResource(R.string.favorites)
                                ),
                                title = stringResource(R.string.default_category),
                                onValueChange = {}
                            )

                            ListPreferenceWidget(
                                preference = loadedState.timelineItemSize.second,
                                selectedKey = loadedState.timelineItemSize.first,
                                entries = mapOf(
                                    "compact" to stringResource(id = R.string.compact),
                                    "regular" to stringResource(id = R.string.regular),
                                    "large" to stringResource(id = R.string.large)
                                ),
                                title = stringResource(id = R.string.item_size),
                                onValueChange = {}
                            )

                            SwitchPreferenceWidget(
                                preference = loadedState.hideReadFeeds.second,
                                isChecked = loadedState.hideReadFeeds.first,
                                title = stringResource(id = R.string.hide_feeds),
                                subtitle = stringResource(R.string.hide_feeds_subtitle)
                            )

                            SwitchPreferenceWidget(
                                preference = loadedState.scrollReadPref.second,
                                isChecked = loadedState.scrollReadPref.first,
                                title = stringResource(id = R.string.mark_items_read_on_scroll)
                            )

                            ListPreferenceWidget(
                                preference = loadedState.swipeToLeft.second,
                                selectedKey = loadedState.swipeToLeft.first,
                                entries = mapOf(
                                    SwipeAction.DISABLED.name to stringResource(R.string.disabled),
                                    SwipeAction.READ.name to stringResource(R.string.mark_read),
                                    SwipeAction.FAVORITE.name to stringResource(R.string.add_to_favorite)
                                ),
                                title = stringResource(R.string.swipe_to_left_action),
                                onValueChange = {}
                            )

                            ListPreferenceWidget(
                                preference = loadedState.swipeToRight.second,
                                selectedKey = loadedState.swipeToRight.first,
                                entries = mapOf(
                                    SwipeAction.DISABLED.name to stringResource(R.string.disabled),
                                    SwipeAction.READ.name to stringResource(R.string.mark_read),
                                    SwipeAction.FAVORITE.name to stringResource(R.string.add_to_favorite)
                                ),
                                title = stringResource(R.string.swipe_to_right_action),
                                onValueChange = {}
                            )

                            PreferenceHeader(text = stringResource(id = R.string.item_view))

                            ListPreferenceWidget(
                                preference = loadedState.openLinksWith.second,
                                selectedKey = loadedState.openLinksWith.first,
                                entries = mapOf(
                                    "navigator_view" to stringResource(id = R.string.navigator_view),
                                    "external_navigator" to stringResource(id = R.string.external_navigator)
                                ),
                                title = stringResource(id = R.string.open_items_in),
                                onValueChange = {}
                            )

                            SwitchPreferenceWidget(
                                preference = loadedState.useCustomShareIntentTpl.second,
                                isChecked = loadedState.useCustomShareIntentTpl.first,
                                title = stringResource(id = R.string.use_custom_share_intent_tpl),
                                onValueChanged = screenModel::updateDialog
                            )

                            if (loadedState.showDialog) {
                                CustomShareIntentTextWidget(
                                    preference = loadedState.customShareIntentTpl.second,
                                    template = loadedState.customShareIntentTpl.first,
                                    exampleItem = loadedState.exampleItem,
                                    onDismiss = { screenModel.updateDialog(false) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

