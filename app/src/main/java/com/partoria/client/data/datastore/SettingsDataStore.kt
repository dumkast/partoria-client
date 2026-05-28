package com.partoria.client.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.partoria.client.presentation.screens.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val AVATAR_COLOR_INDEX_KEY = intPreferencesKey("avatar_color_index")
    }

    val appThemeFlow: Flow<AppTheme> = context.settingsDataStore.data
        .catch { exception ->
            emit(emptyPreferences())
        }
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: AppTheme.SYSTEM.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }

    val avatarColorIndexFlow: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[AVATAR_COLOR_INDEX_KEY] ?: -1
    }

    suspend fun saveAppTheme(theme: AppTheme) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    suspend fun saveAvatarColorIndex(index: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[AVATAR_COLOR_INDEX_KEY] = index
        }
    }
}