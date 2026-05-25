package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository

class SaveAuthDataUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(token: String, username: String, role: String) {
        authRepository.saveAuthData(token, username, role)
    }
}