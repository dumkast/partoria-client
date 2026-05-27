package com.partoria.client.domain.usecase.part

import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.domain.repository.PartRepository

class UpdatePartUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(
        id: Int,
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>
    ) {
        require(name.isNotBlank()) { "Name cannot be empty" }
        require(price > 0) { "Price must be positive" }
        partRepository.updatePart(id, name, category, brand, price, specs, releaseYear, details)
    }
}