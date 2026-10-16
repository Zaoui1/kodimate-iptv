package com.iptvplayer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iptvplayer.app.data.model.UserCredentials
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.credentialsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "iptv_credentials")

class CredentialsDataStore(private val context: Context) {

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val credentialsFlow: Flow<UserCredentials?> = context.credentialsDataStore.data.map { prefs ->
        val isLoggedIn = prefs[KEY_LOGGED_IN] ?: false
        val serverUrl = prefs[KEY_SERVER_URL]
        val username = prefs[KEY_USERNAME]
        val password = prefs[KEY_PASSWORD]

        if (isLoggedIn && serverUrl != null && username != null && password != null) {
            UserCredentials(serverUrl, username, password)
        } else {
            null
        }
    }

    suspend fun saveCredentials(serverUrl: String, username: String, password: String) {
        context.credentialsDataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = serverUrl
            prefs[KEY_USERNAME] = username
            prefs[KEY_PASSWORD] = password
            prefs[KEY_LOGGED_IN] = true
        }
    }

    suspend fun clearCredentials() {
        context.credentialsDataStore.edit { it.clear() }
    }
}
