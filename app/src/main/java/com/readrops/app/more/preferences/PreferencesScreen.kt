package com.readrops.app.more.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.more.preferences.components.ListPreferenceWidget
import com.readrops.app.more.preferences.components.PreferenceHeader
import com.readrops.app.more.preferences.components.SwitchPreferenceWidget
import com.readrops.app.sync.SyncWorker
import com.readrops.app.util.components.AndroidScreen
import com.readrops.app.util.components.CenteredProgressIndicator


class PreferencesScreen : AndroidScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val screenModel = getScreenModel<PreferencesScreenModel>()

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
            }
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

                        Column {
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

                            PreferenceHeader(text = stringResource(id = R.string.timeline))

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
                                title = stringResource(id = R.string.mark_items_read)
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
                        }
                    }
                }
            }
        }
    }
}

