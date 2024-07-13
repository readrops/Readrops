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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.readrops.app.R
import com.readrops.app.more.preferences.components.ListPreferenceWidget
import com.readrops.app.more.preferences.components.PreferenceHeader
import com.readrops.app.more.preferences.components.SwitchPreferenceWidget
import com.readrops.app.util.Preferences
import com.readrops.app.util.components.AndroidScreen
import org.koin.core.component.KoinComponent


class PreferencesScreen(val preferences: Preferences) : AndroidScreen(), KoinComponent {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

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
                Column {
                    PreferenceHeader(text = stringResource(id = R.string.global))

                    ListPreferenceWidget(
                        preference = preferences.theme,
                        entries = mapOf(
                            "light" to stringResource(id = R.string.light),
                            "dark" to stringResource(id = R.string.dark),
                            "system" to stringResource(id = R.string.system)
                        ),
                        title = stringResource(id = R.string.theme),
                        onValueChange = {}
                    )

                    ListPreferenceWidget(
                        preference = preferences.backgroundSynchronization,
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
                        onValueChange = {}
                    )

                    PreferenceHeader(text = "Timeline")

                    SwitchPreferenceWidget(
                        preference = preferences.hideReadFeeds,
                        title = stringResource(id = R.string.hide_feeds),
                        subtitle = "Feeds with no left unread items will be hidden with their respective folder"
                    )

                    SwitchPreferenceWidget(
                        preference = preferences.scrollRead,
                        title = stringResource(id = R.string.mark_items_read)
                    )

                    PreferenceHeader(text = "Item view")

                    ListPreferenceWidget(
                        preference = preferences.openLinksWith,
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

