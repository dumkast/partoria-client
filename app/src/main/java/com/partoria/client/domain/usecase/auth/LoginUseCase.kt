package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.model.AuthUser
import com.partoria.client.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): AuthUser? {
        if (username.isBlank()) return null
        if (password.isBlank()) return null
        if (password.length < 6) return null
        return authRepository.login(username, password)
    }
}