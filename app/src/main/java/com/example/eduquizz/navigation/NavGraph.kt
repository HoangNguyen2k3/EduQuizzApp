package com.example.eduquizz.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.eduquizz.features.ContestOnline.LeaderboardScreen
import com.example.eduquizz.features.admin.screens.AdminDashboardScreen
import com.example.eduquizz.features.admin.screens.GameManagementScreen
import com.example.eduquizz.features.admin.screens.QuestionListScreen
import com.example.eduquizz.features.admin.screens.QuestionEditorScreen
import com.example.eduquizz.features.admin.screens.ContestManagementScreen
import com.example.eduquizz.features.admin.screens.ContestEditorScreen
import com.example.eduquizz.features.admin.viewmodel.GameType
import com.example.eduquizz.features.auth.screens.LoginScreen
import com.example.eduquizz.features.auth.screens.RegisterScreen
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel
import com.example.eduquizz.features.contest.screens.ContestScreen
import com.example.eduquizz.features.mapping.model.Leaderboard
import com.example.eduquizz.features.soundgame.screen.SoundGameScreen
import com.example.eduquizz.features.soundgame.screen.SoundGameDescriptionScreen
import com.example.eduquizz.features.soundgame.viewmodel.SoundGameViewModel
// Import Match Game screens
import com.example.eduquizz.features.match.screen.MatchGameIntroScreen
import com.example.eduquizz.features.match.screen.MatchLevelSelectionScreen
import com.example.eduquizz.features.match.screen.MatchMainScreen
// Import Daily Login
import com.example.eduquizz.features.dailyLogin.screens.DailyLoginScreen

object Routes {
    //Main
    const val ENGLISH_GAMES_SCENE = "english_games_scene"
    const val MATH_GAMES_SCENE = "math_games_scene"
    const val MAPPING_GAMES_SCENE = "mapping_games_scene"
    const val CONTEST_GAMES_SCENE = "contest_games_scene"
    const val LEADERBOARD_GAMES_SCENE = "leaderboard_games_scene"
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

    // Auth
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Admin
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_GAME_MANAGEMENT = "admin_game_management/{gameType}"
    const val ADMIN_QUESTION_LIST = "admin_question_list/{gameType}/{levelId}"
    const val ADMIN_QUESTION_EDITOR = "admin_question_editor/{gameType}/{levelId}?questionId={questionId}"
    const val ADMIN_CONTEST_MANAGEMENT = "admin_contest_management"
    const val ADMIN_CONTEST_EDITOR = "admin_contest_editor?contestId={contestId}"
    
    // Daily Login
    const val DAILY_LOGIN = "daily_login"

    fun adminGameManagement(gameType: String) = "admin_game_management/$gameType"
    fun adminQuestionList(gameType: String, levelId: String) = "admin_question_list/$gameType/$levelId"
    fun adminQuestionEditor(gameType: String, levelId: String, questionId: String? = null) = 
        "admin_question_editor/$gameType/$levelId" + if (questionId != null) "?questionId=$questionId" else ""
    fun adminContestEditor(contestId: String? = null) = 
        "admin_contest_editor" + if (contestId != null) "?contestId=$contestId" else ""
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val userViewModel: UserViewModel = hiltViewModel()
    val dataViewModel: DataViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    val firstTime by dataViewModel.firstTime.observeAsState(0)
    val authUiState by authViewModel.uiState.collectAsState()


    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onLoginSuccess = {
                    android.util.Log.d("NavGraph", "Login success callback, firstTime=$firstTime")
                    
                    // Kiểm tra xem user có phải admin không
                    val isAdmin = authUiState.isAdmin
                    android.util.Log.d("NavGraph", "User isAdmin: $isAdmin")
                    
                    // Nếu là admin, chuyển thẳng đến Admin Dashboard
                    if (isAdmin) {
                        android.util.Log.d("NavGraph", "Admin user detected, navigating to Admin Dashboard")
                        navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        return@LoginScreen
                    }

                    // Re-check firstTime after login (cho user thường)
                    val shouldGoToReady = firstTime == false

                    android.util.Log.d("NavGraph", "shouldGoToReady=$shouldGoToReady")

                    if (shouldGoToReady) {
                        android.util.Log.d("NavGraph", "Navigating to READY")
                        navController.navigate(Routes.READY) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        android.util.Log.d("NavGraph", "Navigating to MAIN_DANH")
                        navController.navigate(Routes.MAIN_DANH) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    android.util.Log.d("NavGraph", "Register success - navigating to LOGIN")
                    // After successful registration, navigate to login screen
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    // Check if user is logged in
                    if (authUiState.isLoggedIn) {
                        if (firstTime == false) {
                            navController.navigate(Routes.READY) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.MAIN_DANH) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
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

        // Admin Dashboard
        composable(Routes.ADMIN_DASHBOARD) {
            val username = authViewModel.getCurrentUsername()

            AdminDashboardScreen(
                username = username,
                onBackClick = {
                    navController.navigate(Routes.MAIN_DANH) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                },
                onGameManagementClick = { gameType ->
                    navController.navigate(Routes.adminGameManagement(gameType.name))
                },
                onContestManagementClick = {
                    navController.navigate(Routes.ADMIN_CONTEST_MANAGEMENT)
                }
            )
        }

        // Admin Game Management
        composable(
            route = Routes.ADMIN_GAME_MANAGEMENT,
            arguments = listOf(navArgument("gameType") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameTypeString = backStackEntry.arguments?.getString("gameType") ?: "WORD_SEARCH"
            val gameType = try {
                GameType.valueOf(gameTypeString)
            } catch (e: Exception) {
                GameType.WORD_SEARCH
            }
            val username = authViewModel.getCurrentUsername()

            GameManagementScreen(
                username = username,
                gameType = gameType,
                onBackClick = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        // Admin Question List
        composable(
            route = Routes.ADMIN_QUESTION_LIST,
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val gameTypeString = backStackEntry.arguments?.getString("gameType") ?: "QUIZ"
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val gameType = try {
                GameType.valueOf(gameTypeString)
            } catch (e: Exception) {
                GameType.QUIZ
            }
            val username = authViewModel.getCurrentUsername()

            QuestionListScreen(
                username = username,
                gameType = gameType,
                onBackClick = {
                    navController.navigateUp()
                },
                onQuestionClick = { questionId ->
                    navController.navigate(
                        Routes.adminQuestionEditor(gameTypeString, levelId, questionId)
                    )
                },
                onAddQuestionClick = {
                    navController.navigate(
                        Routes.adminQuestionEditor(gameTypeString, levelId, null)
                    )
                }
            )
        }

        // Admin Question Editor
        composable(
            route = Routes.ADMIN_QUESTION_EDITOR,
            arguments = listOf(
                navArgument("gameType") { type = NavType.StringType },
                navArgument("levelId") { type = NavType.StringType },
                navArgument("questionId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val gameTypeString = backStackEntry.arguments?.getString("gameType") ?: "QUIZ"
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val questionId = backStackEntry.arguments?.getString("questionId")
            val gameType = try {
                GameType.valueOf(gameTypeString)
            } catch (e: Exception) {
                GameType.QUIZ
            }
            val username = authViewModel.getCurrentUsername()

            QuestionEditorScreen(
                username = username,
                gameType = gameType,
                levelId = levelId,
                questionId = questionId,
                onBackClick = {
                    navController.navigateUp()
                },
                onSaveSuccess = {
                    navController.navigateUp()
                }
            )
        }

        // Admin Contest Management
        composable(Routes.ADMIN_CONTEST_MANAGEMENT) {
            val username = authViewModel.getCurrentUsername()

            ContestManagementScreen(
                username = username,
                onBackClick = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = false }
                    }
                },
                onContestClick = { contestId ->
                    navController.navigate(Routes.adminContestEditor(contestId))
                },
                onCreateContestClick = {
                    navController.navigate(Routes.adminContestEditor(null))
                }
            )
        }

        // Admin Contest Editor
        composable(
            route = Routes.ADMIN_CONTEST_EDITOR,
            arguments = listOf(
                navArgument("contestId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val contestId = backStackEntry.arguments?.getString("contestId")
            val username = authViewModel.getCurrentUsername()

            ContestEditorScreen(
                username = username,
                contestId = contestId,
                onBackClick = {
                    navController.navigateUp()
                },
                onSaveSuccess = {
                    navController.navigateUp()
                }
            )
        }

        composable(Routes.MAIN_DANH) {
            MainScreen(
                onNavigateToEnglish = { navController.navigate(Routes.ENGLISH_GAMES_SCENE) },
                onNavigateToMath = { navController.navigate(Routes.MATH_GAMES_SCENE) },
                onNavigateToContest = { navController.navigate(Routes.CONTEST_GAMES_SCENE) },
                onNavigateToMapping = { navController.navigate(Routes.MAPPING_GAMES_SCENE) },
                onNavigateToLeaderBoard = { navController.navigate(Routes.LEADERBOARD_GAMES_SCENE) },
                onNavigateToAdmin = { navController.navigate(Routes.ADMIN_DASHBOARD) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToDailyLogin = {  // NEW: Daily Login navigation
                    navController.navigate(Routes.DAILY_LOGIN)
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                userViewModel = userViewModel,
                dataviewModel = dataViewModel,
                authViewModel = authViewModel
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
        composable(Routes.CONTEST_GAMES_SCENE) {
            ContestScreen(
                onBackClick = {
                    navController.navigate(Routes.MAIN_DANH)
                },
                navController = navController,
                userName = dataViewModel.playerName.observeAsState("User123").value
            )
        }
        composable(Routes.LEADERBOARD_GAMES_SCENE) {
            LeaderboardScreen(
                onBackClick = {
                    navController.navigate(Routes.MAIN_DANH)
                }
            )
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

        // Daily Login Screen
        composable(Routes.DAILY_LOGIN) {
            DailyLoginScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}