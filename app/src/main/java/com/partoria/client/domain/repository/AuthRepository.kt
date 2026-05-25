package com.partoria.client.domain.repository

import com.partoria.client.domain.model.AuthUser
import com.partoria.client.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthUser?
    suspend fun register(username: String, password: String): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun saveAuthData(token: String, username: String, role: String)
    suspend fun clearAuthData()
    suspend fun getToken(): String?
    fun getUserRole(): Flow<String?>
    fun getUsername(): Flow<String?>
    fun isLoggedIn(): Flow<Boolean>
}