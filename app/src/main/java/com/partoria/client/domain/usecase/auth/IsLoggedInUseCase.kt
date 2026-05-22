package com.partoria.client.domain.usecase.auth

import com.partoria.client.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class IsLoggedInUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}