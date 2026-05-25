package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetUsernameUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<String?> {
        return authRepository.getUsername()
    }
}