package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Category
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.VodStream
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(
        val categories: List<Category>,
        val movies: List<VodStream>,
        val selectedCategoryId: String?
    ) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

class MoviesViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val favorites: StateFlow<List<FavoriteItem>> = _favorites.asStateFlow()

    private var credentials: UserCredentials? = null
    private var allMovies: List<VodStream> = emptyList()
    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { _favorites.value = it }
        }
    }

    fun load(creds: UserCredentials) {
        credentials = creds
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            try {
                allCategories = repository.getVodCategories(creds)
                allMovies = repository.getVodStreams(creds, null)
                _uiState.value = MoviesUiState.Success(
                    categories = allCategories,
                    movies = allMovies,
                    selectedCategoryId = null
                )
            } catch (e: Exception) {
                _uiState.value = MoviesUiState.Error(e.message ?: "Failed to load movies")
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        val current = _uiState.value as? MoviesUiState.Success ?: return
        val filtered = if (categoryId == null) allMovies
        else allMovies.filter { it.categoryId == categoryId }
        _uiState.value = current.copy(movies = filtered, selectedCategoryId = categoryId)
    }

    fun buildStreamUrl(movie: VodStream): String {
        val creds = credentials ?: return ""
        val ext = movie.containerExtension ?: "mkv"
        return repository.buildVodStreamUrl(creds, movie.streamId, ext)
    }

    fun toggleFavorite(movie: VodStream) {
        viewModelScope.launch {
            val favId = "vod_${movie.streamId}"
            if (_favorites.value.any { it.id == favId }) {
                repository.removeFavorite(favId)
            } else {
                repository.addFavorite(
                    FavoriteItem(
                        id = favId,
                        name = movie.name,
                        imageUrl = movie.streamIcon,
                        type = FavoriteType.VOD,
                        streamId = movie.streamId,
                        containerExtension = movie.containerExtension
                    )
                )
            }
        }
    }

    fun isFavorite(streamId: Int): Boolean =
        _favorites.value.any { it.id == "vod_$streamId" }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MoviesViewModel(repository) as T
    }
}
