package com.partoria.client.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {

    private val AVATAR_COLOR_INDEX_KEY = intPreferencesKey("avatar_color_index")

    val avatarColorIndexFlow: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[AVATAR_COLOR_INDEX_KEY] ?: -1
    }

    suspend fun saveAvatarColorIndex(index: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[AVATAR_COLOR_INDEX_KEY] = index
        }
    }
}