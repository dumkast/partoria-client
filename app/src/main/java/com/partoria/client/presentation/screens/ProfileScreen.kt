package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.presentation.viewmodels.AuthViewModel

enum class AppTheme(val title: String) {
    SYSTEM("System Default"),
    LIGHT("Light Theme"),
    DARK("Dark Theme")
}

data class GradientColors(val name: String, val start: Color, val end: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    savedColorIndex: Int,
    onColorIndexChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }
    var showThemeDropdown by remember { mutableStateOf(false) }

    val username by authViewModel.getUsername().collectAsStateWithLifecycle(initialValue = "")
    val role by authViewModel.getUserRole().collectAsStateWithLifecycle(initialValue = "")

    val displayUsername = username?.takeIf { it.isNotBlank() } ?: "User"
    val isAdmin = remember(role) {
        !role.isNullOrBlank() && role.equals("admin", ignoreCase = true)
    }
    val vibrantGradients = remember {
        listOf(
            GradientColors("Neon Purple", Color(0xFF7F00FF), Color(0xFFE100FF)),
            GradientColors("Electric Cyan", Color(0xFF00F2FE), Color(0xFF4FACFE)),
            GradientColors("Hot Pink", Color(0xFFFF0844), Color(0xFFFFB199)),
            GradientColors("Sunset Orange", Color(0xFFFF4E50), Color(0xFFF9D423)),
            GradientColors("Acid Lime", Color(0xFF11998E), Color(0xFF38EF7D))
        )
    }

    val activeColorIndex = remember(savedColorIndex) {
        if (savedColorIndex in vibrantGradients.indices) {
            savedColorIndex
        } else {
            val randomIndex = vibrantGradients.indices.random()
            onColorIndexChange(randomIndex)
            randomIndex
        }
    }

    val selectedGradient = remember(activeColorIndex) {
        vibrantGradients[activeColorIndex]
    }

    val avatarLetter = remember(displayUsername) {
        displayUsername.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(selectedGradient.start, selectedGradient.end)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarLetter,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(displayUsername) },
                        supportingContent = { Text("Username") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    if (!role.isNullOrBlank()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = role?.replaceFirstChar { it.uppercase() } ?: "",
                                    color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isAdmin) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            supportingContent = { Text("Access Level / Role") },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box {
                        ListItem(
                            headlineContent = { Text("Avatar Color") },
                            supportingContent = { Text("Current: ${selectedGradient.name}") },
                            leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(selectedGradient.start, selectedGradient.end)))
                                )
                            },
                            modifier = Modifier.clickable { showColorDropdown = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        DropdownMenu(
                            expanded = showColorDropdown,
                            onDismissRequest = { showColorDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            vibrantGradients.forEachIndexed { index, gradient ->
                                DropdownMenuItem(
                                    text = { Text(gradient.name) },
                                    onClick = {
                                        onColorIndexChange(index)
                                        showColorDropdown = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(gradient.start, gradient.end)))
                                        )
                                    },
                                    trailingIcon = {
                                        if (index == activeColorIndex) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Box {
                        ListItem(
                            headlineContent = { Text("App Theme") },
                            supportingContent = { Text("Current: ${currentTheme.title}") },
                            leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.clickable { showThemeDropdown = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        DropdownMenu(
                            expanded = showThemeDropdown,
                            onDismissRequest = { showThemeDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            AppTheme.entries.forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme.title) },
                                    onClick = {
                                        onThemeChange(theme)
                                        showThemeDropdown = false
                                    },
                                    trailingIcon = {
                                        if (theme == currentTheme) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout from your profile?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}