package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.repository.PartRepository

class DeletePartUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(partId: Int) {
        partRepository.deletePart(partId)
    }
}