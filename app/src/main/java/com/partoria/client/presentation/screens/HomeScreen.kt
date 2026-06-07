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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.presentation.viewmodels.FavoritesUiState
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.presentation.components.SearchBar
import com.partoria.client.presentation.components.PartCard
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.computer_parts),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundStart
                ),
                actions = {
                    if (isFilterActive) {
                        IconButton(onClick = { partsViewModel.resetFilters() }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_filters),
                                tint = AppColors.Error
                            )
                        }
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter),
                            tint = if (isFilterActive) AppColors.Primary else AppColors.TextPrimary
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
                            AppColors.BackgroundStart,
                            AppColors.BackgroundEnd
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
                        .padding(AppDimens.PaddingLarge),
                    shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.CardBackground
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
                                        modifier = Modifier.width(AppDimens.CardWidthEmptyState),
                                        shape = RoundedCornerShape(AppDimens.CornerRadiusXLarge),
                                        colors = CardDefaults.cardColors(
                                            containerColor = AppColors.CardBackground
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(AppDimens.PaddingXXLarge),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(AppDimens.IconSizeXLarge),
                                                tint = AppColors.TextHint
                                            )
                                            Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                            Text(
                                                text = if (searchQuery.isNotEmpty())
                                                    stringResource(R.string.no_results, searchQuery)
                                                else
                                                    stringResource(R.string.no_parts_found),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = AppColors.TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                            Text(
                                                text = if (searchQuery.isNotEmpty())
                                                    stringResource(R.string.try_different_search)
                                                else
                                                    stringResource(R.string.check_back_later),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppColors.TextSecondary
                                            )
                                            if (searchQuery.isNotEmpty() || isFilterActive) {
                                                Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                                Button(
                                                    onClick = {
                                                        searchQuery = ""
                                                        partsViewModel.updateSearchQuery("")
                                                        partsViewModel.resetFilters()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = AppColors.ButtonBackground
                                                    ),
                                                    shape = RoundedCornerShape(AppDimens.CornerRadiusSmall)
                                                ) {
                                                    Text(stringResource(R.string.clear_all_filters), color = AppColors.TextPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        start = AppDimens.PaddingLarge,
                                        end = AppDimens.PaddingLarge,
                                        top = AppDimens.TopContentPadding,
                                        bottom = AppDimens.BottomNavPaddingLarge
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
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
                                        color = AppColors.Primary
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
                                    modifier = Modifier.width(AppDimens.CardWidthEmptyState),
                                    shape = RoundedCornerShape(AppDimens.CornerRadiusXLarge),
                                    colors = CardDefaults.cardColors(
                                        containerColor = AppColors.CardBackground
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(AppDimens.PaddingXXLarge),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.Error,
                                            contentDescription = null,
                                            modifier = Modifier.size(AppDimens.IconSizeXLarge),
                                            tint = AppColors.Error
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AppColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                        Button(
                                            onClick = { partsViewModel.loadParts() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AppColors.ButtonBackground
                                            ),
                                            shape = RoundedCornerShape(AppDimens.CornerRadiusSmall)
                                        ) {
                                            Text(stringResource(R.string.retry), color = AppColors.TextPrimary)
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