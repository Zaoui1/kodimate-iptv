package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.Category
import com.iptvplayer.app.data.model.Episode
import com.iptvplayer.app.data.model.FavoriteItem
import com.iptvplayer.app.data.model.FavoriteType
import com.iptvplayer.app.data.model.Series
import com.iptvplayer.app.data.model.SeriesInfoResponse
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SeriesListUiState {
    object Loading : SeriesListUiState()
    data class Success(
        val categories: List<Category>,
        val seriesList: List<Series>,
        val selectedCategoryId: String?
    ) : SeriesListUiState()
    data class Error(val message: String) : SeriesListUiState()
}

sealed class SeriesDetailUiState {
    object Idle : SeriesDetailUiState()
    object Loading : SeriesDetailUiState()
    data class Success(
        val series: Series,
        val info: SeriesInfoResponse,
        val selectedSeason: Int
    ) : SeriesDetailUiState()
    data class Error(val message: String) : SeriesDetailUiState()
}

class SeriesViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _listState = MutableStateFlow<SeriesListUiState>(SeriesListUiState.Loading)
    val listState: StateFlow<SeriesListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<SeriesDetailUiState>(SeriesDetailUiState.Idle)
    val detailState: StateFlow<SeriesDetailUiState> = _detailState.asStateFlow()

    private val _favorites = MutableStateFlow<List<FavoriteItem>>(emptyList())
    val favorites: StateFlow<List<FavoriteItem>> = _favorites.asStateFlow()

    private var credentials: UserCredentials? = null
    private var allSeries: List<Series> = emptyList()
    private var allCategories: List<Category> = emptyList()

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { _favorites.value = it }
        }
    }

    fun loadList(creds: UserCredentials) {
        credentials = creds
        viewModelScope.launch {
            _listState.value = SeriesListUiState.Loading
            try {
                allCategories = repository.getSeriesCategories(creds)
                allSeries = repository.getSeries(creds, null)
                _listState.value = SeriesListUiState.Success(
                    categories = allCategories,
                    seriesList = allSeries,
                    selectedCategoryId = null
                )
            } catch (e: Exception) {
                _listState.value = SeriesListUiState.Error(e.message ?: "Failed to load series")
            }
        }
    }

    fun selectCategory(categoryId: String?) {
        val current = _listState.value as? SeriesListUiState.Success ?: return
        val filtered = if (categoryId == null) allSeries
        else allSeries.filter { it.categoryId == categoryId }
        _listState.value = current.copy(seriesList = filtered, selectedCategoryId = categoryId)
    }

    fun loadSeriesDetail(series: Series) {
        val creds = credentials ?: return
        viewModelScope.launch {
            _detailState.value = SeriesDetailUiState.Loading
            try {
                val info = repository.getSeriesInfo(creds, series.seriesId)
                val firstSeason = info.episodes?.keys?.mapNotNull { it.toIntOrNull() }?.minOrNull() ?: 1
                _detailState.value = SeriesDetailUiState.Success(
                    series = series,
                    info = info,
                    selectedSeason = firstSeason
                )
            } catch (e: Exception) {
                _detailState.value = SeriesDetailUiState.Error(e.message ?: "Failed to load series details")
            }
        }
    }

    fun selectSeason(season: Int) {
        val current = _detailState.value as? SeriesDetailUiState.Success ?: return
        _detailState.value = current.copy(selectedSeason = season)
    }

    fun buildEpisodeUrl(episode: Episode): String {
        val creds = credentials ?: return ""
        val ext = episode.containerExtension ?: "mkv"
        return repository.buildEpisodeUrl(creds, episode.id, ext)
    }

    fun toggleFavorite(series: Series) {
        viewModelScope.launch {
            val favId = "series_${series.seriesId}"
            if (_favorites.value.any { it.id == favId }) {
                repository.removeFavorite(favId)
            } else {
                repository.addFavorite(
                    FavoriteItem(
                        id = favId,
                        name = series.name,
                        imageUrl = series.cover,
                        type = FavoriteType.SERIES,
                        streamId = series.seriesId
                    )
                )
            }
        }
    }

    fun isFavorite(seriesId: Int): Boolean =
        _favorites.value.any { it.id == "series_$seriesId" }

    fun clearDetail() {
        _detailState.value = SeriesDetailUiState.Idle
    }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SeriesViewModel(repository) as T
    }
}
