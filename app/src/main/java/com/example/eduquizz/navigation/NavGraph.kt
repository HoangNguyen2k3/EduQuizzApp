package com.example.eduquizz.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eduquizz.features.quizzGame.screens.IntroScreen
import com.example.eduquizz.features.quizzGame.screens.MainView
import com.example.eduquizz.features.quizzGame.screens.ResultsScreen
import com.example.eduquizz.features.quizzGame.viewmodel.QuestionViewModel
import com.example.eduquizz.features.match.screen.GameDescriptionScreen

import com.example.quizapp.screen.WordMatchGameScreen
import com.example.eduquizz.features.home.english.EnglishGamesScreen
import com.example.eduquizz.features.home.screens.SettingScreen
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.quizapp.ui.main.MainScreen
import com.example.quizapp.viewmodel.WordMatchGame
import com.example.quizapp.ui.splash.SplashScreen
import com.example.eduquizz.features.wordsearch.screens.IntroductionScreen
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
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
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
                MainView(
                    name = "Android",
                    navController = navController,
                    questionViewModel = viewModel
                )
            }
        }

        composable(Routes.INTRO) {
            Box(modifier = Modifier.fillMaxSize()) {
                IntroScreen(
                    navController,
                    onBackPressed = { navController.popBackStack() }
                )
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
        composable(Routes.MAIN_DANH) {
            MainScreen(
                onNavigateToEnglish = {
                    navController.navigate(Routes.GAME_SCENE)
                }
            )
        }
        composable(Routes.GAME_SCENE) {
            EnglishGamesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onGameClick = { game ->
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
            SettingScreen()
        }

        composable(Routes.INTRO_THONG) {
            GameDescriptionScreen(
                onPlayClick = { navController.navigate(Routes.GAME_THONG) },
                onBackPressed = { navController.popBackStack() },
                subject = "English"
            )
        }
        composable(Routes.INTRO_WORD_SEARCH) {
            val loadingViewModel: LoadingViewModel = hiltViewModel()
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
                    showContinueButton = false,
                    loadingViewModel = loadingViewModel
                )

            }
        }
        composable(Routes.GAME_WORD_SEARCH) {
            WordSearchGame()
        }
    }
}