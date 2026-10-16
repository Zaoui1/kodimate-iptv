package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Category
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import com.iptvplayer.app.data.model.LiveStream
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LiveTvUiState {
    object Loading : LiveTvUiState()
    data class Success(
        val categories: List<Category>,
        val streams: List<LiveStream>,
        val selectedCategoryId: String?
    ) : LiveTvUiState()
    data class Error(val message: String) : LiveTvUiState()
}

class LiveTvViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LiveTvUiState>(LiveTvUiState.Loading)
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val favorites: StateFlow<List<FavoriteItem>> = _favorites.asStateFlow()

    private var credentials: UserCredentials? = null
    private var allStreams: List<LiveStream> = emptyList()
    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { _favorites.value = it }
        }
    }

    fun load(creds: UserCredentials) {
        credentials = creds
        viewModelScope.launch {
            _uiState.value = LiveTvUiState.Loading
            try {
                allCategories = repository.getLiveCategories(creds)
                allStreams = repository.getLiveStreams(creds, null)
                _uiState.value = LiveTvUiState.Success(
                    categories = allCategories,
                    streams = allStreams,
                    selectedCategoryId = null
                )
            } catch (e: Exception) {
                _uiState.value = LiveTvUiState.Error(e.message ?: "Failed to load channels")
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        val current = _uiState.value as? LiveTvUiState.Success ?: return
        val filtered = if (categoryId == null) allStreams
        else allStreams.filter { it.categoryId == categoryId }
        _uiState.value = current.copy(streams = filtered, selectedCategoryId = categoryId)
    }

    fun buildStreamUrl(streamId: Int): String {
        val creds = credentials ?: return ""
        return repository.buildLiveStreamUrl(creds, streamId)
    }

    fun toggleFavorite(stream: LiveStream) {
        viewModelScope.launch {
            val favId = "live_${stream.streamId}"
            val isAlreadyFav = _favorites.value.any { it.id == favId }
            if (isAlreadyFav) {
                repository.removeFavorite(favId)
            } else {
                repository.addFavorite(
                    FavoriteItem(
                        id = favId,
                        name = stream.name,
                        imageUrl = stream.streamIcon,
                        type = FavoriteType.LIVE,
                        streamId = stream.streamId
                    )
                )
            }
        }
    }

    fun isFavorite(streamId: Int): Boolean =
        _favorites.value.any { it.id == "live_$streamId" }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LiveTvViewModel(repository) as T
    }
}
