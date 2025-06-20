package com.example.eduquizz.navigation

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eduquizz.Screens.QuizzTracNhiem.IntroScreen
import com.example.eduquizz.Screens.QuizzTracNhiem.MainView
import com.example.eduquizz.Screens.QuizzTracNhiem.ResultsScreen
import com.example.eduquizz.viewmodel.QuestionViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quizapp.screen.GameDescriptionScreen
import com.example.quizapp.screen.SettingsScreen
import com.example.quizapp.screen.WordMatchGameScreen
import com.example.quizapp.ui.english.EnglishGamesScreen
import com.example.quizapp.ui.main.MainScreen
import com.example.quizapp.viewmodel.WordMatchGame
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eduquizz.MainActivity
import com.example.quizapp.ui.splash.SplashScreen
import com.example.wordsearch.ui.screens.IntroductionScreen
import com.example.wordsearch.ui.screens.WordSearchGame
import com.example.wordsearch.ui.theme.WordSearchGameTheme

object Routes {
    //Hoang
    const val MAIN = "main"
    const val RESULT = "result"
    const val INTRO = "intro"
    //Danh
    const val MAIN_DANH = "main_danh"
    const val GAME_SCENE = "games_scene_danh"
    const val INTRO_WORD_SEARCH = "intro_word_search"
    const val GAME_WORD_SEARCH = "game_word_search"
    //Thong
    const val GAME_THONG = "game_thong"
    const val SETTINGS = "settings"
    const val INTRO_THONG = "game_intro_thong"

    //Splash
    const val SPLASH = "splash"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth }, // Slide in from the right
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth }, // Slide out to the left
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth }, // Slide in from the left when popping
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth }, // Slide out to the right when popping
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(Routes.MAIN_DANH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val viewModel: QuestionViewModel = hiltViewModel()
            Box(modifier = Modifier.fillMaxSize()) {
                MainView(name = "Android",navController=navController, questionViewModel = viewModel)
            }
        }

        composable(Routes.INTRO) {
            Box(modifier = Modifier.fillMaxSize()) {
                IntroScreen(navController)
            }
        }
        composable(
            "result/{correct}/{total}",
            arguments = listOf(
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments?.getInt("correct") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0

            ResultsScreen(navController, correctAnswers = correct, totalQuestions = total)
        }
        composable(Routes.MAIN_DANH){
            MainScreen(
                onNavigateToEnglish = {
                    navController.navigate(Routes.GAME_SCENE)
                }
            )
        }
        composable(Routes.GAME_SCENE){
            EnglishGamesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onGameClick = { game ->
                    // Navigate to specific game screen
                    // navController.navigate("game/${game.id}")
                    when (game.id) {
                        "word_find" -> navController.navigate(Routes.INTRO_WORD_SEARCH)
                        "connect_blocks" -> navController.navigate(Routes.INTRO_THONG)
                        "quiz" -> navController.navigate(Routes.INTRO)
                    }
                }
            )
        }
        composable(Routes.GAME_THONG) {
            val gameViewModel: WordMatchGame = hiltViewModel()
            WordMatchGameScreen(viewModel = gameViewModel, navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
        composable(Routes.INTRO_THONG.replace("{subject}", "{subject}")) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: ""
            GameDescriptionScreen(
                onPlayClick = { navController.navigate(Routes.GAME_THONG) }
            )
        }
        composable(Routes.INTRO_WORD_SEARCH){
         //   val showContinue = intent.getBooleanExtra("showContinue", false)

                WordSearchGameTheme {
                    IntroductionScreen(
                        onPlayClicked = {
                          //  finish()
                            navController.navigate(Routes.GAME_WORD_SEARCH)
                        },
                        onBackPressed = {
                          //  finish()
                            navController.popBackStack()
                        },
                        showContinueButton = false
                    )

            }
        }
        composable(Routes.GAME_WORD_SEARCH){
            WordSearchGame()
        }
    }
}