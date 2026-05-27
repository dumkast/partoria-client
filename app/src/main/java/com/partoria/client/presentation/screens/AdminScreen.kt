package com.partoria.client.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp))
        },
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel (${allParts.size})") },
                actions = {
                    IconButton(onClick = { onNavigateToCreate() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
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
                onValueChange = { partsViewModel.updateAdminSearchQuery(it) }
            )

            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { partsViewModel.selectAdminCategory(null) },
                            label = { Text("All") }
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { partsViewModel.selectAdminCategory(category) },
                            label = { Text(category) }
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
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is PartsUiState.Success -> {
                        if (allParts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty() || selectedCategory != null)
                                        "No matching parts found"
                                    else
                                        "No parts found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onNavigateToCreate) {
                                    Text("Create part")
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
                            Button(onClick = { partsViewModel.loadAdminParts() }) {
                                Text("Retry")
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
            title = { Text("Delete Part") },
            text = { Text("Are you sure you want to delete \"${part.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        partsViewModel.deletePart(part.id) {
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}