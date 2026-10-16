package com.iptvplayer.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.iptvplayer.app.data.model.UserCredentials
import com.iptvplayer.app.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val credentials: UserCredentials) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(private val repository: XtreamRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val savedCredentials = repository.credentialsFlow

    fun login(serverUrl: String, username: String, password: String) {
        if (serverUrl.isBlank() || username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                repository.login(serverUrl.trim(), username.trim(), password.trim())
                _uiState.value = LoginUiState.Success(
                    UserCredentials(serverUrl.trim(), username.trim(), password.trim())
                )
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(
                    e.message ?: "Connection failed. Check server URL and credentials."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    class Factory(private val repository: XtreamRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(repository) as T
    }
}
