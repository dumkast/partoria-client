package com.partoria.client.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPartFormScreen(
    partsViewModel: PartsViewModel,
    onBack: () -> Unit
) {
    val formState by partsViewModel.partFormState.collectAsStateWithLifecycle()

    val filtersMetaState by partsViewModel.filtersMetaState.collectAsStateWithLifecycle()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Part") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = formState.name,
                        onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(name = text) } },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showErrors && !isNameValid,
                        supportingText = {
                            if (showErrors && !isNameValid) Text("Name is required")
                        }
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
                            label = { Text("Category") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            isError = showErrors && !isCategoryValid,
                            supportingText = {
                                if (showErrors && !isCategoryValid) Text("Category is required")
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
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
                            label = { Text("Brand") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            isError = showErrors && !isBrandValid,
                            supportingText = {
                                if (showErrors && !isBrandValid) Text("Brand is required")
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            brands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
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
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showErrors && !isPriceValid,
                        supportingText = {
                            if (showErrors && !isPriceValid) Text("Price must be greater than 0")
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = formState.specs,
                        onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(specs = text) } },
                        label = { Text("Specifications") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        isError = showErrors && !isSpecsValid,
                        supportingText = {
                            if (showErrors && !isSpecsValid) Text("Specifications are required")
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = formState.releaseYear,
                        onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(releaseYear = text) } },
                        label = { Text("Release Year") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = showErrors && !isYearValid,
                        supportingText = {
                            if (showErrors && !isYearValid) Text("Year must be between 2000 and 2026")
                        }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Technical Details",
                                style = MaterialTheme.typography.titleMedium
                            )

                            if (formState.details.isNotEmpty()) {
                                formState.details.forEach { (spec, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                spec,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                value,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                partsViewModel.updatePartFormField { state ->
                                                    state.copy(details = state.details.filterNot { it.first == spec && it.second == value })
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    "No technical details added",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentSpec,
                                    onValueChange = { currentSpec = it },
                                    label = { Text("Specification") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { currentValue = it },
                                    label = { Text("Value") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
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
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
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
                                com.partoria.client.data.model.PartDetailRequest(
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
                                errorMessage = "Failed to create part. Check your data."
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Create")
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}