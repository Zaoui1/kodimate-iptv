package com.iptvplayer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptvplayer.app.data.model.WatchHistoryItem
import com.iptvplayer.app.data.model.WatchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.watchHistoryDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "iptv_watch_history")

class WatchHistoryDataStore(private val context: Context) {

    private val gson = Gson()
    private val KEY_HISTORY = stringPreferencesKey("watch_history_json")
    private val MAX_ITEMS = 50

    val historyFlow: Flow<List<WatchHistoryItem>> = context.watchHistoryDataStore.data.map { prefs ->
        parseHistory(prefs[KEY_HISTORY])
            .map { it.toWatchHistoryItem() }
            .sortedByDescending { it.timestamp }
    }

    suspend fun savePosition(
        id: String,
        title: String,
        imageUrl: String?,
        type: WatchType,
        streamId: Int,
        containerExtension: String?,
        positionMs: Long,
        durationMs: Long
    ) {
        context.watchHistoryDataStore.edit { prefs ->
            val current = parseHistory(prefs[KEY_HISTORY]).toMutableList()
            current.removeAll { it.id == id }
            current.add(
                0,
                WatchHistoryDto(
                    id = id,
                    title = title,
                    imageUrl = imageUrl,
                    type = type.name,
                    streamId = streamId,
                    containerExtension = containerExtension,
                    positionMs = positionMs,
                    durationMs = durationMs,
                    timestamp = System.currentTimeMillis()
                )
            )
            prefs[KEY_HISTORY] = gson.toJson(current.take(MAX_ITEMS))
        }
    }

    suspend fun removeItem(id: String) {
        context.watchHistoryDataStore.edit { prefs ->
            val current = parseHistory(prefs[KEY_HISTORY]).toMutableList()
            current.removeAll { it.id == id }
            prefs[KEY_HISTORY] = gson.toJson(current)
        }
    }

    private fun parseHistory(json: String?): List<WatchHistoryDto> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<WatchHistoryDto>>() {}.type
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private data class WatchHistoryDto(
        val id: String,
        val title: String,
        val imageUrl: String?,
        val type: String,
        val streamId: Int,
        val containerExtension: String?,
        val positionMs: Long,
        val durationMs: Long,
        val timestamp: Long
    ) {
        fun toWatchHistoryItem() = WatchHistoryItem(
            id = id,
            title = title,
            imageUrl = imageUrl,
            type = WatchType.valueOf(type),
            streamId = streamId,
            containerExtension = containerExtension,
            positionMs = positionMs,
            durationMs = durationMs,
            timestamp = timestamp
        )
    }
}
