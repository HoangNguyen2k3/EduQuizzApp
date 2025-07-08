package com.example.eduquizz.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.eduquizz.features.match.screen.WordMatchGameScreen
import com.example.eduquizz.features.home.english.EnglishGamesScreen
import com.example.eduquizz.features.home.screens.SettingScreen
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.eduquizz.features.home.screens.MainScreen
import com.example.eduquizz.features.match.viewmodel.WordMatchGame
import com.example.eduquizz.features.BatChu.screens.Main_BatChu
import com.example.eduquizz.features.home.screens.SplashScreen
import com.example.eduquizz.features.wordsearch.screens.IntroductionScreen
import com.example.eduquizz.features.wordsearch.screens.WordSearchGame
import com.example.eduquizz.features.wordsearch.screens.CompletionScreen
import com.example.wordsearch.ui.theme.WordSearchGameTheme
import com.example.eduquizz.features.bubbleshot.screen.BubbleShotScreen
import com.example.eduquizz.features.bubbleshot.screen.BubbleShotDescriptionScreen
import com.example.eduquizz.features.bubbleshot.viewmodel.BubbleShot
import com.example.eduquizz.features.home.screens.ReadyScreen
import com.example.eduquizz.features.quizzGame.screens.LevelChoice
import com.example.eduquizz.features.wordsearch.screens.TopicSelectionScreen
import com.example.eduquizz.features.wordsearch.viewmodel.WordSearchViewModel
import com.example.eduquizz.data.local.UserViewModel

object Routes {
    const val MAIN = "main"
    const val RESULT = "result"
    const val INTRO = "intro"
    const val MAIN_ROUTE = "main/{level}"
    const val QUIZ_LEVEL = "quiz_level"
    const val MAIN_DANH = "main_danh"
    const val GAME_SCENE = "games_scene_danh"
    const val INTRO_WORD_SEARCH = "intro_word_search"
    const val GAME_WORD_SEARCH = "game_word_search"
    const val GAME_THONG = "game_thong"
    const val SETTINGS = "settings"
    const val INTRO_THONG = "game_intro_thong"
    const val BUBBLE_SHOT = "bubble_shot"
    const val BUBBLE_SHOT_INTRO = "bubble_shot_intro"
    const val SPLASH = "splash"
    const val READY = "ready"
    const val BatChu = "batchu"
}

@Composable
fun NavGraph(
        navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val userViewModel: UserViewModel = hiltViewModel()

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
                    navController.navigate(Routes.READY) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.READY) {
            ReadyScreen(
                onStartClick = { userName ->
                    navController.navigate(Routes.MAIN_DANH) {
                        popUpTo(Routes.READY) { inclusive = true }
                    }
                },
                userViewModel = userViewModel
            )
        }

        composable(Routes.MAIN_DANH) {
            MainScreen(
                onNavigateToEnglish = {
                    navController.navigate(Routes.GAME_SCENE)
                },
                userViewModel = userViewModel
            )
        }

        composable(route = "main/{level}", arguments = listOf(navArgument("level") { type = NavType.StringType })) { backStackEntry ->
            val level = backStackEntry.arguments?.getString("level") ?: ""
            val questionViewModel: QuestionViewModel = hiltViewModel()
            MainView(
                currentLevel = level,
                name = "Android",
                navController = navController,
                questionViewModel = questionViewModel
            )
        }

        composable(Routes.INTRO) {
            Box(modifier = Modifier.fillMaxSize()) {
                IntroScreen(
                    navController,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }

        composable("result/{correct}/{total}", arguments = listOf(
            navArgument("correct") { type = NavType.IntType },
            navArgument("total") { type = NavType.IntType }
        )) { backStackEntry ->
            val correct = backStackEntry.arguments?.getInt("correct") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            ResultsScreen(navController, correctAnswers = correct, totalQuestions = total)
        }

        composable(Routes.GAME_SCENE) {
            EnglishGamesScreen(
                onBackClick = { navController.popBackStack() },
                onGameClick = { game ->
                    when (game.id) {
                        "word_find" -> navController.navigate(Routes.INTRO_WORD_SEARCH)
                        "connect_blocks" -> navController.navigate(Routes.INTRO_THONG)
                        "quiz" -> navController.navigate(Routes.INTRO)
                        "bubble_shot" -> navController.navigate(Routes.BUBBLE_SHOT_INTRO)
                        "batchu" -> navController.navigate(Routes.BatChu)
                    }
                }
            )
        }

        composable(Routes.BatChu) {
            Main_BatChu(navController = navController)
        }

        composable(Routes.QUIZ_LEVEL) {
            LevelChoice(
                onBackClick = { navController.navigate(Routes.INTRO) },
                onGameClick = { game ->
                    when(game.id) {
                        "level_easy" -> navController.navigate("main/LevelEasy")
                        "level_normal" -> navController.navigate("main/LevelNormal")
                        "level_hard" -> navController.navigate("main/LevelHard")
                        "level_image" -> navController.navigate("main/LevelImage")
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
            WordSearchGameTheme {
                IntroductionScreen(
                    onPlayClicked = { navController.navigate("topic_selection") },
                    onBackPressed = { navController.popBackStack() },
                    showContinueButton = false,
                    loadingViewModel = loadingViewModel
                )
            }
        }

        composable("topic_selection") {
            val loadingViewModel: LoadingViewModel = hiltViewModel()
            TopicSelectionScreen(
                onTopicSelected = { topicId -> navController.navigate("word_search_game/$topicId") },
                onBackPressed = { navController.popBackStack() },
            )
        }

        composable(route = "word_search_game/{topicId}", arguments = listOf(navArgument("topicId") { type = NavType.StringType })) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: "Travel"
            val viewModel: WordSearchViewModel = hiltViewModel()
            LaunchedEffect(topicId) {
                viewModel.loadWordsFromFirebase(topicId)
            }
            WordSearchGame(
                topicId = topicId,
                viewModel = viewModel,
                onBackToIntroduction = { navController.popBackStack() },
                navController = navController
            )
        }

        composable(
            route = "completion/{topicId}/{totalWords}/{timeSpent}/{coinsEarned}",
            arguments = listOf(
                navArgument("topicId") { type = NavType.StringType },
                navArgument("totalWords") { type = NavType.IntType },
                navArgument("timeSpent") { type = NavType.StringType },
                navArgument("coinsEarned") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            val totalWords = backStackEntry.arguments?.getInt("totalWords") ?: 0
            val timeSpent = backStackEntry.arguments?.getString("timeSpent") ?: "00:00"
            val coinsEarned = backStackEntry.arguments?.getInt("coinsEarned") ?: 50

            CompletionScreen(
                topicName = topicId,
                totalWords = totalWords,
                timeSpent = timeSpent,
                coinsEarned = coinsEarned,
                onPlayAgain = {
                    navController.navigate("word_search_game/$topicId") {
                        popUpTo("completion/$topicId/$totalWords/$timeSpent/$coinsEarned") { inclusive = true }
                    }
                },
                onBackToTopics = {
                    navController.navigate("topic_selection") {
                        popUpTo("completion/$topicId/$totalWords/$timeSpent/$coinsEarned") { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Routes.MAIN_DANH) {
                        popUpTo("completion/$topicId/$totalWords/$timeSpent/$coinsEarned") { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.BUBBLE_SHOT_INTRO) {
            BubbleShotDescriptionScreen(
                onPlayClick = { navController.navigate(Routes.BUBBLE_SHOT) },
                onBackPressed = { navController.popBackStack() },
                subject = "BubbleShot"
            )
        }

        composable(Routes.BUBBLE_SHOT) {
            val viewModel: BubbleShot = hiltViewModel()
            BubbleShotScreen(viewModel = viewModel, navController = navController)
        }

        composable(Routes.GAME_WORD_SEARCH) {
            WordSearchGame(navController = navController)
        }
    }
}