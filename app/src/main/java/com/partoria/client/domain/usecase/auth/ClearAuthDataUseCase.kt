package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository

class ClearAuthDataUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() {
        authRepository.clearAuthData()
    }
}