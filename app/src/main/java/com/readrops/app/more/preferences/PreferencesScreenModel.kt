package com.readrops.app.more.preferences

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.util.Preference
import com.readrops.app.util.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias PreferenceState<T> = Pair<T, Preference<T>>

class PreferencesScreenModel(
    preferences: Preferences,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<PreferencesScreenState>(PreferencesScreenState.Loading) {

    init {
        screenModelScope.launch(dispatcher) {
            val flows = listOf(
                preferences.theme.flow,
                preferences.backgroundSynchronization.flow,
                preferences.scrollRead.flow,
                preferences.hideReadFeeds.flow,
                preferences.openLinksWith.flow,
                preferences.timelineItemSize.flow,
                preferences.themeColourScheme.flow
            )

            combine(
                flows
            ) { list ->
                PreferencesScreenState.Loaded(
                    themePref = (list[0] as String) to preferences.theme,
                    backgroundSyncPref = (list[1] as String) to preferences.backgroundSynchronization,
                    scrollReadPref = (list[2] as Boolean) to preferences.scrollRead,
                    hideReadFeeds = (list[3] as Boolean) to preferences.hideReadFeeds,
                    openLinksWith = (list[4] as String) to preferences.openLinksWith,
                    timelineItemSize = (list[5] as String) to preferences.timelineItemSize,
                    themeColourScheme = (list[6] as String) to preferences.themeColourScheme
                )
            }.collect { theme ->
                mutableState.update { theme }
            }
        }
    }

}

sealed class PreferencesScreenState {
    data object Loading : PreferencesScreenState()
    data object Error : PreferencesScreenState()

    data class Loaded(
        val themePref: PreferenceState<String>,
        val backgroundSyncPref: PreferenceState<String>,
        val scrollReadPref: PreferenceState<Boolean>,
        val hideReadFeeds: PreferenceState<Boolean>,
        val openLinksWith: PreferenceState<String>,
        val timelineItemSize: PreferenceState<String>,
        val themeColourScheme: PreferenceState<String>
    ) : PreferencesScreenState()

}