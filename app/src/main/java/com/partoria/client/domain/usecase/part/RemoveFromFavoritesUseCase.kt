package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.repository.PartRepository

class RemoveFromFavoritesUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(partId: Int) {
        partRepository.removeFromFavorites(partId)
    }
}