package com.example.eduquizz.features.mapping.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import com.example.eduquizz.features.mapping.model.SceneLocation // Import from model
import com.example.eduquizz.features.mapping.model.SceneLevel    // Import from model
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import kotlin.math.*

// Remove the local data class definitions - use the ones from model package

data class OSMGameState(
    val currentLocation: SceneLocation? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 10,
    val score: Int = 0,
    val hasAnswered: Boolean = false,
    val showAnswer: Boolean = false,
    val userGuessLocation: GeoPoint? = null,
    val correctLocation: GeoPoint? = null,
    val lastAnswerDistance: Double? = null,
    val lastScore: Int = 0,
    val isGameFinished: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val locations: List<SceneLocation> = emptyList()
)

@HiltViewModel
class OSMMappingGameViewModel @Inject constructor(
    private val sceneRepository: SceneRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(OSMGameState())
    val gameState: StateFlow<OSMGameState> = _gameState.asStateFlow()

    fun loadGameData(levelId: String? = null) {
        viewModelScope.launch {
            _gameState.value = _gameState.value.copy(isLoading = true, errorMessage = null)

            try {
                if (levelId != null) {
                    // Load specific level
                    val level = sceneRepository.getSceneLevel(levelId)
                    if (level != null) {
                        startGameWithLevel(level)
                    } else {
                        _gameState.value = _gameState.value.copy(
                            isLoading = false,
                            errorMessage = "Level not found: $levelId"
                        )
                        startGameWithSampleData()
                    }
                } else {
                    // Load available levels from backend
                    val levels = sceneRepository.getAllLevels()

                    if (levels.isNotEmpty()) {
                        // Select first level or random level
                        val selectedLevel = levels.first()
                        startGameWithLevel(selectedLevel)
                    } else {
                        // Fallback to sample data
                        startGameWithSampleData()
                    }
                }
            } catch (e: Exception) {
                println("Error loading game data: ${e.message}")
                e.printStackTrace()
                _gameState.value = _gameState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load game data: ${e.message}"
                )
                // Fallback to sample data
                startGameWithSampleData()
            }
        }
    }

    private fun startGameWithLevel(level: SceneLevel) {
        val shuffledLocations = level.locations.shuffled()

        _gameState.value = OSMGameState(
            locations = shuffledLocations,
            currentLocation = shuffledLocations.firstOrNull(),
            totalQuestions = shuffledLocations.size,
            isLoading = false
        )
    }

    private fun startGameWithSampleData() {
        // Use sample data from SampleLocationData
        val sampleLevel = com.example.eduquizz.features.mapping.model.SampleLocationData.createSampleLevel()
        val shuffledLocations = sampleLevel.locations.shuffled()

        _gameState.value = OSMGameState(
            locations = shuffledLocations,
            currentLocation = shuffledLocations.firstOrNull(),
            totalQuestions = shuffledLocations.size,
            isLoading = false
        )
    }

    fun onMapClick(geoPoint: GeoPoint) {
        val currentState = _gameState.value
        if (currentState.hasAnswered || currentState.showAnswer || currentState.isLoading) return

        _gameState.value = currentState.copy(
            userGuessLocation = geoPoint
        )
    }

    fun submitAnswer() {
        val currentState = _gameState.value
        val userGuess = currentState.userGuessLocation ?: return
        val currentLocation = currentState.currentLocation ?: return

        val correctLocation = GeoPoint(currentLocation.latitude, currentLocation.longitude)
        val distance = calculateDistance(userGuess, correctLocation)
        val scoreEarned = calculateScore(distance, currentLocation.difficulty)

        viewModelScope.launch {
            try {
                // Save guess to backend
                sceneRepository.submitGuess(
                    imageId = currentLocation.locationId,
                    guessLat = userGuess.latitude,
                    guessLon = userGuess.longitude
                )
            } catch (e: Exception) {
                // Continue with local scoring if backend fails
                println("Failed to save guess: ${e.message}")
            }
        }

        _gameState.value = currentState.copy(
            hasAnswered = true,
            showAnswer = true,
            correctLocation = correctLocation,
            lastAnswerDistance = distance,
            lastScore = scoreEarned,
            score = currentState.score + scoreEarned
        )
    }

    fun nextQuestion() {
        val currentState = _gameState.value
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex >= currentState.totalQuestions) {
            // Game finished - save completion
            viewModelScope.launch {
                try {
                    sceneRepository.saveLevelCompletion(
                        userName = "current_user", // Replace with actual user
                        levelId = "default_level",
                        isCompleted = true,
                        timeSpent = "0" // Calculate actual time
                    )
                } catch (e: Exception) {
                    println("Failed to save completion: ${e.message}")
                }
            }

            _gameState.value = currentState.copy(
                isGameFinished = true
            )
        } else {
            _gameState.value = currentState.copy(
                currentLocation = currentState.locations[nextIndex],
                currentQuestionIndex = nextIndex,
                hasAnswered = false,
                showAnswer = false,
                userGuessLocation = null,
                correctLocation = null,
                lastAnswerDistance = null,
                lastScore = 0
            )
        }
    }

    fun restartGame() {
        loadGameData()
    }

    private fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadius = 6371.0 // Radius of the earth in km

        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLngRad / 2) * sin(deltaLngRad / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Distance in km
    }

    private fun calculateScore(distanceKm: Double, difficulty: String): Int {
        val baseScore = when (difficulty.uppercase()) {
            "EASY" -> 100
            "MEDIUM" -> 150
            "HARD" -> 200
            else -> 150
        }

        return when {
            distanceKm < 50 -> baseScore
            distanceKm < 100 -> (baseScore * 0.9).toInt()
            distanceKm < 250 -> (baseScore * 0.8).toInt()
            distanceKm < 500 -> (baseScore * 0.7).toInt()
            distanceKm < 1000 -> (baseScore * 0.6).toInt()
            distanceKm < 2000 -> (baseScore * 0.4).toInt()
            distanceKm < 5000 -> (baseScore * 0.2).toInt()
            else -> (baseScore * 0.1).toInt()
        }
    }
}