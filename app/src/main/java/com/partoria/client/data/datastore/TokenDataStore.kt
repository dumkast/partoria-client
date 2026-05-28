package com.partoria.client.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class TokenDataStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role")
    }

    suspend fun saveToken(token: String, username: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USERNAME_KEY] = username
            preferences[ROLE_KEY] = role
        }
    }

    fun getRole(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ROLE_KEY]
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    fun getUsername(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USERNAME_KEY]
        }
    }
}