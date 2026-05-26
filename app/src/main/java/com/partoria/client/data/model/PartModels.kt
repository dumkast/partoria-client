package com.partoria.client.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PartResponse(
    val id: Int,
    val name: String,
    val category: String,
    val brand: String,
    val price: Double,
    val specs: String,
    val releaseYear: Int,
    val details: List<PartDetailResponse> = emptyList()
)

@Serializable
data class PartDetailResponse(
    val id: String,
    val specification: String,
    val value: String
)

@Serializable
data class FilterRequest(
    val searchQuery: String? = null,
    val categories: List<String>? = null,
    val brands: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val sortBy: String? = null,
    val sortDirection: String? = null
)

@Serializable
data class PartsResponse(
    val items: List<PartResponse>
)

@Serializable
data class FiltersMetaResponse(
    val categories: List<String>,
    val brands: List<String>,
    val priceRange: PriceRange,
    val yearRange: YearRange
)

@Serializable
data class PriceRange(
    val min: Double,
    val max: Double
)

@Serializable
data class YearRange(
    val min: Int,
    val max: Int
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)