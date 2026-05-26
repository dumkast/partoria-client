package com.partoria.client.data.api

import com.partoria.client.data.model.*

interface ApiService {
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun register(request: RegisterRequest): RegisterResponse
    suspend fun getAllParts(token: String): List<PartResponse>
    suspend fun getPartById(token: String, id: Int): PartResponse
    suspend fun getPartWithDetails(token: String, id: Int): PartResponse
    suspend fun getFilteredParts(token: String, filter: FilterRequest): PartsResponse
    suspend fun getFiltersMeta(token: String): FiltersMetaResponse
    suspend fun getFavorites(token: String): List<PartResponse>
    suspend fun addToFavorites(token: String, partId: Int)
    suspend fun removeFromFavorites(token: String, partId: Int)
    suspend fun searchParts(token: String, query: String): PartsResponse
    suspend fun deletePart(token: String, partId: Int)
    suspend fun createPart(token: String, part: CreatePartRequest): PartResponse
}