package com.partoria.client.domain.usecase.part

import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.domain.repository.PartRepository

class CreatePartUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    ): Int {
        require(name.isNotBlank()) { "Name cannot be empty" }
        require(price > 0) { "Price must be positive" }
        return partRepository.createPart(name, category, brand, price, specs, releaseYear, details)
    }
}