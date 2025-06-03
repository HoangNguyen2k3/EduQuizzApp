package com.example.quizapp.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizapp.ui.main.MainScreen
import com.example.quizapp.ui.english.EnglishGamesScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onNavigateToEnglish = {
                    navController.navigate("english_games")
                }
            )
        }

        composable("english_games") {
            EnglishGamesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onGameClick = { game ->
                    // Navigate to specific game screen
                    // navController.navigate("game/${game.id}")
                }
            )
        }
    }
}