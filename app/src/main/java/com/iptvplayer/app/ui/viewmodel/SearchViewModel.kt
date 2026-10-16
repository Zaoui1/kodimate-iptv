package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.SearchResults
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.model.VodStream
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: SearchResults) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@OptIn(FlowPreview::class)
class SearchViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var credentials: UserCredentials? = null

    init {
        viewModelScope.launch {
            _query.debounce(400L).distinctUntilChanged().collect { q ->
                if (q.isBlank()) {
                    _state.value = SearchUiState.Idle
                } else {
                    performSearch(q)
                }
            }
        }
    }

    fun setCredentials(creds: UserCredentials) {
        credentials = creds
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun clearQuery() {
        _query.value = ""
        _state.value = SearchUiState.Idle
    }

    private fun performSearch(query: String) {
        val creds = credentials ?: return
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val results = repository.search(creds, query)
                _state.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                _state.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun buildVodUrl(movie: VodStream): String {
        val creds = credentials ?: return ""
        return repository.buildVodStreamUrl(creds, movie.streamId, movie.containerExtension ?: "mkv")
    }

    fun buildLiveUrl(streamId: Int): String {
        val creds = credentials ?: return ""
        return repository.buildLiveStreamUrl(creds, streamId)
    }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SearchViewModel(repository) as T
    }
}
