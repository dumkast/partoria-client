package com.partoria.client.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partoria.client.domain.repository.AuthRepository
import com.partoria.client.domain.usecase.auth.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val saveAuthDataUseCase: SaveAuthDataUseCase,
    private val clearAuthDataUseCase: ClearAuthDataUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val getUserRoleUseCase: GetUserRoleUseCase,
    private val getUsernameUseCase: GetUsernameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _showRegistrationSuccess = MutableStateFlow(false)
    val showRegistrationSuccess: StateFlow<Boolean> = _showRegistrationSuccess.asStateFlow()

    fun setRegistrationSuccess(show: Boolean) {
        _showRegistrationSuccess.value = show
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = loginUseCase(username, password)
            if (result != null) {
                saveAuthDataUseCase(result.token, result.username, result.role)
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("Invalid username or password")
            }
        }
    }

    fun getUserRole(): Flow<String?> {
        return getUserRoleUseCase()
    }

    fun getUsername(): Flow<String?> {
        return getUsernameUseCase()
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

    fun isLoggedIn(): Flow<Boolean> {
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