package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                priceRange = (filter.minPrice ?: meta.priceRange.min).toFloat()..(filter.maxPrice ?: meta.priceRange.max).toFloat()
                yearRange = (filter.minYear ?: meta.yearRange.min).toFloat()..(filter.maxYear ?: meta.yearRange.max).toFloat()
                sortBy = filter.sortBy
                sortDirection = filter.sortDirection ?: "asc"
            }
            lastInitializedFilter = filter
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Filters",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E)
                ),
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
                        Text("Reset all", color = Color(0xFFFF6B6B))
                    }
                }
            )
        },
        bottomBar = {
            if (currentState is FiltersMetaUiState.Success) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color(0xFF1A1A2E),
                    tonalElevation = 0.dp
                ) {
                    Button(
                        onClick = {
                            val meta = currentState.meta
                            onApplyFilter(
                                Filter(
                                    categories = selectedCategories.takeIf { it.isNotEmpty() },
                                    brands = selectedBrands.takeIf { it.isNotEmpty() },
                                    minPrice = priceRange.start.toDouble().takeIf { it > meta.priceRange.min + 0.01 },
                                    maxPrice = priceRange.endInclusive.toDouble().takeIf { it < meta.priceRange.max - 0.01 },
                                    minYear = yearRange.start.toInt().takeIf { it > meta.yearRange.min },
                                    maxYear = yearRange.endInclusive.toInt().takeIf { it < meta.yearRange.max },
                                    sortBy = sortBy,
                                    sortDirection = sortDirection
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Filters", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    }
                }
            }
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
            when (currentState) {
                is FiltersMetaUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6C63FF)
                    )
                }
                is FiltersMetaUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = currentState.message, color = Color(0xFFFF6B6B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { partsViewModel.loadFiltersMeta() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                        ) {
                            Text("Retry")
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
                            FilterSection(
                                title = "Categories",
                                items = meta.categories,
                                selectedItems = selectedCategories,
                                onSelectionChange = { selectedCategories = it }
                            )
                        }
                        item {
                            FilterSection(
                                title = "Brands",
                                items = meta.brands,
                                selectedItems = selectedBrands,
                                onSelectionChange = { selectedBrands = it }
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        text = "Price Range: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RangeSlider(
                                        value = priceRange,
                                        onValueChange = { priceRange = it },
                                        valueRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat(),
                                        steps = 20,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF6C63FF),
                                            activeTrackColor = Color(0xFF6C63FF)
                                        )
                                    )
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        text = "Year Range: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RangeSlider(
                                        value = yearRange,
                                        onValueChange = { yearRange = it },
                                        valueRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat(),
                                        steps = 20,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF6C63FF),
                                            activeTrackColor = Color(0xFF6C63FF)
                                        )
                                    )
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text(
                                        text = "Sort By",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        sortOptions.forEach { option ->
                                            FilterChip(
                                                selected = sortBy == option,
                                                onClick = { sortBy = if (sortBy == option) null else option },
                                                label = { Text(
                                                    option.replaceFirstChar { it.uppercase() },
                                                    color = if (sortBy == option) Color.White else Color.White.copy(alpha = 0.8f)
                                                )},
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF6C63FF),
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    if (sortBy != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            FilterChip(
                                                selected = sortDirection == "asc",
                                                onClick = { sortDirection = "asc" },
                                                label = { Text("Ascending", color = Color.White) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF6C63FF),
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                            FilterChip(
                                                selected = sortDirection == "desc",
                                                onClick = { sortDirection = "desc" },
                                                label = { Text("Descending", color = Color.White) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF6C63FF),
                                                    selectedLabelColor = Color.White
                                                )
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
}

@Composable
fun FilterSection(
    title: String,
    items: List<String>,
    selectedItems: List<String>,
    onSelectionChange: (List<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (selectedItems.isNotEmpty()) {
                    TextButton(
                        onClick = { onSelectionChange(emptyList()) },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Clear", color = Color(0xFFFF6B6B))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    val isSelected = selectedItems.contains(item)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onSelectionChange(
                                if (isSelected) selectedItems - item
                                else selectedItems + item
                            )
                        },
                        label = { Text(item, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6C63FF),
                            selectedLabelColor = Color.White,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f),
                            disabledLabelColor = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    }
}