package com.readrops.app.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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