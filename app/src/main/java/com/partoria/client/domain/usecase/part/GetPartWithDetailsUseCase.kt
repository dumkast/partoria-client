package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.repository.PartRepository

class GetPartWithDetailsUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(id: Int): ComputerPart? {
        return partRepository.getPartWithDetails(id)
    }
}