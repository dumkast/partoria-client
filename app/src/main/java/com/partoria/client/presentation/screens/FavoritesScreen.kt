package com.partoria.client.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.presentation.viewmodels.FavoritesUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    partsViewModel: PartsViewModel,
    onPartClick: (Int) -> Unit
) {
    val favoritesState by partsViewModel.favoritesState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        partsViewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = favoritesState) {
                is FavoritesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is FavoritesUiState.Success -> {
                    if (state.favorites.isEmpty()) {
                        Text(
                            text = "No favorites yet",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = 6.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 90.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.favorites) { part ->
                                PartCard(
                                    part = part,
                                    onClick = { onPartClick(part.id) },
                                    isFavorite = true,
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
                is FavoritesUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { partsViewModel.loadFavorites() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}