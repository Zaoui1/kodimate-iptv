package com.iptvplayer.app.data.repository

import com.iptvplayer.app.data.api.XtreamApi
import com.iptvplayer.app.data.local.CredentialsDataStore
import com.iptvplayer.app.data.local.FavoritesDataStore
import com.iptvplayer.app.data.local.WatchHistoryDataStore
import com.iptvplayer.app.data.model.AuthResponse
import com.iptvplayer.app.data.model.Category
import com.iptvplayer.app.data.model.EpgEntry
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.SearchResults
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.SeriesInfoResponse
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.VodStream
import com.iptvplayer.app.data.model.WatchHistoryItem
import com.iptvplayer.app.data.model.WatchType
import com.iptvplayer.app.data.network.RetrofitClient
import kotlinx.coroutines.flow.Flow

class XtreamRepository(
    private val credentialsDataStore: CredentialsDataStore,
    private val favoritesDataStore: FavoritesDataStore,
    private val watchHistoryDataStore: WatchHistoryDataStore
) {

    val credentialsFlow: Flow<UserCredentials?> = credentialsDataStore.credentialsFlow
    val favoritesFlow: Flow<List<FavoriteItem>> = favoritesDataStore.favoritesFlow
    val watchHistoryFlow: Flow<List<WatchHistoryItem>> = watchHistoryDataStore.historyFlow

    private fun api(credentials: UserCredentials): XtreamApi =
        RetrofitClient.createApi(credentials.serverUrl)

    // ─── Auth ────────────────────────────────────────────────────────────────

    suspend fun login(serverUrl: String, username: String, password: String): AuthResponse {
        val api = RetrofitClient.createApi(serverUrl)
        val response = api.authenticate(username, password)
        credentialsDataStore.saveCredentials(serverUrl, username, password)
        return response
    }

    suspend fun logout() = credentialsDataStore.clearCredentials()

    // ─── Live TV ─────────────────────────────────────────────────────────────

    suspend fun getLiveCategories(credentials: UserCredentials): List<Category> =
        api(credentials).getLiveCategories(
            credentials.username, credentials.password, "get_live_categories"
        )

    suspend fun getLiveStreams(credentials: UserCredentials, categoryId: String? = null): List<LiveStream> =
        api(credentials).getLiveStreams(
            credentials.username, credentials.password, "get_live_streams", categoryId
        )

    fun buildLiveStreamUrl(credentials: UserCredentials, streamId: Int): String =
        "${credentials.serverUrl}/${credentials.username}/${credentials.password}/$streamId"

    // ─── VOD ─────────────────────────────────────────────────────────────────

    suspend fun getVodCategories(credentials: UserCredentials): List<Category> =
        api(credentials).getVodCategories(
            credentials.username, credentials.password, "get_vod_categories"
        )

    suspend fun getVodStreams(credentials: UserCredentials, categoryId: String? = null): List<VodStream> =
        api(credentials).getVodStreams(
            credentials.username, credentials.password, "get_vod_streams", categoryId
        )

    fun buildVodStreamUrl(credentials: UserCredentials, streamId: Int, ext: String): String =
        "${credentials.serverUrl}/movie/${credentials.username}/${credentials.password}/$streamId.$ext"

    // ─── Series ──────────────────────────────────────────────────────────────

    suspend fun getSeriesCategories(credentials: UserCredentials): List<Category> =
        api(credentials).getSeriesCategories(
            credentials.username, credentials.password, "get_series_categories"
        )

    suspend fun getSeries(credentials: UserCredentials, categoryId: String? = null): List<Series> =
        api(credentials).getSeries(
            credentials.username, credentials.password, "get_series", categoryId
        )

    suspend fun getSeriesInfo(credentials: UserCredentials, seriesId: Int): SeriesInfoResponse =
        api(credentials).getSeriesInfo(
            credentials.username, credentials.password, "get_series_info", seriesId
        )

    fun buildEpisodeUrl(credentials: UserCredentials, episodeId: String, ext: String): String =
        "${credentials.serverUrl}/series/${credentials.username}/${credentials.password}/$episodeId.$ext"

    // ─── EPG ─────────────────────────────────────────────────────────────────

    suspend fun getEpg(credentials: UserCredentials, streamId: Int): List<EpgEntry> {
        val response = api(credentials).getEpg(
            credentials.username, credentials.password, "get_simple_data_table", streamId
        )
        return response.epgListings ?: emptyList()
    }

    // ─── Favorites ───────────────────────────────────────────────────────────

    suspend fun addFavorite(item: FavoriteItem) = favoritesDataStore.addFavorite(item)
    suspend fun removeFavorite(id: String) = favoritesDataStore.removeFavorite(id)

    // ─── Watch History ───────────────────────────────────────────────────────

    suspend fun saveWatchPosition(
        id: String,
        title: String,
        imageUrl: String?,
        type: WatchType,
        streamId: Int,
        containerExtension: String?,
        positionMs: Long,
        durationMs: Long
    ) = watchHistoryDataStore.savePosition(
        id, title, imageUrl, type, streamId, containerExtension, positionMs, durationMs
    )

    suspend fun removeWatchHistoryItem(id: String) = watchHistoryDataStore.removeItem(id)

    // ─── Search ──────────────────────────────────────────────────────────────

    suspend fun search(credentials: UserCredentials, query: String): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), emptyList(), emptyList())
        val q = query.trim()
        return SearchResults(
            channels = getLiveStreams(credentials).filter { it.name.contains(q, ignoreCase = true) },
            movies = getVodStreams(credentials).filter { it.name.contains(q, ignoreCase = true) },
            series = getSeries(credentials).filter { it.name.contains(q, ignoreCase = true) }
        )
    }
}
