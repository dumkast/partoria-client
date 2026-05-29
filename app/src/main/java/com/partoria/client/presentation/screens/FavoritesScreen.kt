package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.presentation.components.PartCard
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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Favorites",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E)
                )
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
                .padding(paddingValues)
        ) {
            when (val state = favoritesState) {
                is FavoritesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6C63FF)
                    )
                }
                is FavoritesUiState.Success -> {
                    if (state.favorites.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.Center)
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
                                    Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No favorites yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Parts you favorite will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
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
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
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
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFFF6B6B).copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { partsViewModel.loadFavorites() },
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