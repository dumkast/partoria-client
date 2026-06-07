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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.domain.model.Filter
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.filters),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.BackgroundStart
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
                        Text(stringResource(R.string.reset_all), color = AppColors.TextError)
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
                    color = AppColors.BackgroundStart,
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
                            .padding(AppDimens.PaddingLarge)
                            .height(AppDimens.ButtonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.ButtonBackground
                        ),
                        shape = RoundedCornerShape(AppDimens.CornerRadiusSmall)
                    ) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = AppColors.TextPrimary)
                        Spacer(modifier = Modifier.width(AppDimens.PaddingSmall))
                        Text(stringResource(R.string.apply_filters), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
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
                        colors = listOf(AppColors.BackgroundStart, AppColors.BackgroundEnd)
                    )
                )
        ) {
            when (currentState) {
                is FiltersMetaUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppColors.Primary
                    )
                }
                is FiltersMetaUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = currentState.message, color = AppColors.TextError)
                        Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                        Button(
                            onClick = { partsViewModel.loadFiltersMeta() },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.ButtonBackground)
                        ) {
                            Text(stringResource(R.string.retry), color = AppColors.TextPrimary)
                        }
                    }
                }
                is FiltersMetaUiState.Success -> {
                    val meta = currentState.meta
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(AppDimens.PaddingLarge),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingLarge)
                    ) {
                        item {
                            FilterSection(
                                title = stringResource(R.string.categories),
                                items = meta.categories,
                                selectedItems = selectedCategories,
                                onSelectionChange = { selectedCategories = it }
                            )
                        }
                        item {
                            FilterSection(
                                title = stringResource(R.string.brands),
                                items = meta.brands,
                                selectedItems = selectedBrands,
                                onSelectionChange = { selectedBrands = it }
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppColors.CardBackground
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(AppDimens.PaddingLarge)) {
                                    Text(
                                        text = "${stringResource(R.string.price_range)}: $${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                    RangeSlider(
                                        value = priceRange,
                                        onValueChange = { priceRange = it },
                                        valueRange = meta.priceRange.min.toFloat()..meta.priceRange.max.toFloat(),
                                        steps = 20,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AppColors.Primary,
                                            activeTrackColor = AppColors.Primary
                                        )
                                    )
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppColors.CardBackground
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(AppDimens.PaddingLarge)) {
                                    Text(
                                        text = "${stringResource(R.string.year_range)}: ${yearRange.start.toInt()} - ${yearRange.endInclusive.toInt()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                    RangeSlider(
                                        value = yearRange,
                                        onValueChange = { yearRange = it },
                                        valueRange = meta.yearRange.min.toFloat()..meta.yearRange.max.toFloat(),
                                        steps = 20,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AppColors.Primary,
                                            activeTrackColor = AppColors.Primary
                                        )
                                    )
                                }
                            }
                        }
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
                                colors = CardDefaults.cardColors(
                                    containerColor = AppColors.CardBackground
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(AppDimens.PaddingLarge)) {
                                    Text(
                                        text = stringResource(R.string.sort_by),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                    Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)) {
                                        sortOptions.forEach { option ->
                                            val stringResId = when (option) {
                                                "price" -> R.string.price
                                                "name" -> R.string.name
                                                "year" -> R.string.year
                                                "brand" -> R.string.brand_label
                                                else -> R.string.clear
                                            }
                                            FilterChip(
                                                selected = sortBy == option,
                                                onClick = { sortBy = if (sortBy == option) null else option },
                                                label = { Text(
                                                    stringResource(stringResId).replaceFirstChar { it.uppercase() },
                                                    color = if (sortBy == option) AppColors.TextPrimary else AppColors.TextSecondary
                                                )},
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.Primary,
                                                    selectedLabelColor = AppColors.TextPrimary
                                                )
                                            )
                                        }
                                    }
                                    if (sortBy != null) {
                                        Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                                        Row(horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)) {
                                            FilterChip(
                                                selected = sortDirection == "asc",
                                                onClick = { sortDirection = "asc" },
                                                label = { Text(stringResource(R.string.ascending), color = AppColors.TextPrimary) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.Primary,
                                                    selectedLabelColor = AppColors.TextPrimary
                                                )
                                            )
                                            FilterChip(
                                                selected = sortDirection == "desc",
                                                onClick = { sortDirection = "desc" },
                                                label = { Text(stringResource(R.string.descending), color = AppColors.TextPrimary) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AppColors.Primary,
                                                    selectedLabelColor = AppColors.TextPrimary
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
        shape = RoundedCornerShape(AppDimens.CornerRadiusCard),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.CardBackground
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(AppDimens.PaddingLarge)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                if (selectedItems.isNotEmpty()) {
                    TextButton(
                        onClick = { onSelectionChange(emptyList()) },
                        modifier = Modifier.height(AppDimens.PaddingXXLarge)
                    ) {
                        Text(stringResource(R.string.clear), color = AppColors.TextError)
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)) {
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
                        label = { Text(item, color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(AppDimens.IconSizeSmall), tint = AppColors.TextPrimary) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Primary,
                            selectedLabelColor = AppColors.TextPrimary,
                            disabledContainerColor = AppColors.BorderUnfocused,
                            disabledLabelColor = AppColors.TextSecondary
                        )
                    )
                }
            }
        }
    }
}