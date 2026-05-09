package com.example.gemmasample.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gemmasample.ui.chat.ChatScreen
import com.example.gemmasample.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Chat : Screen("chat")
    object Settings : Screen("settings")
}

@Composable
fun GemmaSampleNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
