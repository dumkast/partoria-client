package com.partoria.client.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Added missing import
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // Added missing import
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.domain.model.Filter
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    partsViewModel: PartsViewModel,
    onApplyFilter: (Filter) -> Unit,
    onBack: () -> Unit
) {
    val filtersMetaState by partsViewModel.filtersMetaState.collectAsStateWithLifecycle()

    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedBrands by remember { mutableStateOf<List<String>>(emptyList()) }
    var minPrice by remember { mutableStateOf(0.0) }
    var maxPrice by remember { mutableStateOf(10000.0) }
    var minYear by remember { mutableStateOf(2000) }
    var maxYear by remember { mutableStateOf(2025) }
    var sortBy by remember { mutableStateOf<String?>(null) }
    var sortDirection by remember { mutableStateOf("asc") }
    var priceRange by remember { mutableStateOf(0f..10000f) }
    var yearRange by remember { mutableStateOf(2000f..2025f) }
    var metaLoaded by remember { mutableStateOf(false) }

    val sortOptions = listOf("price", "name", "year", "brand")

    LaunchedEffect(Unit) {
        partsViewModel.loadFiltersMeta()
    }

    // Capture state locally to allow easy smart casting across the file
    val currentState = filtersMetaState

    LaunchedEffect(currentState) {
        if (currentState is FiltersMetaUiState.Success && !metaLoaded) {
            val meta = currentState.meta
            minPrice = meta.priceRange.min
            maxPrice = meta.priceRange.max
            minYear = meta.yearRange.min
            maxYear = meta.yearRange.max
            priceRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat()
            yearRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat()
            metaLoaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filters") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val maxLimit = (currentState as? FiltersMetaUiState.Success)?.meta?.priceRange?.max ?: 10000.0
                            onApplyFilter(
                                Filter(
                                    categories = selectedCategories.takeIf { it.isNotEmpty() },
                                    brands = selectedBrands.takeIf { it.isNotEmpty() },
                                    minPrice = if (minPrice > 0) minPrice else null,
                                    maxPrice = if (maxPrice < maxLimit) maxPrice else null,
                                    minYear = if (minYear > 2000) minYear else null,
                                    maxYear = if (maxYear < 2025) maxYear else null,
                                    sortBy = sortBy,
                                    sortDirection = sortDirection
                                )
                            )
                        }
                    ) {
                        Text("Apply")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (currentState) {
            is FiltersMetaUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FiltersMetaUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { partsViewModel.loadFiltersMeta() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is FiltersMetaUiState.Success -> {
                val meta = currentState.meta

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Categories",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(meta.categories) { category ->
                                        FilterChip(
                                            selected = selectedCategories.contains(category),
                                            onClick = {
                                                selectedCategories = if (selectedCategories.contains(category)) {
                                                    selectedCategories - category
                                                } else {
                                                    selectedCategories + category
                                                }
                                            },
                                            label = { Text(category) },
                                            leadingIcon = if (selectedCategories.contains(category)) {
                                                { Icon(Icons.Default.Check, contentDescription = null) }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Brands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(meta.brands) { brand ->
                                        FilterChip(
                                            selected = selectedBrands.contains(brand),
                                            onClick = {
                                                selectedBrands = if (selectedBrands.contains(brand)) {
                                                    selectedBrands - brand
                                                } else {
                                                    selectedBrands + brand
                                                }
                                            },
                                            label = { Text(brand) },
                                            leadingIcon = if (selectedBrands.contains(brand)) {
                                                { Icon(Icons.Default.Check, contentDescription = null) }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Price Range: \$${minPrice.toInt()} - \$${maxPrice.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                RangeSlider(
                                    value = priceRange,
                                    onValueChange = { priceRange = it },
                                    valueRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat(),
                                    steps = 20
                                )
                                LaunchedEffect(priceRange) {
                                    minPrice = priceRange.start.toDouble()
                                    maxPrice = priceRange.endInclusive.toDouble()
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Year Range: $minYear - $maxYear",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                RangeSlider(
                                    value = yearRange,
                                    onValueChange = { yearRange = it },
                                    valueRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat(),
                                    steps = 20
                                )
                                LaunchedEffect(yearRange) {
                                    minYear = yearRange.start.toInt()
                                    maxYear = yearRange.endInclusive.toInt()
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Sort By",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(sortOptions) { option ->
                                        FilterChip(
                                            selected = sortBy == option,
                                            onClick = {
                                                sortBy = if (sortBy == option) null else option
                                            },
                                            label = { Text(option.replaceFirstChar { it.uppercase() }) }
                                        )
                                    }
                                }

                                if (sortBy != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        FilterChip(
                                            selected = sortDirection == "asc",
                                            onClick = { sortDirection = "asc" },
                                            label = { Text("Ascending") }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        FilterChip(
                                            selected = sortDirection == "desc",
                                            onClick = { sortDirection = "desc" },
                                            label = { Text("Descending") }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                onApplyFilter(
                                    Filter(
                                        categories = selectedCategories.takeIf { it.isNotEmpty() },
                                        brands = selectedBrands.takeIf { it.isNotEmpty() },
                                        minPrice = minPrice.takeIf { it > meta.priceRange.min },
                                        maxPrice = maxPrice.takeIf { it < meta.priceRange.max },
                                        minYear = minYear.takeIf { it > meta.yearRange.min },
                                        maxYear = maxYear.takeIf { it < meta.yearRange.max },
                                        sortBy = sortBy,
                                        sortDirection = sortDirection
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            }
        }
    }
}