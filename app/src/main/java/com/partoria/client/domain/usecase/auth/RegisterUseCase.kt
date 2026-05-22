package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Boolean {
        if (username.isBlank()) return false
        if (password.length < 6) return false
        return authRepository.register(username, password)
    }
}