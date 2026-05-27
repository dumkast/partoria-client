package com.partoria.client.domain.repository

import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.model.Filter
import com.partoria.client.domain.model.FilterMeta

interface PartRepository {
    suspend fun getAllParts(): List<ComputerPart>
    suspend fun getPartById(id: Int): ComputerPart?
    suspend fun getPartWithDetails(id: Int): ComputerPart?
    suspend fun getFilteredParts(filter: Filter): List<ComputerPart>
    suspend fun getFiltersMeta(): FilterMeta
    suspend fun addToFavorites(partId: Int)
    suspend fun removeFromFavorites(partId: Int)
    suspend fun getFavorites(): List<ComputerPart>
    suspend fun searchParts(query: String): List<ComputerPart>
    suspend fun deletePart(partId: Int)
    suspend fun createPart(
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    ): Int
    suspend fun updatePart(
        id: Int,
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    )
}