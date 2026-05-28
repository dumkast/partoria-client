package com.partoria.client.data.repository

import com.partoria.client.data.api.ApiService
import com.partoria.client.data.datastore.TokenDataStore
import com.partoria.client.data.model.LoginRequest
import com.partoria.client.data.model.RegisterRequest
import com.partoria.client.domain.model.AuthUser
import com.partoria.client.domain.model.User
import com.partoria.client.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): AuthUser? {
        return try {
            println("LOGIN ATTEMPT: $username")
            val response = apiService.login(LoginRequest(username, password))
            println("LOGIN SUCCESS: ${response.token}")
            AuthUser(response.username, response.token, response.role)
        } catch (e: Exception) {
            println("LOGIN ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun register(username: String, password: String): Boolean {
        return try {
            println("REGISTER ATTEMPT: $username")
            apiService.register(RegisterRequest(username, password))
            println("REGISTER SUCCESS")
            true
        } catch (e: Exception) {
            println("REGISTER ERROR: ${e.message}")
            false
        }
    }

    override suspend fun saveAuthData(token: String, username: String, role: String) {
        tokenDataStore.saveToken(token, username, role)
    }

    override fun getUserRole(): Flow<String?> {
        return tokenDataStore.getRole()
    }

    override fun getUsername(): Flow<String?> {
        return tokenDataStore.getUsername()
    }

    override suspend fun clearAuthData() {
        tokenDataStore.clearToken()
    }

    override suspend fun getToken(): String? {
        return tokenDataStore.getToken().firstOrNull()
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return tokenDataStore.getToken().map { token ->
            token != null && token.isNotEmpty()
        }
    }
}