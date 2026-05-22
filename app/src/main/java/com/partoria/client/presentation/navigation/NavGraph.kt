package com.partoria.client.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.partoria.client.presentation.screens.FavoritesScreen
import com.partoria.client.presentation.screens.FilterScreen
import com.partoria.client.presentation.screens.HomeScreen
import com.partoria.client.presentation.screens.LoginScreen
import com.partoria.client.presentation.screens.PartDetailScreen
import com.partoria.client.presentation.screens.ProfileScreen
import com.partoria.client.presentation.screens.RegisterScreen
import com.partoria.client.presentation.viewmodels.AuthViewModel
import com.partoria.client.presentation.viewmodels.PartsViewModel

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    partsViewModel: PartsViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn().collectAsState(initial = false)

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                partsViewModel = partsViewModel,
                onPartClick = { partId ->
                    navController.navigate(Screen.PartDetail.createRoute(partId))
                },
                onFilterClick = {
                    navController.navigate(Screen.Filter.route)
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                partsViewModel = partsViewModel,
                onPartClick = { partId ->
                    navController.navigate(Screen.PartDetail.createRoute(partId))
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Filter.route) {
            FilterScreen(
                partsViewModel = partsViewModel,
                onApplyFilter = { filter ->
                    partsViewModel.loadFilteredParts(filter)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PartDetail.route,
            arguments = listOf(navArgument("partId") { type = NavType.IntType })
        ) { backStackEntry ->
            val partId = backStackEntry.arguments?.getInt("partId") ?: return@composable
            PartDetailScreen(
                partId = partId,
                partsViewModel = partsViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onFavoriteClick = { isFavorite ->
                    if (isFavorite) {
                        partsViewModel.removeFromFavorites(partId)
                    } else {
                        partsViewModel.addToFavorites(partId)
                    }
                }
            )
        }
    }
}