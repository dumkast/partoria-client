package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.model.Filter
import com.partoria.client.domain.repository.PartRepository

class GetFilteredPartsUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(filter: Filter): List<ComputerPart> {
        return partRepository.getFilteredParts(filter)
    }
}