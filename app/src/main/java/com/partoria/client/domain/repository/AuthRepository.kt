package com.partoria.client.domain.repository

import com.partoria.client.domain.model.AuthUser
import com.partoria.client.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthUser?
    suspend fun register(username: String, password: String): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun saveAuthData(token: String, username: String)
    suspend fun clearAuthData()
    suspend fun getToken(): String?
    fun isLoggedIn(): kotlinx.coroutines.flow.Flow<Boolean>
}