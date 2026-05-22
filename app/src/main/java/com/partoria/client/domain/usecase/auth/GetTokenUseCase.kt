package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository

class GetTokenUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): String? {
        return authRepository.getToken()
    }
}