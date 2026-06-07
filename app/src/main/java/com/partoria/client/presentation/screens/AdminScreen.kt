package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.presentation.components.PartCard
import com.partoria.client.presentation.components.SearchBar
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    partsViewModel: PartsViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit
) {
    val adminPartsState by partsViewModel.adminPartsState.collectAsStateWithLifecycle()
    val isRefreshing by partsViewModel.isRefreshing.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<ComputerPart?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val searchQuery by partsViewModel.adminSearchQuery.collectAsStateWithLifecycle()
    val allParts = (adminPartsState as? PartsUiState.Success)?.parts ?: emptyList()

    val filtersMetaState by partsViewModel.filtersMetaState.collectAsStateWithLifecycle()
    val categories = (filtersMetaState as? FiltersMetaUiState.Success)?.meta?.categories ?: emptyList()
    val adminFilter by partsViewModel.adminFilter.collectAsStateWithLifecycle()
    val selectedCategory = adminFilter.categories?.firstOrNull()

    LaunchedEffect(Unit) {
        if (filtersMetaState is FiltersMetaUiState.Loading) {
            partsViewModel.loadFiltersMeta()
        }
    }

    LaunchedEffect(Unit) {
        partsViewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundEnd,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = AppDimens.SnackbarBottomOffset)
            )
        },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(end = AppDimens.PaddingLarge),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(AppDimens.IconSizeNormal)
                        )
                        Text(
                            "${stringResource(R.string.admin_panel)} (${allParts.size})",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    FloatingActionButton(
                        onClick = onNavigateToCreate,
                        containerColor = AppColors.Primary,
                        modifier = Modifier
                            .size(AppDimens.FabSize),
                        shape = RoundedCornerShape(AppDimens.CornerRadiusButton)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), tint = AppColors.TextPrimary)
                    }
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
                        colors = listOf(AppColors.BackgroundStart, AppColors.BackgroundEnd)
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
                        .padding(horizontal = AppDimens.PaddingLarge, vertical = AppDimens.PaddingSmall)
                        .clip(RoundedCornerShape(AppDimens.CornerRadiusCard)),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.CardBackground
                    )
                ) {
                    SearchBar(
                        value = searchQuery,
                        onValueChange = { partsViewModel.updateAdminSearchQuery(it) },
                        isDarkBackground = true
                    )
                }

                if (categories.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall),
                        contentPadding = PaddingValues(horizontal = AppDimens.PaddingLarge)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { partsViewModel.selectAdminCategory(null) },
                                label = { Text(stringResource(R.string.all), color = AppColors.TextPrimary) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.Primary,
                                    selectedLabelColor = AppColors.TextPrimary
                                )
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { partsViewModel.selectAdminCategory(category) },
                                label = { Text(category, color = AppColors.TextPrimary) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AppColors.Primary,
                                    selectedLabelColor = AppColors.TextPrimary
                                )
                            )
                        }
                    }
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { partsViewModel.loadAdminParts(isSwipe = true) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = adminPartsState) {
                        is PartsUiState.Loading -> {
                            if (!isRefreshing) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AppColors.Primary)
                                }
                            }
                        }
                        is PartsUiState.Success -> {
                            if (allParts.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .width(AppDimens.IconSizeXLarge * 4)
                                        .clip(RoundedCornerShape(AppDimens.CornerRadiusXLarge)),
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
                                            text = if (searchQuery.isNotEmpty() || selectedCategory != null)
                                                stringResource(R.string.no_matching_parts)
                                            else
                                                stringResource(R.string.no_parts_found),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = AppColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                                        Button(
                                            onClick = onNavigateToCreate,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AppColors.Primary,
                                                contentColor = AppColors.TextPrimary
                                            ),
                                            shape = RoundedCornerShape(AppDimens.CornerRadiusButton)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(AppDimens.PaddingSmall))
                                            Text(stringResource(R.string.create_part))
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        start = AppDimens.PaddingLarge,
                                        end = AppDimens.PaddingLarge,
                                        top = AppDimens.PaddingMedium,
                                        bottom = AppDimens.BottomNavPaddingLarge
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                                ) {
                                    items(allParts) { part ->
                                        PartCard(
                                            part = part,
                                            onClick = { onNavigateToEdit(part.id) },
                                            showEditDelete = true,
                                            onEdit = { onNavigateToEdit(part.id) },
                                            onDelete = { showDeleteDialog = part }
                                        )
                                    }
                                }
                            }
                        }
                        is PartsUiState.Error -> {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .width(AppDimens.CardWidthEmptyState)
                                    .clip(RoundedCornerShape(AppDimens.CornerRadiusXLarge)),
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
                                        Icons.Default.AdminPanelSettings,
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
                                        onClick = { partsViewModel.loadAdminParts() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AppColors.Primary
                                        ),
                                        shape = RoundedCornerShape(AppDimens.CornerRadiusButton)
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

    showDeleteDialog?.let { part ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = AppColors.BackgroundStart,
            title = {
                Text(
                    stringResource(R.string.delete_part),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_part_confirm, part.name),
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        partsViewModel.deletePart(part.id) {
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text(stringResource(R.string.delete), color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel), color = AppColors.TextSecondary)
                }
            }
        )
    }
}