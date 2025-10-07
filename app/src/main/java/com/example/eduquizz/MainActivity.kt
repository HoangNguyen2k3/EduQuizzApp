package com.example.eduquizz

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import com.example.eduquizz.navigation.NavGraph
import com.example.eduquizz.Screens.WordSearch.ui.theme.EduQuizzTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.FirebaseDatabase
import com.example.eduquizz.data_save.AudioManager
import com.example.eduquizz.features.widget.StreakManager
import com.example.eduquizz.features.widget.WidgetUpdateManager
import androidx.activity.viewModels
import com.example.eduquizz.data_save.DataViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val dataViewModel: DataViewModel by viewModels()

    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }
        // Lưu thời gian vào app
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataViewModel.updateLastSeenNow()
        Log.d("MainActivity", "✅ Updated lastSeen: ${System.currentTimeMillis()}")

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        AudioManager.init(this)
        StreakManager.updateStreak(this)
        //AudioManager.setBgmEnabled(true)

        initializeWidgetSystem()


        StreakManager.updateStreak(this)
        WidgetUpdateManager.scheduleWidgetUpdates(this)


        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Android 13+ cáº§n xin quyá»n thÃ´ng bÃ¡o
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {

            val navController = rememberNavController()
            NavGraph(navController = navController)

            LaunchedEffect(Unit) {
                handleWidgetIntent(navController)
            }

            EduQuizzTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0.dp)
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }


    private fun initializeWidgetSystem() {
        // Update streak khi mở app
        StreakManager.updateStreak(this)

        // Schedule periodic updates cho widgets
        WidgetUpdateManager.scheduleWidgetUpdates(this)

        println("Widget system initialized. Current streak: ${StreakManager.getCurrentStreak(this)}")
    }

    private fun handleWidgetIntent(navController: NavHostController) {
        val navigateTo = intent?.getStringExtra("navigate_to")

        when (navigateTo) {
            "mapping" -> {
                navController.navigate(com.example.eduquizz.navigation.Routes.MAPPING_GAMES_SCENE) {
                    popUpTo(com.example.eduquizz.navigation.Routes.MAIN_DANH) { inclusive = false }
                }
            }
            "batchu" -> {
                navController.navigate(com.example.eduquizz.navigation.Routes.IntroBatChu) {
                    popUpTo(com.example.eduquizz.navigation.Routes.MAIN_DANH) { inclusive = false }
                }
                println("Widget clicked: Navigate to BatChu")
            }
            "word_search" -> {
                navController.navigate(com.example.eduquizz.navigation.Routes.INTRO_WORD_SEARCH) {
                    popUpTo(com.example.eduquizz.navigation.Routes.MAIN_DANH) { inclusive = false }
                }
                println("Widget clicked: Navigate to WordSearch")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdateManager.updateAllWidgets(this)
        // Cập nhật lastSeen mỗi khi vào app
        dataViewModel.updateLastSeenNow()
        Log.d("MainActivity", "✅ onResume - Updated lastSeen: ${System.currentTimeMillis()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        AudioManager.release()
    }
}

@Composable
fun MainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentStreak by remember { mutableStateOf(StreakManager.getCurrentStreak(context)) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StreakCard(
                streak = currentStreak,
                onRefresh = {
                    currentStreak = StreakManager.getCurrentStreak(context)
                }
            )

            // Test buttons
            Button(
                onClick = {
                    StreakManager.updateStreak(context)
                    currentStreak = StreakManager.getCurrentStreak(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Streak (Daily Check)")
            }

            Button(
                onClick = {
                    StreakManager.addStreakBonus(context, 1)
                    currentStreak = StreakManager.getCurrentStreak(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Streak Bonus +1")
            }

            Button(
                onClick = {
                    WidgetUpdateManager.updateAllWidgets(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Force Update All Widgets")
            }

            Button(
                onClick = {
                    StreakManager.resetStreak(context)
                    currentStreak = 0
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset Streak")
            }

            // Streak info
            val streakInfo = remember { StreakManager.getStreakInfo(context) }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Streak Details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Current: ${streakInfo.currentStreak} days")
                    Text("Active: ${if (streakInfo.isActive) "Yes âœ“" else "No âœ—"}")
                    Text("Needs Update: ${if (streakInfo.needsUpdate) "Yes" else "No"}")
                    Text(
                        "Last Active: ${
                            if (streakInfo.lastActiveDate > 0)
                                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(streakInfo.lastActiveDate))
                            else "Never"
                        }"
                    )
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    streak: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (streak > 0) "Keep it going!" else "Start today!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " ðŸ”¥",
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}