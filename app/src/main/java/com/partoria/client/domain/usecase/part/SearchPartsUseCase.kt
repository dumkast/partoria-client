package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.repository.PartRepository

class SearchPartsUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(query: String): List<ComputerPart> {
        require(query.isNotBlank()) { "Search query cannot be empty" }
        return partRepository.searchParts(query)
    }
}