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
    val savedFilter by partsViewModel.activeFilter.collectAsStateWithLifecycle()

    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedBrands by remember { mutableStateOf<List<String>>(emptyList()) }

    var priceRange by remember { mutableStateOf(0f..10000f) }
    var yearRange by remember { mutableStateOf(2000f..2025f) }

    var sortBy by remember { mutableStateOf<String?>(null) }
    var sortDirection by remember { mutableStateOf("asc") }

    var lastInitializedFilter by remember { mutableStateOf<Filter?>(null) }

    val sortOptions = listOf("price", "name", "year", "brand")
    val currentState = filtersMetaState

    LaunchedEffect(Unit) {
        if (currentState is FiltersMetaUiState.Loading) {
            partsViewModel.loadFiltersMeta()
        }
    }

    LaunchedEffect(currentState, savedFilter) {
        if (currentState is FiltersMetaUiState.Success && savedFilter != lastInitializedFilter) {
            val meta = currentState.meta
            val filter = savedFilter

            if (filter == null) {
                priceRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat()
                yearRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat()
                selectedCategories = emptyList()
                selectedBrands = emptyList()
                sortBy = null
                sortDirection = "asc"
            } else {
                selectedCategories = filter.categories ?: emptyList()
                selectedBrands = filter.brands ?: emptyList()

                val minP = filter.minPrice ?: meta.priceRange.min
                val maxP = filter.maxPrice ?: meta.priceRange.max
                priceRange = minP.toFloat()..maxP.toFloat()

                val minY = filter.minYear ?: meta.yearRange.min
                val maxY = filter.maxYear ?: meta.yearRange.max
                yearRange = minY.toFloat()..maxY.toFloat()

                sortBy = filter.sortBy
                sortDirection = filter.sortDirection ?: "asc"
            }
            lastInitializedFilter = filter
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
                    TextButton(onClick = {
                        selectedCategories = emptyList()
                        selectedBrands = emptyList()
                        sortBy = null
                        sortDirection = "asc"
                        if (currentState is FiltersMetaUiState.Success) {
                            val meta = currentState.meta
                            priceRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat()
                            yearRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat()
                        }
                    }) {
                        Text("Reset all")
                    }
                }
            )
        },
        bottomBar = {
            if (currentState is FiltersMetaUiState.Success) {
                val meta = currentState.meta
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    tonalElevation = 4.dp
                ) {
                    Button(
                        onClick = {
                            val minP = priceRange.start.toDouble()
                            val maxP = priceRange.endInclusive.toDouble()
                            val minY = yearRange.start.toInt()
                            val maxY = yearRange.endInclusive.toInt()

                            onApplyFilter(
                                Filter(
                                    categories = selectedCategories.takeIf { it.isNotEmpty() },
                                    brands = selectedBrands.takeIf { it.isNotEmpty() },
                                    minPrice = minP.takeIf { minP > meta.priceRange.min + 0.01 },
                                    maxPrice = maxP.takeIf { maxP < meta.priceRange.max - 0.01 },
                                    minYear = minY.takeIf { minY > meta.yearRange.min },
                                    maxYear = maxY.takeIf { maxY < meta.yearRange.max },
                                    sortBy = sortBy,
                                    sortDirection = sortDirection
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    ) { paddingValues ->
        when (currentState) {
            is FiltersMetaUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FiltersMetaUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = currentState.message, color = MaterialTheme.colorScheme.error)
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
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Categories", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (selectedCategories.isNotEmpty()) {
                                        TextButton(onClick = { selectedCategories = emptyList() }, modifier = Modifier.height(32.dp)) {
                                            Text("Clear", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(meta.categories) { category ->
                                        val isSelected = selectedCategories.contains(category)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedCategories = if (isSelected) selectedCategories - category else selectedCategories + category
                                            },
                                            label = { Text(category) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Brands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (selectedBrands.isNotEmpty()) {
                                        TextButton(onClick = { selectedBrands = emptyList() }, modifier = Modifier.height(32.dp)) {
                                            Text("Clear", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(meta.brands) { brand ->
                                        val isSelected = selectedBrands.contains(brand)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                selectedBrands = if (isSelected) selectedBrands - brand else selectedBrands + brand
                                            },
                                            label = { Text(brand) },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null
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
                                    text = "Price Range: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                RangeSlider(
                                    value = priceRange,
                                    onValueChange = { priceRange = it },
                                    valueRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat(),
                                    steps = 20
                                )
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
                                    text = "Year Range: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                RangeSlider(
                                    value = yearRange,
                                    onValueChange = { yearRange = it },
                                    valueRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat(),
                                    steps = 20
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Text(text = "Sort By", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    sortOptions.forEach { option ->
                                        FilterChip(
                                            selected = sortBy == option,
                                            onClick = { sortBy = if (sortBy == option) null else option },
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
                }
            }
        }
    }
}