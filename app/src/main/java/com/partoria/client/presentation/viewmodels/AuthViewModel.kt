package com.partoria.client.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partoria.client.domain.usecase.auth.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val saveAuthDataUseCase: SaveAuthDataUseCase,
    private val clearAuthDataUseCase: ClearAuthDataUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = loginUseCase(username, password)
            if (result != null) {
                saveAuthDataUseCase(result.token, result.username)
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("Invalid username or password")
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val success = registerUseCase(username, password)
            if (success) {
                _uiState.value = AuthUiState.RegisterSuccess
            } else {
                _uiState.value = AuthUiState.Error("Username already exists or invalid data")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            clearAuthDataUseCase()
            _uiState.value = AuthUiState.Logout
        }
    }

    fun isLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean> {
        return isLoggedInUseCase()
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object RegisterSuccess : AuthUiState()
    object Logout : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}