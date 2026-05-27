package com.partoria.client.data.repository

import com.partoria.client.data.api.ApiService
import com.partoria.client.data.model.CreatePartRequest
import com.partoria.client.data.model.FilterRequest
import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.data.model.PartResponse
import com.partoria.client.data.model.UpdatePartRequest
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.model.Filter
import com.partoria.client.domain.model.FilterMeta
import com.partoria.client.domain.model.PartDetail
import com.partoria.client.domain.model.PriceRange
import com.partoria.client.domain.model.YearRange
import com.partoria.client.domain.repository.PartRepository

class PartRepositoryImpl(
    private val apiService: ApiService,
    private val tokenProvider: suspend () -> String?
) : PartRepository {

    private suspend fun getToken(): String {
        return tokenProvider() ?: throw Exception("No token available")
    }

    override suspend fun getAllParts(): List<ComputerPart> {
        val token = getToken()
        val response = apiService.getAllParts(token)
        return response.map { it.toDomain() }
    }

    override suspend fun getPartById(id: Int): ComputerPart? {
        val token = getToken()
        return try {
            apiService.getPartById(token, id).toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPartWithDetails(id: Int): ComputerPart? {
        val token = getToken()
        return try {
            apiService.getPartWithDetails(token, id).toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getFilteredParts(filter: Filter): List<ComputerPart> {
        println("FILTER REQUEST: $filter")
        val token = getToken()
        val request = FilterRequest(
            searchQuery = filter.searchQuery,
            categories = filter.categories,
            brands = filter.brands,
            minPrice = filter.minPrice,
            maxPrice = filter.maxPrice,
            minYear = filter.minYear,
            maxYear = filter.maxYear,
            sortBy = filter.sortBy,
            sortDirection = filter.sortDirection
        )
        val response = apiService.getFilteredParts(token, request)
        println("FILTER RESPONSE: ${response.items.size} items")
        return response.items.map { it.toDomain() }
    }

    override suspend fun getFiltersMeta(): FilterMeta {
        val token = getToken()
        val response = apiService.getFiltersMeta(token)
        return FilterMeta(
            categories = response.categories,
            brands = response.brands,
            priceRange = PriceRange(
                min = response.priceRange.min,
                max = response.priceRange.max
            ),
            yearRange = YearRange(
                min = response.yearRange.min,
                max = response.yearRange.max
            )
        )
    }

    override suspend fun addToFavorites(partId: Int) {
        val token = getToken()
        apiService.addToFavorites(token, partId)
    }

    override suspend fun removeFromFavorites(partId: Int) {
        val token = getToken()
        apiService.removeFromFavorites(token, partId)
    }

    override suspend fun getFavorites(): List<ComputerPart> {
        val token = getToken()
        val response = apiService.getFavorites(token)
        return response.map { it.toDomain() }
    }

    override suspend fun searchParts(query: String): List<ComputerPart> {
        println("SEARCH REQUEST: $query")
        val token = getToken()
        val response = apiService.searchParts(token, query)
        return response.items.map { it.toDomain() }
    }

    override suspend fun deletePart(partId: Int) {
        val token = getToken()
        apiService.deletePart(token, partId)
    }

    override suspend fun createPart(
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    ): Int {
        val token = getToken()
        val request = CreatePartRequest(
            name = name,
            category = category,
            brand = brand,
            price = price,
            specs = specs,
            releaseYear = releaseYear,
            details = details
        )
        return apiService.createPart(token, request)
    }

    private fun PartResponse.toDomain(): ComputerPart {
        return ComputerPart(
            id = id,
            name = name,
            category = category,
            brand = brand,
            price = price,
            specs = specs,
            releaseYear = releaseYear,
            details = details.map { detail ->
                PartDetail(
                    id = detail.id,
                    specification = detail.specification,
                    value = detail.value
                )
            }
        )
    }

    override suspend fun updatePart(
        id: Int,
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    ) {
        val token = getToken()
        val request = UpdatePartRequest(
            id = id,
            name = name,
            category = category,
            brand = brand,
            price = price,
            specs = specs,
            releaseYear = releaseYear,
            details = details
        )
        apiService.updatePart(token, request)
    }
}