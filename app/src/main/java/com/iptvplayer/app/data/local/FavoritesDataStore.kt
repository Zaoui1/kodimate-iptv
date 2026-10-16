package com.iptvplayer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favoritesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "iptv_favorites")

class FavoritesDataStore(private val context: Context) {

    private val gson = Gson()
    private val KEY_FAVORITES = stringPreferencesKey("favorites_json")

    val favoritesFlow: Flow<List<FavoriteItem>> = context.favoritesDataStore.data.map { prefs ->
        val json = prefs[KEY_FAVORITES] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<FavoriteItemDto>>() {}.type
            val dtos: List<FavoriteItemDto> = gson.fromJson(json, type)
            dtos.map { it.toFavoriteItem() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFavorite(item: FavoriteItem) {
        context.favoritesDataStore.edit { prefs ->
            val current = parseFavorites(prefs[KEY_FAVORITES])
            if (current.none { it.id == item.id }) {
                val updated = current + FavoriteItemDto.fromFavoriteItem(item)
                prefs[KEY_FAVORITES] = gson.toJson(updated)
            }
        }
    }

    suspend fun removeFavorite(id: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = parseFavorites(prefs[KEY_FAVORITES])
            val updated = current.filter { it.id != id }
            prefs[KEY_FAVORITES] = gson.toJson(updated)
        }
    }

    private fun parseFavorites(json: String?): List<FavoriteItemDto> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<FavoriteItemDto>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Internal DTO for JSON serialization
    private data class FavoriteItemDto(
        val id: String,
        val name: String,
        val imageUrl: String?,
        val type: String,
        val streamId: Int,
        val containerExtension: String?
    ) {
        fun toFavoriteItem() = FavoriteItem(
            id = id,
            name = name,
            imageUrl = imageUrl,
            type = FavoriteType.valueOf(type),
            streamId = streamId,
            containerExtension = containerExtension
        )

        companion object {
            fun fromFavoriteItem(item: FavoriteItem) = FavoriteItemDto(
                id = item.id,
                name = item.name,
                imageUrl = item.imageUrl,
                type = item.type.name,
                streamId = item.streamId,
                containerExtension = item.containerExtension
            )
        }
    }
}
