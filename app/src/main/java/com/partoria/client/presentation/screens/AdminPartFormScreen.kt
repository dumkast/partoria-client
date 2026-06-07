package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPartFormScreen(
    partsViewModel: PartsViewModel,
    partId: Int? = null,
    isEditMode: Boolean = false,
    onBack: () -> Unit
) {
    val formState by partsViewModel.partFormState.collectAsStateWithLifecycle()
    val filtersMetaState by partsViewModel.filtersMetaState.collectAsStateWithLifecycle()
    val isDetailLoading by partsViewModel.isDetailLoading.collectAsStateWithLifecycle()

    val categories = (filtersMetaState as? FiltersMetaUiState.Success)?.meta?.categories ?: emptyList()
    val brands = (filtersMetaState as? FiltersMetaUiState.Success)?.meta?.brands ?: emptyList()

    val isNameValid = formState.name.isNotBlank()
    val isCategoryValid = formState.category.isNotBlank()
    val isBrandValid = formState.brand.isNotBlank()
    val isPriceValid = formState.price.toDoubleOrNull()?.let { it > 0 } == true
    val isSpecsValid = formState.specs.isNotBlank()
    val isYearValid = formState.releaseYear.toIntOrNull()?.let { it in 2000..2026 } == true

    val isFormValid = isNameValid && isCategoryValid && isBrandValid && isPriceValid && isSpecsValid && isYearValid

    var currentSpec by remember { mutableStateOf("") }
    var currentValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var brandExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        partsViewModel.loadFiltersMeta()
    }

    LaunchedEffect(partId) {
        if (isEditMode && partId != null) {
            partsViewModel.loadPartForEditing(partId)
        }
    }

    Scaffold(
        containerColor = AppColors.BackgroundEnd,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall)
                    ) {
                        Text(
                            if (isEditMode) stringResource(R.string.edit_part) else stringResource(R.string.create_part),
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = AppColors.TextPrimary)
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
            if (isEditMode && isDetailLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(AppDimens.PaddingLarge),
                        verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                    ) {
                        item {
                            OutlinedTextField(
                                value = formState.name,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(name = text) } },
                                label = { Text(stringResource(R.string.name_label), color = AppColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = showErrors && !isNameValid,
                                supportingText = {
                                    if (showErrors && !isNameValid) Text(stringResource(R.string.name_required), color = AppColors.Error)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.BorderFocused,
                                    unfocusedBorderColor = AppColors.BorderUnfocused,
                                    focusedTextColor = AppColors.TextPrimary,
                                    unfocusedTextColor = AppColors.TextPrimary
                                )
                            )
                        }
                        item {
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = formState.category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.category_label), color = AppColors.TextSecondary) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    isError = showErrors && !isCategoryValid,
                                    supportingText = {
                                        if (showErrors && !isCategoryValid) Text(stringResource(R.string.category_required), color = AppColors.Error)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.BorderFocused,
                                        unfocusedBorderColor = AppColors.BorderUnfocused,
                                        focusedTextColor = AppColors.TextPrimary,
                                        unfocusedTextColor = AppColors.TextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    containerColor = AppColors.BackgroundStart
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category, color = AppColors.TextPrimary) },
                                            onClick = {
                                                partsViewModel.updatePartFormField { state -> state.copy(category = category) }
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            ExposedDropdownMenuBox(
                                expanded = brandExpanded,
                                onExpandedChange = { brandExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = formState.brand,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.brand_label), color = AppColors.TextSecondary) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                                    isError = showErrors && !isBrandValid,
                                    supportingText = {
                                        if (showErrors && !isBrandValid) Text(stringResource(R.string.brand_required), color = AppColors.Error)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.BorderFocused,
                                        unfocusedBorderColor = AppColors.BorderUnfocused,
                                        focusedTextColor = AppColors.TextPrimary,
                                        unfocusedTextColor = AppColors.TextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = brandExpanded,
                                    onDismissRequest = { brandExpanded = false },
                                    containerColor = AppColors.BackgroundStart
                                ) {
                                    brands.forEach { brand ->
                                        DropdownMenuItem(
                                            text = { Text(brand, color = AppColors.TextPrimary) },
                                            onClick = {
                                                partsViewModel.updatePartFormField { state -> state.copy(brand = brand) }
                                                brandExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = formState.price,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(price = text) } },
                                label = { Text(stringResource(R.string.price_label), color = AppColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = showErrors && !isPriceValid,
                                supportingText = {
                                    if (showErrors && !isPriceValid) Text(stringResource(R.string.price_error), color = AppColors.Error)
                                },
                                leadingIcon = { Text("$", color = AppColors.TextSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.BorderFocused,
                                    unfocusedBorderColor = AppColors.BorderUnfocused,
                                    focusedTextColor = AppColors.TextPrimary,
                                    unfocusedTextColor = AppColors.TextPrimary
                                )
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formState.specs,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(specs = text) } },
                                label = { Text(stringResource(R.string.specs_label), color = AppColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                isError = showErrors && !isSpecsValid,
                                supportingText = {
                                    if (showErrors && !isSpecsValid) Text(stringResource(R.string.specs_required), color = AppColors.Error)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.BorderFocused,
                                    unfocusedBorderColor = AppColors.BorderUnfocused,
                                    focusedTextColor = AppColors.TextPrimary,
                                    unfocusedTextColor = AppColors.TextPrimary
                                )
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formState.releaseYear,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(releaseYear = text) } },
                                label = { Text(stringResource(R.string.release_year_label), color = AppColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = showErrors && !isYearValid,
                                supportingText = {
                                    if (showErrors && !isYearValid) Text(stringResource(R.string.year_error), color = AppColors.Error)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.BorderFocused,
                                    unfocusedBorderColor = AppColors.BorderUnfocused,
                                    focusedTextColor = AppColors.TextPrimary,
                                    unfocusedTextColor = AppColors.TextPrimary
                                )
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
                                Column(
                                    modifier = Modifier.padding(AppDimens.PaddingLarge),
                                    verticalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                                ) {
                                    Text(
                                        text = stringResource(R.string.technical_details_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )

                                    if (formState.details.isNotEmpty()) {
                                        formState.details.forEach { (spec, value) ->
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(AppDimens.CornerRadiusButton),
                                                color = AppColors.SurfaceDark
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(AppDimens.PaddingMedium),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            spec,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = AppColors.TextPrimary
                                                        )
                                                        Text(
                                                            value,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = AppColors.TextSecondary
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            partsViewModel.updatePartFormField { state ->
                                                                state.copy(details = state.details.filterNot { it.first == spec && it.second == value })
                                                            }
                                                        },
                                                        modifier = Modifier.size(AppDimens.IconButtonSize)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Delete,
                                                            contentDescription = stringResource(R.string.delete),
                                                            tint = AppColors.Error,
                                                            modifier = Modifier.size(AppDimens.IconSizeMedium)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            stringResource(R.string.no_details_added),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.TextHint
                                        )
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = AppDimens.PaddingMicro),
                                        color = AppColors.BorderUnfocused
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingSmall),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        OutlinedTextField(
                                            value = currentSpec,
                                            onValueChange = { currentSpec = it },
                                            label = { Text(stringResource(R.string.specification_hint), color = AppColors.TextSecondary) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AppColors.BorderFocused,
                                                unfocusedBorderColor = AppColors.BorderUnfocused,
                                                focusedTextColor = AppColors.TextPrimary,
                                                unfocusedTextColor = AppColors.TextPrimary
                                            )
                                        )
                                        OutlinedTextField(
                                            value = currentValue,
                                            onValueChange = { currentValue = it },
                                            label = { Text(stringResource(R.string.value_hint), color = AppColors.TextSecondary) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AppColors.BorderFocused,
                                                unfocusedBorderColor = AppColors.BorderUnfocused,
                                                focusedTextColor = AppColors.TextPrimary,
                                                unfocusedTextColor = AppColors.TextPrimary
                                            )
                                        )
                                        IconButton(
                                            onClick = {
                                                if (currentSpec.isNotBlank() && currentValue.isNotBlank()) {
                                                    partsViewModel.updatePartFormField { state ->
                                                        state.copy(details = state.details + (currentSpec to currentValue))
                                                    }
                                                    currentSpec = ""
                                                    currentValue = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(top = AppDimens.PaddingSmall)
                                                .size(AppDimens.AddButtonSize)
                                                .clip(RoundedCornerShape(AppDimens.CornerRadiusButton))
                                                .background(AppColors.Primary)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add), tint = AppColors.TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppDimens.PaddingLarge, vertical = AppDimens.PaddingSmall),
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.Error.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = AppColors.Error,
                                modifier = Modifier.padding(AppDimens.PaddingMedium),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    val errorUpdatePart = stringResource(R.string.error_update_part)
                    val errorCreatePart = stringResource(R.string.error_create_part)
                    Button(
                        onClick = {
                            if (isEditMode && partId != null) {
                                isLoading = true
                                partsViewModel.updatePart(
                                    id = partId,
                                    name = formState.name,
                                    category = formState.category,
                                    brand = formState.brand,
                                    price = formState.price.toDoubleOrNull() ?: 0.0,
                                    specs = formState.specs,
                                    releaseYear = formState.releaseYear.toIntOrNull() ?: 2024,
                                    details = formState.details.map { (spec, value) ->
                                        PartDetailRequest(specification = spec, value = value)
                                    },
                                    onSuccess = {
                                        isLoading = false
                                        onBack()
                                    },
                                    onError = {
                                        isLoading = false
                                        errorMessage = errorUpdatePart
                                    }
                                )
                            } else {
                                showErrors = true
                                if (isFormValid) {
                                    isLoading = true
                                    partsViewModel.createPart(
                                        name = formState.name,
                                        category = formState.category,
                                        brand = formState.brand,
                                        price = formState.price.toDoubleOrNull() ?: 0.0,
                                        specs = formState.specs,
                                        releaseYear = formState.releaseYear.toIntOrNull() ?: 2024,
                                        details = formState.details.map { (spec, value) ->
                                            PartDetailRequest(
                                                specification = spec,
                                                value = value
                                            )
                                        },
                                        onSuccess = {
                                            isLoading = false
                                            errorMessage = null
                                            partsViewModel.clearPartFormState()
                                            onBack()
                                        },
                                        onError = {
                                            isLoading = false
                                            errorMessage = errorCreatePart
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.PaddingLarge)
                            .height(AppDimens.ButtonHeight),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.ButtonBackground
                        ),
                        shape = RoundedCornerShape(AppDimens.CornerRadiusButton)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppDimens.IconSizeNormal),
                                color = AppColors.TextPrimary
                            )
                        } else {
                            Text(
                                if (isEditMode) stringResource(R.string.update_part) else stringResource(R.string.create_part),
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}