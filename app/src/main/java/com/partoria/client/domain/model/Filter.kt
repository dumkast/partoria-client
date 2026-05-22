package com.partoria.client.domain.model

data class Filter(
    val categories: List<String>? = null,
    val brands: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val sortBy: String? = null,
    val sortDirection: String? = null,
    val page: Int = 1,
    val pageSize: Int = 100
)

data class FilterMeta(
    val categories: List<String>,
    val brands: List<String>,
    val priceRange: PriceRange,
    val yearRange: YearRange
)

data class PriceRange(
    val min: Double,
    val max: Double
)

data class YearRange(
    val min: Int,
    val max: Int
)