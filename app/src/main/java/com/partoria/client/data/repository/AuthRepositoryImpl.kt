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

//    override suspend fun login(username: String, password: String): AuthUser? {
//        return try {
//            val response = apiService.login(LoginRequest(username, password))
//            AuthUser(response.username, response.token)
//        } catch (e: Exception) {
//            null
//        }
//    }
    override suspend fun login(username: String, password: String): AuthUser? {
        return try {
            println("LOGIN ATTEMPT: $username")
            val response = apiService.login(LoginRequest(username, password))
            println("LOGIN SUCCESS: ${response.token}")
            AuthUser(response.username, response.token)
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

    override suspend fun getCurrentUser(): User? {
        val username = tokenDataStore.getUsername().firstOrNull()
        return if (username != null) {
            User(0, username, "user")
        } else {
            null
        }
    }

    override suspend fun saveAuthData(token: String, username: String) {
        tokenDataStore.saveToken(token, username)
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