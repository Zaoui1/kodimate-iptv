package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.EpgEntry
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EpgUiState {
    object Idle : EpgUiState()
    object Loading : EpgUiState()
    data class Success(val entries: List<EpgEntry>) : EpgUiState()
    data class Error(val message: String) : EpgUiState()
}

class EpgViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _state = MutableStateFlow<EpgUiState>(EpgUiState.Idle)
    val state: StateFlow<EpgUiState> = _state.asStateFlow()

    // Cache to avoid refetching on re-entry
    private val cache = mutableMapOf<Int, List<EpgEntry>>()

    fun loadEpg(credentials: UserCredentials, streamId: Int) {
        cache[streamId]?.let {
            _state.value = EpgUiState.Success(it)
            return
        }
        viewModelScope.launch {
            _state.value = EpgUiState.Loading
            try {
                val entries = repository.getEpg(credentials, streamId)
                cache[streamId] = entries
                _state.value = EpgUiState.Success(entries)
            } catch (e: Exception) {
                _state.value = EpgUiState.Error(e.message ?: "Failed to load EPG")
            }
        }
    }

    fun currentProgram(streamId: Int): EpgEntry? {
        val entries = cache[streamId] ?: return null
        val now = System.currentTimeMillis() / 1000
        return entries.firstOrNull { entry ->
            val start = entry.startTimestamp ?: return@firstOrNull false
            val stop = entry.stopTimestamp ?: return@firstOrNull false
            now in start..stop
        }
    }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EpgViewModel(repository) as T
    }
}
