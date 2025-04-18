package com.readrops.app.more.preferences

import android.content.Context
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.readrops.app.R
import com.readrops.app.util.Preference
import com.readrops.app.util.Preferences
import com.readrops.db.Database
import com.readrops.db.entities.Item
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

typealias PreferenceState<T> = Pair<T, Preference<T>>

class PreferencesScreenModel(
    database: Database,
    context: Context,
    preferences: Preferences,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : StateScreenModel<PreferencesScreenState>(PreferencesScreenState.Loading) {
    init {
        screenModelScope.launch(dispatcher) {
            with(preferences) {
                val flows = listOf(
                    theme.flow,
                    backgroundSynchronization.flow,
                    scrollRead.flow,
                    hideReadFeeds.flow,
                    openLinksWith.flow,
                    timelineItemSize.flow,
                    mainFilter.flow,
                    synchAtLaunch.flow,
                    useCustomShareIntentTpl.flow,
                    customShareIntentTpl.flow,
                    swipeToLeft.flow,
                    swipeToRight.flow,
                    themeColorScheme.flow
                )

                combine(
                    flows
                ) { list ->
                    PreferencesScreenState.Loaded(
                        themePref = (list[0] as String) to theme,
                        backgroundSyncPref = (list[1] as String) to backgroundSynchronization,
                        scrollReadPref = (list[2] as Boolean) to scrollRead,
                        hideReadFeeds = (list[3] as Boolean) to hideReadFeeds,
                        openLinksWith = (list[4] as String) to openLinksWith,
                        timelineItemSize = (list[5] as String) to timelineItemSize,
                        mainFilterPref = (list[6] as String) to mainFilter,
                        syncAtLaunchPref = (list[7] as Boolean) to synchAtLaunch,
                        useCustomShareIntentTpl = (list[8] as Boolean) to useCustomShareIntentTpl,
                        customShareIntentTpl = (list[9] as String) to customShareIntentTpl,
                        swipeToLeft = (list[10] as String) to swipeToLeft,
                        swipeToRight = (list[11] as String) to swipeToRight,
                        themeColorScheme = (list[12] as String) to themeColorScheme,
                        exampleItem = if (database.itemDao().count() > 0) {
                            database.itemDao().selectFirst()
                        } else {
                            Item(
                                title = context.getString(R.string.example_item_title),
                                author = context.getString(R.string.example_item_author),
                                content = context.getString(R.string.example_item_content),
                                link = "https://example.org"
                            )
                        }
                    )
                }.collect { theme ->
                    mutableState.update { previous ->
                        (previous as? PreferencesScreenState.Loaded)?.let {
                            theme.copy(showDialog = previous.showDialog)
                        } ?: theme
                    }
                }
            }
        }
    }

    fun updateDialog(isVisible: Boolean) {
        if (mutableState.value is PreferencesScreenState.Loaded) {
            mutableState.update {
                (mutableState.value as PreferencesScreenState.Loaded).copy(
                    showDialog = isVisible
                )
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
        val mainFilterPref: PreferenceState<String>,
        val syncAtLaunchPref: PreferenceState<Boolean>,
        val useCustomShareIntentTpl: PreferenceState<Boolean>,
        val customShareIntentTpl: PreferenceState<String>,
        val swipeToLeft: PreferenceState<String>,
        val swipeToRight: PreferenceState<String>,
        val themeColorScheme: PreferenceState<String>,
        val exampleItem: Item,
        val showDialog: Boolean = false
    ) : PreferencesScreenState()

}