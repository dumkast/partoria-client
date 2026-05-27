package com.partoria.client.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.domain.model.ComputerPart
import com.partoria.client.presentation.viewmodels.PartsUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.utils.CategoryIcon
import com.partoria.client.presentation.components.SearchBar

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
    val selectedCategory by partsViewModel.adminSelectedCategory.collectAsStateWithLifecycle()
    val filteredParts by partsViewModel.filteredAdminParts.collectAsStateWithLifecycle()
    val allParts = (adminPartsState as? PartsUiState.Success)?.parts ?: emptyList()
    val categories = allParts.map { it.category }.distinct().sorted()

    LaunchedEffect(Unit) {
        partsViewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(Unit) {
        partsViewModel.loadAdminParts()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp)) },
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel (${filteredParts.size}/${allParts.size})") },
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
                        if (filteredParts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No parts found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onNavigateToCreate
                                ) {
                                    Text("Create first part")
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
                                items(filteredParts) { part ->
                                    AdminPartCard(
                                        part = part,
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

@Composable
fun AdminPartCard(
    part: ComputerPart,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryIcon.getIcon(part.category),
                    contentDescription = part.category,
                    modifier = Modifier.size(40.dp),
                    tint = CategoryIcon.getColor(part.category)
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

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}