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
            combine(
                preferences.theme.flow,
                preferences.backgroundSynchronization.flow,
                preferences.scrollRead.flow,
                preferences.hideReadFeeds.flow,
                preferences.openLinksWith.flow
            ) { (theme, backgroundSync, scrollRead, hideReadFeeds, openLinksWith) ->
                PreferencesScreenState.Loaded(
                    themePref = (theme as String) to preferences.theme,
                    backgroundSyncPref = (backgroundSync as String) to preferences.backgroundSynchronization,
                    scrollReadPref = (scrollRead as Boolean) to preferences.scrollRead,
                    hideReadFeeds = (hideReadFeeds as Boolean) to preferences.hideReadFeeds,
                    openLinksWith = (openLinksWith as String) to preferences.openLinksWith
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
        val openLinksWith: PreferenceState<String>
    ) : PreferencesScreenState()

}