package com.example.eduquizz.features.mapping.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eduquizz.features.mapping.viewmodel.OSMGameState
import com.example.eduquizz.features.mapping.viewmodel.OSMMappingGameViewModel
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingMainScreen(
    levelId: String,
    onBackPressed: () -> Unit,
    mappingGameViewModel: OSMMappingGameViewModel = hiltViewModel()
) {
    // Initialize the game with the specific level ID
    LaunchedEffect(levelId) {
        mappingGameViewModel.loadGameData(levelId) // Pass the levelId here
    }

    val context = LocalContext.current
    val gameState by mappingGameViewModel.gameState.collectAsState()

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var mapController by remember { mutableStateOf<IMapController?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // OpenStreetMap
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    controller.setZoom(2.0)
                    controller.setCenter(GeoPoint(0.0, 0.0))

                    mapView = this
                    mapController = controller

                    // Map click listener
                    setOnTouchListener { _, event ->
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            val projection = projection
                            val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                            mappingGameViewModel.onMapClick(geoPoint)
                        }
                        false
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Update map markers when game state changes
                view.overlays.clear()

                // Add user guess marker
                gameState.userGuessLocation?.let { guess ->
                    val marker = Marker(view)
                    marker.position = guess
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Your Guess"
                    view.overlays.add(marker)
                }

                // Add correct location marker when answer is shown
                if (gameState.showAnswer) {
                    gameState.correctLocation?.let { correct ->
                        val marker = Marker(view)
                        marker.position = correct
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Correct Location"
                        view.overlays.add(marker)
                    }

                    // Add distance line
                    if (gameState.userGuessLocation != null && gameState.correctLocation != null) {
                        val polyline = Polyline()
                        polyline.addPoint(gameState.userGuessLocation!!)
                        polyline.addPoint(gameState.correctLocation!!)
                        polyline.color = android.graphics.Color.YELLOW
                        polyline.width = 5f
                        view.overlays.add(polyline)
                    }
                }

                view.invalidate()
            }
        )

        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Geography Scene Challenge",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2E7D32).copy(alpha = 0.9f)
            ),
            modifier = Modifier.zIndex(1f)
        )

        // Game Info Panel
        GameInfoPanel(
            gameState = gameState,
            onAnswerSubmit = { mappingGameViewModel.submitAnswer() },
            onNextQuestion = { mappingGameViewModel.nextQuestion() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        )

        // Score and Progress
        ScorePanel(
            score = gameState.score,
            currentQuestion = gameState.currentQuestionIndex + 1,
            totalQuestions = gameState.totalQuestions,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
                .zIndex(1f)
        )

        // Loading indicator
        if (gameState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading level data...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Error message
        gameState.errorMessage?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .zIndex(3f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: $errorMessage",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Game Over Dialog
        if (gameState.isGameFinished) {
            GameOverDialog(
                score = gameState.score,
                totalQuestions = gameState.totalQuestions,
                onPlayAgain = { mappingGameViewModel.restartGame() },
                onExit = onBackPressed
            )
        }
    }
}

@Composable
private fun GameInfoPanel(
    gameState: OSMGameState,
    onAnswerSubmit: () -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Current location image
            gameState.currentLocation?.let { location ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    AsyncImage(
                        model = location.imageUrl,
                        contentDescription = "Location to guess",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Where is this location?",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Show location name if answer is revealed
                if (gameState.showAnswer) {
                    Text(
                        text = location.locationName,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Show additional location info
                    if (location.description.isNotEmpty()) {
                        Text(
                            text = location.description,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    if (location.country.isNotEmpty() || location.region.isNotEmpty()) {
                        Text(
                            text = "${location.region}${if (location.region.isNotEmpty() && location.country.isNotEmpty()) ", " else ""}${location.country}",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF666666)
                        )
                    }
                }

                // Instruction
                if (!gameState.hasAnswered) {
                    Text(
                        text = "Tap on the map to place your guess",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Answer feedback
            if (gameState.showAnswer) {
                gameState.lastAnswerDistance?.let { distance ->
                    val accuracy = when {
                        distance < 100 -> "Excellent!"
                        distance < 500 -> "Great!"
                        distance < 1000 -> "Good!"
                        distance < 2000 -> "Not bad!"
                        else -> "Keep trying!"
                    }

                    Text(
                        text = accuracy,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Distance: ${String.format("%.1f", distance)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Points earned: ${gameState.lastScore}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Submit/Next Button
                Button(
                    onClick = if (gameState.showAnswer) onNextQuestion else onAnswerSubmit,
                    enabled = if (gameState.showAnswer) true else gameState.userGuessLocation != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (gameState.showAnswer) "Next Location" else "Submit Guess",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Hint Button (optional)
                if (!gameState.showAnswer && gameState.currentLocation != null) {
                    IconButton(
                        onClick = { /* Show hint functionality */ },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF4CAF50),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Show hint",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorePanel(
    score: Int,
    currentQuestion: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E7D32).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "$currentQuestion/$totalQuestions",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GameOverDialog(
    score: Int,
    totalQuestions: Int,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Challenge Complete!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Final Score: $score",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You completed $totalQuestions locations!",
                    textAlign = TextAlign.Center
                )

                val accuracy = if (totalQuestions > 0) (score / (totalQuestions * 200.0) * 100).toInt() else 0
                Text(
                    text = "Accuracy: $accuracy%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text("Play Again", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onExit) {
                Text("Exit", color = Color(0xFF2E7D32))
            }
        }
    )
}