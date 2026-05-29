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
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.presentation.components.SearchBar
import com.partoria.client.presentation.components.PartCard
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState

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
        containerColor = Color(0xFFF5F5F5),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)
            )
        },
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(end = 16.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Admin Panel (${allParts.size})",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    FloatingActionButton(
                        onClick = onNavigateToCreate,
                        containerColor = Color(0xFF6C63FF),
                        modifier = Modifier
                            .size(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
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
                        colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { partsViewModel.selectAdminCategory(null) },
                                label = { Text("All", color = Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6C63FF),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { partsViewModel.selectAdminCategory(category) },
                                label = { Text(category, color = Color.White) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6C63FF),
                                    selectedLabelColor = Color.White
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
                                    CircularProgressIndicator(color = Color(0xFF6C63FF))
                                }
                            }
                        }
                        is PartsUiState.Success -> {
                            if (allParts.isEmpty()) {
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
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = Color.White.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = if (searchQuery.isNotEmpty() || selectedCategory != null)
                                                "No matching parts"
                                            else
                                                "No parts found",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = onNavigateToCreate,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF6C63FF)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Create Part")
                                        }
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
                                        Icons.Default.AdminPanelSettings,
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
                                        onClick = { partsViewModel.loadAdminParts() },
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

    showDeleteDialog?.let { part ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text(
                    "Delete Part",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${part.name}\"?",
                    color = Color.White.copy(alpha = 0.7f)
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
                    Text("Delete", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}