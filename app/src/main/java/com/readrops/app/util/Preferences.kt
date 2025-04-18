package com.readrops.app.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class Preference<T>(
    val dataStore: DataStorePreferences,
    val key: Preferences.Key<T>,
    val default: T,
    val flow: Flow<T> = dataStore.read(key, default)
) {

    suspend fun write(value: T) {
        dataStore.write(key, value)
    }
}

class Preferences(
    dataStore: DataStorePreferences,
) {

    val theme = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("theme"),
        default = "system"
    )

    val themeColorScheme = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("theme_color_scheme"),
        default = "readrops"
    )

    val backgroundSynchronization = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("synchro"),
        default = "manual"
    )

    val scrollRead = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("scroll_read"),
        default = false
    )

    val hideReadFeeds = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("hide_read_feeds"),
        default = false
    )

    val openLinksWith = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("open_links_with"),
        default = "navigator_view"
    )

    val timelineItemSize = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("timeline_item_size"),
        default = "large"
    )

    val displayNotificationsPermission = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("display_notification_permission"),
        default = true
    )

    val lastVersionCode = Preference(
        dataStore = dataStore,
        key = intPreferencesKey("last_version_code"),
        default = 0
    )

    val showReadItems = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("show_read_items"),
        default = true
    )

    val orderField = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("order_field"),
        default = "DATE" // or "ID", uppercase important, used with Enum.valueOf()
    )

    val orderType = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("order_type"),
        default = "DESC" // or "ASC", uppercase important, used with Enum.valueOf()
    )

    val globalOpenInAsk = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("open_in_ask"),
        default = true
    )

    val mainFilter = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("main_filter"),
        default = "ALL" // uppercase important, used with Enum.valueOf()
    )

    val synchAtLaunch = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("sync_at_launch"),
        default = false
    )

    val useCustomShareIntentTpl = Preference(
        dataStore = dataStore,
        key = booleanPreferencesKey("use_custom_share_intent_tpl"),
        default = false
    )

    val customShareIntentTpl = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("custom_share_intent_tpl"),
        default = ""
    )

    val swipeToRight = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("swipe_to_right"),
        default = "DISABLED"
    )

    val swipeToLeft = Preference(
        dataStore = dataStore,
        key = stringPreferencesKey("swipe_to_left_action"),
        default = "READ"
    )
}


class DataStorePreferences(private val dataStore: DataStore<Preferences>) {

    fun <T> read(key: Preferences.Key<T>, default: T): Flow<T> {
        return dataStore.data
            .map { it[key] ?: default }
            .distinctUntilChanged()
    }

    suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }
}