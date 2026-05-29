package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.data.model.PartDetailRequest
import com.partoria.client.presentation.viewmodels.FiltersMetaUiState
import com.partoria.client.presentation.viewmodels.PartsViewModel

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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            if (isEditMode) "Edit Part" else "Create Part",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            if (isEditMode && isDetailLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6C63FF))
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = formState.name,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(name = text) } },
                                label = { Text("Name", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = showErrors && !isNameValid,
                                supportingText = {
                                    if (showErrors && !isNameValid) Text("Name is required", color = Color(0xFFFF6B6B))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
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
                                    label = { Text("Category", color = Color.White.copy(alpha = 0.7f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    isError = showErrors && !isCategoryValid,
                                    supportingText = {
                                        if (showErrors && !isCategoryValid) Text("Category is required", color = Color(0xFFFF6B6B))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6C63FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    containerColor = Color(0xFF1A1A2E)
                                ) {
                                    categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category, color = Color.White) },
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
                                    label = { Text("Brand", color = Color.White.copy(alpha = 0.7f)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                                    isError = showErrors && !isBrandValid,
                                    supportingText = {
                                        if (showErrors && !isBrandValid) Text("Brand is required", color = Color(0xFFFF6B6B))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6C63FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = brandExpanded,
                                    onDismissRequest = { brandExpanded = false },
                                    containerColor = Color(0xFF1A1A2E)
                                ) {
                                    brands.forEach { brand ->
                                        DropdownMenuItem(
                                            text = { Text(brand, color = Color.White) },
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
                                label = { Text("Price", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = showErrors && !isPriceValid,
                                supportingText = {
                                    if (showErrors && !isPriceValid) Text("Price must be greater than 0", color = Color(0xFFFF6B6B))
                                },
                                leadingIcon = { Text("$", color = Color.White.copy(alpha = 0.7f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formState.specs,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(specs = text) } },
                                label = { Text("Specifications", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                isError = showErrors && !isSpecsValid,
                                supportingText = {
                                    if (showErrors && !isSpecsValid) Text("Specifications are required", color = Color(0xFFFF6B6B))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = formState.releaseYear,
                                onValueChange = { text -> partsViewModel.updatePartFormField { state -> state.copy(releaseYear = text) } },
                                label = { Text("Release Year", color = Color.White.copy(alpha = 0.7f)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = showErrors && !isYearValid,
                                supportingText = {
                                    if (showErrors && !isYearValid) Text("Year must be between 2000 and 2026", color = Color(0xFFFF6B6B))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
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
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Technical Details",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    if (formState.details.isNotEmpty()) {
                                        formState.details.forEach { (spec, value) ->
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color.White.copy(alpha = 0.05f)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            spec,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            value,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.White.copy(alpha = 0.7f)
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
                                                            tint = Color(0xFFFF6B6B),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            "No technical details added",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = Color.White.copy(alpha = 0.1f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        OutlinedTextField(
                                            value = currentSpec,
                                            onValueChange = { currentSpec = it },
                                            label = { Text("Specification", color = Color.White.copy(alpha = 0.7f)) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6C63FF),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        OutlinedTextField(
                                            value = currentValue,
                                            onValueChange = { currentValue = it },
                                            label = { Text("Value", color = Color.White.copy(alpha = 0.7f)) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF6C63FF),
                                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
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
                                                .padding(top = 8.dp)
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF6C63FF))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
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
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF6B6B).copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

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
                                        errorMessage = "Failed to update part."
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
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                if (isEditMode) "Update Part" else "Create Part",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}