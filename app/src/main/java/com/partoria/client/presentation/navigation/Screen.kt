package com.partoria.client.presentation.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object PartDetail : Screen("part_detail/{partId}") {
        fun createRoute(partId: Int): String = "part_detail/$partId"
    }
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object Filter : Screen("filter")
}