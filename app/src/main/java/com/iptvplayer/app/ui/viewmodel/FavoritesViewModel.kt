package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.WatchHistoryItem
import com.iptvplayer.app.data.model.WatchType
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val liveItems: List<FavoriteItem> = emptyList(),
    val vodItems: List<FavoriteItem> = emptyList(),
    val seriesItems: List<FavoriteItem> = emptyList()
)

class FavoritesViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _watchHistory = MutableStateFlow<List<WatchHistoryItem>>(emptyList())
    val watchHistory: StateFlow<List<WatchHistoryItem>> = _watchHistory.asStateFlow()

    private var credentials: UserCredentials? = null

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { items ->
                _uiState.value = FavoritesUiState(
                    liveItems = items.filter { it.type == FavoriteType.LIVE },
                    vodItems = items.filter { it.type == FavoriteType.VOD },
                    seriesItems = items.filter { it.type == FavoriteType.SERIES }
                )
            }
        }
        viewModelScope.launch {
            repository.watchHistoryFlow.collect { _watchHistory.value = it }
        }
    }

    fun setCredentials(creds: UserCredentials) {
        credentials = creds
    }

    fun buildStreamUrl(item: FavoriteItem): String {
        val creds = credentials ?: return ""
        return when (item.type) {
            FavoriteType.LIVE -> repository.buildLiveStreamUrl(creds, item.streamId)
            FavoriteType.VOD -> repository.buildVodStreamUrl(creds, item.streamId, item.containerExtension ?: "mkv")
            FavoriteType.SERIES -> ""
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch { repository.removeFavorite(id) }
    }

    fun removeWatchHistoryItem(id: String) {
        viewModelScope.launch { repository.removeWatchHistoryItem(id) }
    }

    fun buildWatchHistoryUrl(item: WatchHistoryItem): String {
        val creds = credentials ?: return ""
        val ext = item.containerExtension ?: "mkv"
        return when (item.type) {
            WatchType.VOD -> repository.buildVodStreamUrl(creds, item.streamId, ext)
            WatchType.EPISODE -> repository.buildEpisodeUrl(creds, item.streamId.toString(), ext)
        }
    }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            FavoritesViewModel(repository) as T
    }
}
