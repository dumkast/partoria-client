package com.partoria.client.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.presentation.viewmodels.FavoritesUiState
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.utils.CategoryIcon
import com.partoria.client.presentation.components.SearchBar
import com.partoria.client.presentation.components.PartCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    partsViewModel: PartsViewModel,
    onPartClick: (Int) -> Unit,
    onFilterClick: () -> Unit
) {
    val partsState by partsViewModel.partsState.collectAsStateWithLifecycle()
    val activeFilter by partsViewModel.activeFilter.collectAsStateWithLifecycle()
    val favoritesState by partsViewModel.favoritesState.collectAsStateWithLifecycle()

    val isRefreshing by partsViewModel.isRefreshing.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(activeFilter.searchQuery) {
        searchQuery = activeFilter.searchQuery ?: ""
    }

    val favoritesIds = remember(favoritesState) {
        val state = favoritesState
        if (state is FavoritesUiState.Success) {
            state.favorites.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    val isFilterActive = activeFilter.categories?.isNotEmpty() == true ||
            activeFilter.brands?.isNotEmpty() == true ||
            activeFilter.minPrice != null ||
            activeFilter.maxPrice != null ||
            activeFilter.minYear != null ||
            activeFilter.maxYear != null ||
            activeFilter.sortBy != null

    LaunchedEffect(Unit) {
        partsViewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Computer Parts") },
                actions = {
                    if (isFilterActive) {
                        IconButton(onClick = { partsViewModel.resetFilters() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear filters")
                        }
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Filter",
                            tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    partsViewModel.updateSearchQuery(query)
                }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { partsViewModel.loadParts(isSwipe = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = partsState) {
                    is PartsUiState.Success -> {
                        if (state.parts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No parts found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            searchQuery = ""
                                            partsViewModel.updateSearchQuery("")
                                        }
                                    ) {
                                        Text("Clear search")
                                    }
                                } else if (isFilterActive) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { partsViewModel.resetFilters() }
                                    ) {
                                        Text("Clear filters")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 6.dp,
                                    bottom = 90.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.parts) { part ->
                                    PartCard(
                                        part = part,
                                        onClick = { onPartClick(part.id) },
                                        isFavorite = part.id in favoritesIds,
                                        onFavoriteClick = { isFavorite ->
                                            if (isFavorite) {
                                                partsViewModel.removeFromFavorites(part.id)
                                            } else {
                                                partsViewModel.addToFavorites(part.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is PartsUiState.Loading -> {
                        if (!isRefreshing) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is PartsUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { partsViewModel.loadParts() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}