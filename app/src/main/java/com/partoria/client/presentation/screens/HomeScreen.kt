package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.presentation.viewmodels.FavoritesUiState
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Computer Parts",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
                actions = {
                    if (isFilterActive) {
                        IconButton(onClick = { partsViewModel.resetFilters() }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear filters",
                                tint = Color(0xFFFF6B6B)
                            )
                        }
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (isFilterActive) Color(0xFF6C63FF) else Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SearchBar(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            partsViewModel.updateSearchQuery(query)
                        }
                    )
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { partsViewModel.loadParts(isSwipe = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = partsState) {
                        is PartsUiState.Success -> {
                            if (state.parts.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .width(280.dp)
                                            .clip(RoundedCornerShape(24.dp)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(64.dp),
                                                tint = Color.White.copy(alpha = 0.5f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = if (searchQuery.isNotEmpty())
                                                    "No results for \"$searchQuery\""
                                                else
                                                    "No parts found",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (searchQuery.isNotEmpty())
                                                    "Try a different search term"
                                                else
                                                    "Check back later for new parts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            if (searchQuery.isNotEmpty() || isFilterActive) {
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(
                                                    onClick = {
                                                        searchQuery = ""
                                                        partsViewModel.updateSearchQuery("")
                                                        partsViewModel.resetFilters()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF6C63FF)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Clear all filters")
                                                }
                                            }
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
                                    CircularProgressIndicator(
                                        color = Color(0xFF6C63FF)
                                    )
                                }
                            }
                        }
                        is PartsUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .clip(RoundedCornerShape(24.dp)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = Color(0xFFFF6B6B)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { partsViewModel.loadParts() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6C63FF)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Try Again")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}