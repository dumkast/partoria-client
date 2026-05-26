package com.partoria.client.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.partoria.client.data.model.PartDetailRequest
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
    private val searchPartsUseCase: SearchPartsUseCase,
    private val deletePartUseCase: DeletePartUseCase,
    private val createPartUseCase: CreatePartUseCase
) : ViewModel() {

    private val _partsState = MutableStateFlow<PartsUiState>(PartsUiState.Loading)
    val partsState: StateFlow<PartsUiState> = _partsState.asStateFlow()

    private val _favoritesState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val favoritesState: StateFlow<FavoritesUiState> = _favoritesState.asStateFlow()

    private val _filtersMetaState = MutableStateFlow<FiltersMetaUiState>(FiltersMetaUiState.Loading)
    val filtersMetaState: StateFlow<FiltersMetaUiState> = _filtersMetaState.asStateFlow()

    private val _activeFilter = MutableStateFlow(Filter())
    val activeFilter: StateFlow<Filter> = _activeFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadParts()
    }

    fun loadParts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val filter = _activeFilter.value
                val isEmpty = filter.searchQuery.isNullOrBlank() &&
                        filter.categories.isNullOrEmpty() &&
                        filter.brands.isNullOrEmpty() &&
                        filter.minPrice == null && filter.maxPrice == null &&
                        filter.minYear == null && filter.maxYear == null &&
                        filter.sortBy == null

                val parts = if (isEmpty) {
                    getAllPartsUseCase()
                } else {
                    getFilteredPartsUseCase(filter)
                }
                _partsState.value = PartsUiState.Success(parts)
            } catch (e: Exception) {
                _partsState.value = PartsUiState.Error(e.message ?: "Unknown error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun loadFilteredParts(filter: Filter) {
        val currentSearch = _activeFilter.value.searchQuery
        _activeFilter.value = filter.copy(searchQuery = currentSearch)
        loadParts()
    }

    fun updateSearchQuery(query: String) {
        _activeFilter.value = _activeFilter.value.copy(
            searchQuery = query.takeIf { it.isNotBlank() }
        )
        loadParts()
    }

    fun searchParts(query: String) {
        updateSearchQuery(query)
    }

    fun resetFilters() {
        val currentSearch = _activeFilter.value.searchQuery
        _activeFilter.value = Filter(searchQuery = currentSearch)
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
                val currentState = _favoritesState.value
                if (currentState is FavoritesUiState.Success) {
                    val currentFavorites = currentState.favorites.toMutableList()
                    val part = partsState.value.let { state ->
                        if (state is PartsUiState.Success) {
                            state.parts.find { it.id == partId }
                        } else null
                    }
                    part?.let {
                        currentFavorites.add(it)
                        _favoritesState.value = FavoritesUiState.Success(currentFavorites)
                    }
                } else {
                    loadFavorites()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun removeFromFavorites(partId: Int) {
        viewModelScope.launch {
            try {
                removeFromFavoritesUseCase(partId)
                val currentState = _favoritesState.value
                if (currentState is FavoritesUiState.Success) {
                    val currentFavorites = currentState.favorites.filter { it.id != partId }
                    _favoritesState.value = FavoritesUiState.Success(currentFavorites)
                } else {
                    loadFavorites()
                }
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

    fun deletePart(partId: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                deletePartUseCase(partId)
                loadParts()
                loadFavorites()
                onSuccess()
            } catch (e: Exception) {
            }
        }
    }

    fun createPart(
        name: String,
        category: String,
        brand: String,
        price: Double,
        specs: String,
        releaseYear: Int,
        details: List<PartDetailRequest>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                createPartUseCase(name, category, brand, price, specs, releaseYear, details)
                loadParts()
                onSuccess()
            } catch (e: Exception) {
                onError()
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