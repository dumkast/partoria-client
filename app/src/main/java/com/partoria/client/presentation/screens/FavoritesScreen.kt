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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.presentation.components.PartCard
import com.partoria.client.presentation.viewmodels.FavoritesUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.favorites),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundStart
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
                            AppColors.BackgroundStart,
                            AppColors.BackgroundEnd
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when (val state = favoritesState) {
                is FavoritesUiState.Success -> {
                    if (state.favorites.isEmpty()) {
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
                                        Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(AppDimens.IconSizeXLarge),
                                        tint = AppColors.TextHint
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                    Text(
                                        text = stringResource(R.string.no_favorites_yet),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                    Text(
                                        text = stringResource(R.string.favorites_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = AppDimens.PaddingLarge,
                                end = AppDimens.PaddingLarge,
                                top = AppDimens.PaddingSmall,
                                bottom = AppDimens.BottomNavPaddingLarge
                            ),
                            verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.favorites) { part ->
                                PartCard(
                                    part = part,
                                    onClick = { onPartClick(part.id) },
                                    isFavorite = true,
                                    onFavoriteClick = {
                                        partsViewModel.removeFromFavorites(part.id)
                                    }
                                )
                            }
                        }
                    }
                }
                is FavoritesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.Primary
                        )
                    }
                }
                is FavoritesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppDimens.PaddingXXLarge),
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
                                    Icons.Default.Favorite,
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
                                    onClick = { partsViewModel.loadFavorites() },
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