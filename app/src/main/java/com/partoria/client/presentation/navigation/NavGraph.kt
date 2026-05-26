package com.partoria.client.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.partoria.client.presentation.screens.*
import com.partoria.client.presentation.viewmodels.AuthViewModel
import com.partoria.client.presentation.viewmodels.PartsViewModel
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    partsViewModel: PartsViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn().collectAsState(initial = false)
    val userRole by authViewModel.getUserRole().collectAsState(initial = "")
    val isAdmin = userRole == "admin"

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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = isLoggedIn &&
            currentRoute != Screen.PartDetail.route &&
            currentRoute != Screen.Filter.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    val screens = if (isAdmin) {
                        listOf(Screen.Home, Screen.Favorites, Screen.Admin, Screen.Profile)
                    } else {
                        listOf(Screen.Home, Screen.Favorites, Screen.Profile)
                    }
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = screen.label) } },
                            label = { Text(screen.label ?: "") },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier
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
                    onPartClick = { partId -> navController.navigate(Screen.PartDetail.createRoute(partId)) },
                    onFilterClick = { navController.navigate(Screen.Filter.route) }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    partsViewModel = partsViewModel,
                    onPartClick = { partId ->
                        navController.navigate(Screen.PartDetail.createRoute(partId))
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
                    }
                )
            }

            composable(Screen.Admin.route) {
                AdminScreen(
                    // TO DO
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
}