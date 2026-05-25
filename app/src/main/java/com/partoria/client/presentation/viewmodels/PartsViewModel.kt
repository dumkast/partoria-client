package com.partoria.client.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.domain.model.Filter
import com.partoria.client.domain.model.FilterMeta
import com.partoria.client.domain.usecase.part.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PartsViewModel(
    private val getAllPartsUseCase: GetAllPartsUseCase,
    private val getPartByIdUseCase: GetPartByIdUseCase,
    private val getPartWithDetailsUseCase: GetPartWithDetailsUseCase,
    private val getFilteredPartsUseCase: GetFilteredPartsUseCase,
    private val getFiltersMetaUseCase: GetFiltersMetaUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val searchPartsUseCase: SearchPartsUseCase
) : ViewModel() {

    private val _partsState = MutableStateFlow<PartsUiState>(PartsUiState.Loading)
    val partsState: StateFlow<PartsUiState> = _partsState.asStateFlow()

    private val _favoritesState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val favoritesState: StateFlow<FavoritesUiState> = _favoritesState.asStateFlow()

    private val _filtersMetaState = MutableStateFlow<FiltersMetaUiState>(FiltersMetaUiState.Loading)
    val filtersMetaState: StateFlow<FiltersMetaUiState> = _filtersMetaState.asStateFlow()

    private val _currentFilter = MutableStateFlow<Filter?>(null)
    val currentFilter: StateFlow<Filter?> = _currentFilter.asStateFlow()

    fun loadParts() {
        viewModelScope.launch {
            _partsState.value = PartsUiState.Loading
            try {
                _currentFilter.value = null
                val parts = getAllPartsUseCase()
                _partsState.value = PartsUiState.Success(parts)
            } catch (e: Exception) {
                _partsState.value = PartsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadFilteredParts(filter: Filter) {
        viewModelScope.launch {
            _partsState.value = PartsUiState.Loading
            try {
                _currentFilter.value = filter
                val parts = getFilteredPartsUseCase(filter)
                _partsState.value = PartsUiState.Success(parts)
            } catch (e: Exception) {
                _partsState.value = PartsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchParts(query: String) {
        viewModelScope.launch {
            _partsState.value = PartsUiState.Loading
            try {
                _currentFilter.value = null
                val parts = searchPartsUseCase(query)
                _partsState.value = PartsUiState.Success(parts)
            } catch (e: Exception) {
                _partsState.value = PartsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetFilters() {
        loadParts()
    }

    fun loadPartDetails(partId: Int, onResult: (ComputerPart?) -> Unit) {
        viewModelScope.launch {
            try {
                val part = getPartWithDetailsUseCase(partId)
                onResult(part)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favoritesState.value = FavoritesUiState.Loading
            try {
                val favorites = getFavoritesUseCase()
                _favoritesState.value = FavoritesUiState.Success(favorites)
            } catch (e: Exception) {
                _favoritesState.value = FavoritesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addToFavorites(partId: Int) {
        viewModelScope.launch {
            try {
                addToFavoritesUseCase(partId)
                loadFavorites()
            } catch (e: Exception) {
            }
        }
    }

    fun removeFromFavorites(partId: Int) {
        viewModelScope.launch {
            try {
                removeFromFavoritesUseCase(partId)
                loadFavorites()
            } catch (e: Exception) {
            }
        }
    }

    fun loadFiltersMeta() {
        viewModelScope.launch {
            _filtersMetaState.value = FiltersMetaUiState.Loading
            try {
                val meta = getFiltersMetaUseCase()
                _filtersMetaState.value = FiltersMetaUiState.Success(meta)
            } catch (e: Exception) {
                _filtersMetaState.value = FiltersMetaUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun isFavorite(partId: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val favorites = getFavoritesUseCase()
                onResult(favorites.any { it.id == partId })
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}

sealed class PartsUiState {
    object Loading : PartsUiState()
    data class Success(val parts: List<ComputerPart>) : PartsUiState()
    data class Error(val message: String) : PartsUiState()
}

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<ComputerPart>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}

sealed class FiltersMetaUiState {
    object Loading : FiltersMetaUiState()
    data class Success(val meta: FilterMeta) : FiltersMetaUiState()
    data class Error(val message: String) : FiltersMetaUiState()
}