package com.roomie.belottracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roomie.belottracker.ui.screens.HomeScreen
import com.roomie.belottracker.ui.screens.NewGameScreen

@Composable
fun BelotNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNewGameClick = { navController.navigate("new_game") },
                onHistoryClick = { navController.navigate("history") },
                onGameClick = { gameId -> navController.navigate("game/$gameId") }
            )
        }

        composable("new_game") {
            NewGameScreen(
                onGameCreated = { gameId -> navController.navigate("game/$gameId") { popUpTo("home") } },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
