package com.partoria.client.domain.usecase.part

import com.partoria.client.domain.model.FilterMeta
import com.partoria.client.domain.repository.PartRepository

class GetFiltersMetaUseCase(private val partRepository: PartRepository) {
    suspend operator fun invoke(): FilterMeta {
        return partRepository.getFiltersMeta()
    }
}