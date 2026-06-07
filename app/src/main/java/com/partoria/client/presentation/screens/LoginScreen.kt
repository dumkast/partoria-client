package com.partoria.client.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.partoria.client.R
import com.partoria.client.presentation.viewmodels.AuthUiState
import com.partoria.client.presentation.viewmodels.AuthViewModel
import com.partoria.client.ui.theme.AppColors
import com.partoria.client.ui.theme.AppDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val registrationSuccess by authViewModel.showRegistrationSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentState = uiState
    val registerSuccessMsg = stringResource(R.string.register_success)

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            snackbarHostState.showSnackbar(registerSuccessMsg)
            authViewModel.setRegistrationSuccess(false)
        }
    }

    LaunchedEffect(currentState) {
        if (currentState is AuthUiState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AppColors.BackgroundStart,
                            AppColors.BackgroundEnd
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppDimens.PaddingXLarge + AppDimens.PaddingMicro),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AppDimens.CornerRadiusXLarge),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.SurfaceDark
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppDimens.PaddingXXLarge),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.welcome_back),
                            style = MaterialTheme.typography.headlineLarge,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                        Text(
                            text = stringResource(R.string.sign_in_to_continue),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(AppDimens.PaddingXXLarge))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(stringResource(R.string.username), color = AppColors.TextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AppColors.TextSecondary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.BorderFocused,
                                unfocusedBorderColor = AppColors.BorderUnfocused,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(R.string.password), color = AppColors.TextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.TextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = AppColors.TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.BorderFocused,
                                unfocusedBorderColor = AppColors.BorderUnfocused,
                                focusedTextColor = AppColors.TextPrimary,
                                unfocusedTextColor = AppColors.TextPrimary
                            )
                        )

                        if (currentState is AuthUiState.Error) {
                            Spacer(modifier = Modifier.height(AppDimens.PaddingSmall))
                            Text(
                                text = currentState.message,
                                color = AppColors.TextError,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(AppDimens.PaddingXLarge + AppDimens.PaddingMicro))

                        Button(
                            onClick = { authViewModel.login(username, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AppDimens.ButtonHeight),
                            enabled = currentState !is AuthUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.ButtonBackground
                            ),
                            shape = RoundedCornerShape(AppDimens.CornerRadiusSmall)
                        ) {
                            if (currentState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(AppDimens.IconSizeNormal),
                                    color = AppColors.TextPrimary
                                )
                            } else {
                                Text(stringResource(R.string.sign_in), style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(AppDimens.PaddingLarge))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.no_account) + " ",
                                color = AppColors.TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.sign_up),
                                color = AppColors.Primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.clickable { onNavigateToRegister() }
                            )
                        }
                    }
                }
            }
        }
    }
}