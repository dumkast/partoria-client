package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

data class GradientColors(val name: String, val start: Color, val end: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    savedColorIndex: Int,
    onColorIndexChange: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }

    val username by authViewModel.getUsername().collectAsStateWithLifecycle(initialValue = "")
    val role by authViewModel.getUserRole().collectAsStateWithLifecycle(initialValue = "")

    val displayUsername = username?.takeIf { it.isNotBlank() } ?: "User"

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
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(120.dp)
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
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = displayUsername,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Username",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }

                        if (!role.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color(0xFF6C63FF),
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = role?.replaceFirstChar { it.uppercase() } ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Access Level / Role",
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Avatar Color
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Avatar Color",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Current: ${selectedGradient.name}",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = Color(0xFF6C63FF),
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.linearGradient(
                                                listOf(selectedGradient.start, selectedGradient.end)
                                            )
                                        )
                                )
                            },
                            modifier = Modifier.clickable { showColorDropdown = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        DropdownMenu(
                            expanded = showColorDropdown,
                            onDismissRequest = { showColorDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            containerColor = Color(0xFF1A1A2E)
                        ) {
                            vibrantGradients.forEachIndexed { index, gradient ->
                                DropdownMenuItem(
                                    text = { Text(gradient.name, color = Color.White) },
                                    onClick = {
                                        onColorIndexChange(index)
                                        showColorDropdown = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        listOf(gradient.start, gradient.end)
                                                    )
                                                )
                                        )
                                    },
                                    trailingIcon = {
                                        if (index == activeColorIndex) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFF6C63FF)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Logout button
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Logout",
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF1A1A2E),
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Are you sure you want to logout?",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.logout()
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Yes", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}