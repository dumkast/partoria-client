package com.partoria.client.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
    val savedFilter by partsViewModel.currentFilter.collectAsStateWithLifecycle()

    var selectedCategories by remember { mutableStateOf<List<String>>(savedFilter.categories ?: emptyList()) }
    var selectedBrands by remember { mutableStateOf<List<String>>(savedFilter.brands ?: emptyList()) }

    var minPrice by remember { mutableStateOf(savedFilter.minPrice ?: 0.0) }
    var maxPrice by remember { mutableStateOf(savedFilter.maxPrice ?: 10000.0) }
    var minYear by remember { mutableStateOf(savedFilter.minYear ?: 2000) }
    var maxYear by remember { mutableStateOf(savedFilter.maxYear ?: 2026) }

    var sortBy by remember { mutableStateOf<String?>(savedFilter.sortBy) }
    var sortDirection by remember { mutableStateOf(savedFilter.sortDirection ?: "asc") }

    var priceRange by remember { mutableStateOf((savedFilter.minPrice?.toFloat() ?: 0f)..(savedFilter.maxPrice?.toFloat() ?: 10000f)) }
    var yearRange by remember { mutableStateOf((savedFilter.minYear?.toFloat() ?: 2000f)..(savedFilter.maxYear?.toFloat() ?: 2026f)) }

    var metaLoaded by remember { mutableStateOf(false) }
    val sortOptions = listOf("price", "name", "year", "brand")

    LaunchedEffect(Unit) {
        partsViewModel.loadFiltersMeta()
    }

    val currentState = filtersMetaState

    LaunchedEffect(currentState) {
        if (currentState is FiltersMetaUiState.Success && !metaLoaded) {
            val meta = currentState.meta

            if (savedFilter.minPrice == null) minPrice = meta.priceRange.min
            if (savedFilter.maxPrice == null) maxPrice = meta.priceRange.max
            if (savedFilter.minYear == null) minYear = meta.yearRange.min
            if (savedFilter.maxYear == null) maxYear = meta.yearRange.max

            priceRange = (savedFilter.minPrice?.toFloat() ?: meta.priceRange.min.toFloat())..(savedFilter.maxPrice?.toFloat() ?: meta.priceRange.max.toFloat())
            yearRange = (savedFilter.minYear?.toFloat() ?: meta.yearRange.min.toFloat())..(savedFilter.maxYear?.toFloat() ?: meta.yearRange.max.toFloat())

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
                    if (currentState is FiltersMetaUiState.Success) {
                        TextButton(
                            onClick = {
                                val meta = currentState.meta
                                selectedCategories = emptyList()
                                selectedBrands = emptyList()
                                minPrice = meta.priceRange.min
                                maxPrice = meta.priceRange.max
                                minYear = meta.yearRange.min
                                maxYear = meta.yearRange.max
                                priceRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat()
                                yearRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat()
                                sortBy = null
                                sortDirection = "asc"

                                partsViewModel.resetFilters()
                                onBack()
                            }
                        ) {
                            Text(
                                text = "Reset",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(
                                    text = "Categories",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(
                                    text = "Brands",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                val priceMin = meta.priceRange.min.toFloat()
                                val priceMax = meta.priceRange.max.toFloat()

                                if (priceMax > priceMin && priceRange.start >= priceMin && priceRange.endInclusive <= priceMax) {
                                    Text(
                                        text = "Price Range: \$${priceRange.start.toInt()} - \$${priceRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    RangeSlider(
                                        value = priceRange,
                                        onValueChange = { priceRange = it },
                                        onValueChangeFinished = {
                                            minPrice = priceRange.start.toDouble()
                                            maxPrice = priceRange.endInclusive.toDouble()
                                        },
                                        valueRange = priceMin..priceMax
                                    )
                                } else {
                                    Text(
                                        text = "Price Range: \$${priceMin.toInt()} - \$${priceMax.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                val yearMin = meta.yearRange.min.toFloat()
                                val yearMax = meta.yearRange.max.toFloat()

                                if (yearMax > yearMin && yearRange.start >= yearMin && yearRange.endInclusive <= yearMax) {
                                    Text(
                                        text = "Year Range: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    RangeSlider(
                                        value = yearRange,
                                        onValueChange = { yearRange = it },
                                        onValueChangeFinished = {
                                            minYear = yearRange.start.toInt()
                                            maxYear = yearRange.endInclusive.toInt()
                                        },
                                        valueRange = yearMin..yearMax
                                    )
                                } else {
                                    Text(
                                        text = "Year Range: ${yearMin.toInt()} - ${yearMax.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(
                                    text = "Sort By",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        sortDirection = sortDirection,
                                        page = 1,
                                        pageSize = 100
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