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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    partsViewModel: PartsViewModel,
    onPartClick: (Int) -> Unit,
    onFilterClick: () -> Unit
) {
    val partsState by partsViewModel.partsState.collectAsStateWithLifecycle()
    val currentFilter by partsViewModel.currentFilter.collectAsStateWithLifecycle()
    val favoritesState by partsViewModel.favoritesState.collectAsStateWithLifecycle()

    val isRefreshing by partsViewModel.isRefreshing.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    val favoritesIds = remember(favoritesState) {
        val state = favoritesState
        if (state is FavoritesUiState.Success) {
            state.favorites.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    val isFilterActive = currentFilter != null && (
            currentFilter?.categories?.isNotEmpty() == true ||
                    currentFilter?.brands?.isNotEmpty() == true ||
                    currentFilter?.minPrice != null ||
                    currentFilter?.maxPrice != null ||
                    currentFilter?.minYear != null ||
                    currentFilter?.maxYear != null ||
                    currentFilter?.sortBy != null
            )

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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    if (query.isNotEmpty()) {
                        partsViewModel.searchParts(query)
                    } else {
                        partsViewModel.loadParts()
                    }
                },
                placeholder = { Text("Search by name, brand...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            partsViewModel.loadParts()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 4.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { partsViewModel.loadParts() },
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
                                            partsViewModel.loadParts()
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
                        if (state is PartsUiState.Loading && partsState is PartsUiState.Loading && !isRefreshing) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

@Composable
fun PartCard(
    part: ComputerPart,
    onClick: () -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${part.brand} • ${part.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${part.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Released: ${part.releaseYear}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onFavoriteClick(isFavorite) }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}