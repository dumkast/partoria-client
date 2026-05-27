package com.partoria.client.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector? = null, val label: String? = null) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home", Icons.Default.Home, "Catalog")
    object PartDetail : Screen("part_detail/{partId}") {
        fun createRoute(partId: Int): String = "part_detail/$partId"
    }
    object Favorites : Screen("favorites", Icons.Default.FavoriteBorder, "Favorites")
    object Profile : Screen("profile", Icons.Default.Person, "Profile")
    object Filter : Screen("filter")
    object Admin : Screen("admin", Icons.Default.AdminPanelSettings, "Admin")
    object AdminPartForm : Screen("admin_part_form")
    object AdminPartEdit : Screen("admin_part_edit/{partId}") {
        fun createRoute(partId: Int): String = "admin_part_edit/$partId"
    }
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Favorites,
    Screen.Profile,
    Screen.Admin
)