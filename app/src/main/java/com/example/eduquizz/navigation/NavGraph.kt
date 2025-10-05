package com.example.eduquizz.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.eduquizz.features.home.math.MathGamesScreen
import com.example.eduquizz.features.home.english.EnglishGamesScreen
import com.example.eduquizz.features.home.screens.SettingScreen
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.eduquizz.features.home.screens.MainScreen
import com.example.eduquizz.data.local.UserViewModel
import com.example.eduquizz.features.BatChu.screens.IntroScreenBatChu
import com.example.eduquizz.features.BatChu.screens.LevelChoiceBatChu
import com.example.eduquizz.features.BatChu.screens.Main_BatChu
import com.example.eduquizz.features.home.screens.SplashScreen
import com.example.eduquizz.features.wordsearch.screens.IntroductionScreen
import com.example.eduquizz.features.wordsearch.screens.WordSearchGame
import com.example.wordsearch.ui.theme.WordSearchGameTheme
import com.example.eduquizz.features.bubbleshot.screen.BubbleShotScreen
import com.example.eduquizz.features.bubbleshot.screen.BubbleShotDescriptionScreen
import com.example.eduquizz.features.bubbleshot.viewmodel.BubbleShot
import com.example.eduquizz.features.home.screens.ReadyScreen
import com.example.eduquizz.features.mapping.screens.MappingGamesIntroductionScreen
import com.example.eduquizz.features.mapping.screens.MappingLevelSelectionScreen
import com.example.eduquizz.features.mapping.screens.MappingMainScreen
import com.example.eduquizz.features.quizzGame.screens.LevelChoice
import com.example.eduquizz.features.wordsearch.screens.TopicSelectionScreen
import com.example.eduquizz.features.wordsearch.viewmodel.WordSearchViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.features.soundgame.screen.SoundGameScreen
import com.example.eduquizz.features.soundgame.screen.SoundGameDescriptionScreen
import com.example.eduquizz.features.soundgame.viewmodel.SoundGameViewModel
// Import Match Game screens
import com.example.eduquizz.features.match.screen.MatchGameIntroScreen
import com.example.eduquizz.features.match.screen.MatchLevelSelectionScreen
import com.example.eduquizz.features.match.screen.MatchMainScreen

object Routes {
    //Main
    const val ENGLISH_GAMES_SCENE = "english_games_scene"
    const val MATH_GAMES_SCENE = "math_games_scene"
    const val MAPPING_GAMES_SCENE = "mapping_games_scene"
    const val MAPPING_LEVEL_SELECTION = "mapping_level_selection"
    const val MAPPING_MAIN_GAME = "mapping_main_game/{levelId}"
    //Hoang
    const val INTRO = "intro"
    const val QUIZ_LEVEL = "quiz_level"
    //Danh
    const val MAIN_DANH = "main_danh"
    const val GAME_SCENE = "games_scene_danh"
    const val INTRO_WORD_SEARCH = "intro_word_search"
    const val GAME_WORD_SEARCH = "game_word_search"
    //Thong
    const val GAME_THONG = "game_thong"
    const val SETTINGS = "settings"
    const val INTRO_THONG = "game_intro_thong"
    // Bubble Shot
    const val BUBBLE_SHOT = "bubble_shot"
    const val BUBBLE_SHOT_INTRO = "bubble_shot_intro"
    //Splash
    const val SPLASH = "splash"
    const val READY ="ready"
    //BatChu
    const val BatChu = "batchu"
    const val IntroBatChu = "introbatchu"
    const val LevelBatChu = "levelbatchu"
    //Mapping
    //Sound Game
    const val SOUND_GAME = "sound_game/{levelId}"
    const val SOUND_GAME_INTRO = "sound_game_intro"
    //Match Game
    const val MATCH_GAME_INTRO = "match_game_intro"
    const val MATCH_GAME_LEVEL_SELECTION = "match_game_level_selection"
    const val MATCH_GAME_MAIN = "match_game_main/{levelId}"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val userViewModel: UserViewModel = hiltViewModel()
    val dataViewModel: DataViewModel = hiltViewModel()
    val firstTime by dataViewModel.firstTime.observeAsState(0)
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
                    if(firstTime == false){
                        navController.navigate(Routes.READY) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }else{
                        navController.navigate(Routes.MAIN_DANH)
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
                    navController.navigate(Routes.ENGLISH_GAMES_SCENE)
                },
                onNavigateToMath = {
                    navController.navigate(Routes.MATH_GAMES_SCENE)
                },
                onNavigateToMapping = {
                    navController.navigate(Routes.MAPPING_GAMES_SCENE)
                },
                userViewModel = userViewModel
            )
        }

        composable( route = "main/{level}",
            arguments = listOf(navArgument("level") { type = NavType.StringType })) {
                backStackEntry ->
            val level = backStackEntry.arguments?.getString("level") ?: ""
            val questionViewModel: QuestionViewModel = hiltViewModel()
            MainView(
                currentLevel = level,
                name = "Android",
                navController = navController,
                questionViewModel = questionViewModel
            )
        }

        composable(
            "result/{correct}/{total}/{route_back}/{route_again}",
            arguments = listOf(
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("route_back") { type = NavType.StringType },
                navArgument("route_again") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments?.getInt("correct") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val route_back = backStackEntry.arguments?.getString("route_back")?:""
            val route_again = backStackEntry.arguments?.getString("route_again")?:""
            ResultsScreen(navController, correctAnswers = correct, totalQuestions = total, back_route = route_back, play_agian_route = route_again)
        }

        composable(Routes.ENGLISH_GAMES_SCENE) {
            EnglishGamesScreen(
                onBackClick = {
                    navController.navigate(Routes.MAIN_DANH)
                },
                onGameClick = { game ->
                    when (game.id) {
                        "word_find" -> navController.navigate("${Routes.INTRO_WORD_SEARCH}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "connect_blocks" -> navController.navigate("${Routes.INTRO_THONG}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "quiz" -> navController.navigate("${Routes.INTRO}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "bubble_shot" -> navController.navigate("${Routes.BUBBLE_SHOT_INTRO}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "batchu" -> navController.navigate("${Routes.IntroBatChu}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "sound_game" -> navController.navigate("${Routes.SOUND_GAME_INTRO}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        "match_game" -> navController.navigate("${Routes.MATCH_GAME_INTRO}?from=${Routes.ENGLISH_GAMES_SCENE}")
                        else -> {
                            // Handle other games or show an error
                        }

                    }
                }
            )
        }
        composable(Routes.MATH_GAMES_SCENE) {
            MathGamesScreen(
                onBackClick = {
                    navController.navigate(Routes.MAIN_DANH)
                },
                onGameClick = { game ->
                    when (game.id) {
                        "connect_blocks" -> navController.navigate("${Routes.INTRO_THONG}?from=${Routes.MATH_GAMES_SCENE}")
                        "quiz" -> navController.navigate("${Routes.INTRO}?from=${Routes.MATH_GAMES_SCENE}")
                        "bubble_shot" -> navController.navigate("${Routes.BUBBLE_SHOT_INTRO}?from=${Routes.MATH_GAMES_SCENE}")
                        "match_game" -> navController.navigate("${Routes.MATCH_GAME_INTRO}?from=${Routes.MATH_GAMES_SCENE}")
                        else -> {
                            // Handle other games or show an error
                        }
                    }
                }
            )
        }

        // Match Game Routes
        composable(
            route = "${Routes.MATCH_GAME_INTRO}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            MatchGameIntroScreen(
                onPlayClick = {
                    navController.navigate(Routes.MATCH_GAME_LEVEL_SELECTION) {
                        popUpTo(Routes.MATCH_GAME_INTRO) { inclusive = false }
                    }
                },
                onBackPressed = { navController.navigate(from) }
            )
        }

        composable(Routes.MATCH_GAME_LEVEL_SELECTION) {
            MatchLevelSelectionScreen(
                onLevelSelected = { levelId ->
                    navController.navigate("match_game_main/$levelId") {
                        popUpTo(Routes.MATCH_GAME_LEVEL_SELECTION) { inclusive = false }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "match_game_main/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "Level1"
            val userName = userViewModel.userName.value ?: "defaultUser"

            MatchMainScreen(
                levelId = levelId,
                navController = navController,
                userName = userName,
                onBackPressed = {
                    navController.navigate(Routes.MATCH_GAME_LEVEL_SELECTION) {
                        popUpTo(Routes.MATCH_GAME_LEVEL_SELECTION) { inclusive = false }
                    }
                }
            )
        }

        // Updated navigation composables
        composable(Routes.MAPPING_GAMES_SCENE) {
            MappingGamesIntroductionScreen(
                onPlayClicked = {
                    // Navigate to level selection instead of directly to main game
                    navController.navigate(Routes.MAPPING_LEVEL_SELECTION) {
                        popUpTo(Routes.MAPPING_GAMES_SCENE) {
                            inclusive = false
                        }
                    }
                },
                onBackPressed = {
                    navController.navigate(Routes.MAIN_DANH) {
                        popUpTo(Routes.MAIN_DANH) {
                            inclusive = false
                        }
                    }
                },
                showContinueButton = false
            )
        }

        composable(Routes.MAPPING_LEVEL_SELECTION) {
            MappingLevelSelectionScreen(
                onLevelSelected = { levelId ->
                    // Navigate to main game with selected level ID
                    navController.navigate("mapping_main_game/$levelId") {
                        popUpTo(Routes.MAPPING_LEVEL_SELECTION) {
                            inclusive = false
                        }
                    }
                },
                onBackPressed = {
                    navController.navigate(Routes.MAPPING_GAMES_SCENE) {
                        popUpTo(Routes.MAPPING_GAMES_SCENE) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = "mapping_main_game/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "LevelEasy"

            MappingMainScreen(
                levelId = levelId,
                onBackPressed = {
                    navController.navigate(Routes.MAPPING_LEVEL_SELECTION) {
                        popUpTo(Routes.MAPPING_LEVEL_SELECTION) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(
            route = "${Routes.IntroBatChu}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            Box(modifier = Modifier.fillMaxSize()) {
                IntroScreenBatChu(
                    navController,
                    onBackPressed = { navController.navigate(from) },
                )
            }
        }
        composable("batchu/{level}",
            arguments = listOf(
                navArgument("level") { type = NavType.StringType },
            )) {
                backStackEntry ->
            val level = backStackEntry.arguments?.getString("level")?:""
            Main_BatChu(navController, currentLevel = level)
        }
        composable(Routes.LevelBatChu) {
            LevelChoiceBatChu(
                onBackClick = {navController.navigate(Routes.IntroBatChu)},
                onGameClick = {
                        game ->
                    when(game.id){
                        "LevelEasy"->navController.navigate("batchu/LevelEasy")
                        "LevelNormal"->navController.navigate("batchu/LevelNormal")
                        "LevelHard"->navController.navigate("batchu/LevelHard")
                    }
                }
            )
        }

        //Intro Quiz
        composable(
            route = "${Routes.INTRO}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            Box(modifier = Modifier.fillMaxSize()) {
                IntroScreen(
                    navController,
                    onBackPressed = { navController.navigate(from) },
                )
            }
        }
        composable(
            route = "${Routes.QUIZ_LEVEL}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            LevelChoice(
                onBackClick = { navController.navigate("${Routes.INTRO}?from=$from") },
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

        composable(
            route = "${Routes.BUBBLE_SHOT_INTRO}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            BubbleShotDescriptionScreen(
                onPlayClick = { navController.navigate(Routes.BUBBLE_SHOT) },
                onBackPressed = { navController.navigate(from) },
                subject = "BubbleShot"
            )
        }
        composable(Routes.BUBBLE_SHOT) {
            val viewModel: BubbleShot = hiltViewModel()
            BubbleShotScreen(viewModel = viewModel, navController = navController)
        }

        // Sound Game Routes
        composable(
            route = "${Routes.SOUND_GAME_INTRO}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            SoundGameDescriptionScreen(
                onPlayClick = { levelId ->
                    navController.navigate("sound_game/$levelId")
                },
                onBackPressed = { navController.navigate(from) }
            )
        }

        composable(
            route = "sound_game/{levelId}",
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "LevelEasy"
            val viewModel: SoundGameViewModel = hiltViewModel()
            SoundGameScreen(
                viewModel = viewModel,
                navController = navController,
                levelId = levelId
            )
        }

        composable(
            route = "${Routes.INTRO_WORD_SEARCH}?from={from}",
            arguments = listOf(navArgument("from") { defaultValue = Routes.ENGLISH_GAMES_SCENE; type = NavType.StringType })
        ) { backStackEntry ->
            val from = backStackEntry.arguments?.getString("from") ?: Routes.ENGLISH_GAMES_SCENE
            val loadingViewModel: LoadingViewModel = hiltViewModel()
            WordSearchGameTheme {
                IntroductionScreen(
                    onPlayClicked = {
                        navController.navigate("topic_selection")
                    },
                    onBackPressed = { navController.navigate(from) },
                    showContinueButton = false,
                    loadingViewModel = loadingViewModel
                )
            }
        }
        composable(Routes.GAME_WORD_SEARCH) {
            WordSearchGame(navController = navController)
        }
        composable(
            route = "word_search_game/{topicId}",
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: "Travel"
            val viewModel: WordSearchViewModel = hiltViewModel()

            LaunchedEffect(topicId) {
                viewModel.loadWordsFromFirebase(topicId)
            }

            WordSearchGame(
                viewModel = viewModel,
                onBackToIntroduction = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("topic_selection") {
            TopicSelectionScreen(
                onTopicSelected = { topicId ->
                    navController.navigate("word_search_game/$topicId")
                },
                onBackPressed = { navController.navigate(Routes.INTRO_WORD_SEARCH) },
            )
        }

        composable(Routes.SETTINGS) {
            SettingScreen()
        }
    }
}