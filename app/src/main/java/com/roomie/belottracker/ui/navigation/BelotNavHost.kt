package com.roomie.belottracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.roomie.belottracker.ui.screens.HistoryScreen
import com.roomie.belottracker.ui.screens.HomeScreen
import com.roomie.belottracker.ui.screens.NewGameScreen
import com.roomie.belottracker.ui.screens.ScoringScreen
import com.roomie.belottracker.ui.screens.WinnerScreen

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

        composable("game/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.LongType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            ScoringScreen(
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onWinner = { gId, winnerName -> navController.navigate("winner/$gId/$winnerName") }
            )
        }

        composable("winner/{gameId}/{winnerName}",
            arguments = listOf(
                navArgument("gameId") { type = NavType.LongType },
                navArgument("winnerName") { type = NavType.StringType }
            )
        ) {
            WinnerScreen(onSaveAndExit = { navController.navigate("home") { popUpTo("home") { inclusive = true } } })
        }

        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onGameClick = { gameId -> navController.navigate("game/$gameId") }
            )
        }
    }
}
