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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.presentation.viewmodels.AuthViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

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

    val displayUsername = username?.takeIf { it.isNotBlank() } ?: stringResource(R.string.app_name)

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
        containerColor = AppColors.BackgroundEnd,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile),
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppDimens.PaddingLarge, vertical = AppDimens.PaddingXLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.AvatarSize)
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
                        color = AppColors.TextPrimary,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.CornerRadiusLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.CardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.PaddingXLarge)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(AppDimens.IconSizeLarge)
                            )
                            Column {
                                Text(
                                    text = displayUsername,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.username).lowercase(),
                                    color = AppColors.TextHint
                                )
                            }
                        }

                        if (!role.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                            Divider(color = AppColors.BorderUnfocused)
                            Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppDimens.PaddingMedium)
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(AppDimens.IconSizeLarge)
                                )
                                Column {
                                    Text(
                                        text = role?.replaceFirstChar { it.uppercase() } ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Normal,
                                        color = AppColors.TextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.access_level),
                                        color = AppColors.TextHint
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.PaddingXLarge))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.CornerRadiusLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.CardBackground
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    stringResource(R.string.avatar_color),
                                    color = AppColors.TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            supportingContent = {
                                Text(
                                    "${stringResource(R.string.current)} ${selectedGradient.name}",
                                    color = AppColors.TextHint
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(AppDimens.IconSizeLarge)
                                )
                            },
                            trailingContent = {
                                Box(
                                    modifier = Modifier
                                        .size(AppDimens.IconSizeLarge)
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
                            containerColor = AppColors.BackgroundStart
                        ) {
                            vibrantGradients.forEachIndexed { index, gradient ->
                                DropdownMenuItem(
                                    text = { Text(gradient.name, color = AppColors.TextPrimary) },
                                    onClick = {
                                        onColorIndexChange(index)
                                        showColorDropdown = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(AppDimens.IconSizeNormal)
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
                                                tint = AppColors.Primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppDimens.SpacingLarge))
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppDimens.ButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.DeleteButtonBackground
                    ),
                    shape = RoundedCornerShape(AppDimens.CornerRadiusButton)
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = AppColors.Error
                    )
                    Spacer(modifier = Modifier.width(AppDimens.PaddingSmall))
                    Text(
                        stringResource(R.string.logout),
                        color = AppColors.Error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = AppColors.BackgroundStart,
            title = {
                Text(
                    stringResource(R.string.logout),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    stringResource(R.string.logout_confirm),
                    color = AppColors.TextSecondary
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
                    Text(stringResource(R.string.yes), color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.no), color = AppColors.TextSecondary)
                }
            }
        )
    }
}