package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.repository.PartRepository

class GetFavoritesUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(): List<ComputerPart> {
        return partRepository.getFavorites()
    }
}