//package com.example.eduquizz.features.mapping.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import com.google.android.gms.maps.model.LatLng
//import javax.inject.Inject
//import kotlin.math.*
//
//data class MappingQuestion(
//    val id: Int,
//    val question: String,
//    val correctAnswer: String,
//    val correctLocation: LatLng,
//    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM
//)
//
//enum class QuestionDifficulty {
//    EASY, MEDIUM, HARD
//}
//
//data class MappingGameState(
//    val currentQuestion: MappingQuestion? = null,
//    val currentQuestionIndex: Int = 0,
//    val totalQuestions: Int = 10,
//    val score: Int = 0,
//    val hasAnswered: Boolean = false,
//    val showAnswer: Boolean = false,
//    val userGuessLocation: LatLng? = null,
//    val targetCountryLocation: LatLng? = null,
//    val lastAnswerDistance: Double? = null,
//    val lastScore: Int = 0,
//    val isGameFinished: Boolean = false,
//    val questions: List<MappingQuestion> = emptyList()
//)
//
//@HiltViewModel
//class MappingGameViewModel @Inject constructor() : ViewModel() {
//
//    private val _gameState = MutableStateFlow(MappingGameState())
//    val gameState: StateFlow<MappingGameState> = _gameState.asStateFlow()
//
//    private val sampleQuestions = listOf(
//        MappingQuestion(
//            1, "Where is France located?", "France",
//            LatLng(46.6034, 1.8883), QuestionDifficulty.EASY
//        ),
//        MappingQuestion(
//            2, "Where is Japan located?", "Japan",
//            LatLng(36.2048, 138.2529), QuestionDifficulty.EASY
//        ),
//        MappingQuestion(
//            3, "Where is Brazil located?", "Brazil",
//            LatLng(-14.2350, -51.9253), QuestionDifficulty.EASY
//        ),
//        MappingQuestion(
//            4, "Where is Egypt located?", "Egypt",
//            LatLng(26.0975, 31.2357), QuestionDifficulty.MEDIUM
//        ),
//        MappingQuestion(
//            5, "Where is Norway located?", "Norway",
//            LatLng(60.4720, 8.4689), QuestionDifficulty.MEDIUM
//        ),
//        MappingQuestion(
//            6, "Where is Chile located?", "Chile",
//            LatLng(-35.6751, -71.5430), QuestionDifficulty.MEDIUM
//        ),
//        MappingQuestion(
//            7, "Where is Madagascar located?", "Madagascar",
//            LatLng(-18.7669, 46.8691), QuestionDifficulty.HARD
//        ),
//        MappingQuestion(
//            8, "Where is Kazakhstan located?", "Kazakhstan",
//            LatLng(48.0196, 66.9237), QuestionDifficulty.HARD
//        ),
//        MappingQuestion(
//            9, "Where is New Zealand located?", "New Zealand",
//            LatLng(-40.9006, 174.8860), QuestionDifficulty.MEDIUM
//        ),
//        MappingQuestion(
//            10, "Where is Iceland located?", "Iceland",
//            LatLng(64.9631, -19.0208), QuestionDifficulty.HARD
//        )
//    )
//
//    init {
//        startNewGame()
//    }
//
//    fun startNewGame() {
//        viewModelScope.launch {
//            val shuffledQuestions = sampleQuestions.shuffled().take(10)
//            _gameState.value = MappingGameState(
//                questions = shuffledQuestions,
//                currentQuestion = shuffledQuestions.firstOrNull(),
//                totalQuestions = shuffledQuestions.size
//            )
//        }
//    }
//
//    fun onMapClick(latLng: LatLng) {
//        val currentState = _gameState.value
//        if (currentState.hasAnswered || currentState.showAnswer) return
//
//        _gameState.value = currentState.copy(
//            userGuessLocation = latLng
//        )
//    }
//
//    fun submitAnswer() {
//        val currentState = _gameState.value
//        val userGuess = currentState.userGuessLocation ?: return
//        val currentQuestion = currentState.currentQuestion ?: return
//
//        val distance = calculateDistance(userGuess, currentQuestion.correctLocation)
//        val scoreEarned = calculateScore(distance, currentQuestion.difficulty)
//
//        _gameState.value = currentState.copy(
//            hasAnswered = true,
//            showAnswer = true,
//            targetCountryLocation = currentQuestion.correctLocation,
//            lastAnswerDistance = distance,
//            lastScore = scoreEarned,
//            score = currentState.score + scoreEarned
//        )
//    }
//
//    fun nextQuestion() {
//        val currentState = _gameState.value
//        val nextIndex = currentState.currentQuestionIndex + 1
//
//        if (nextIndex >= currentState.totalQuestions) {
//            _gameState.value = currentState.copy(
//                isGameFinished = true
//            )
//        } else {
//            _gameState.value = currentState.copy(
//                currentQuestion = currentState.questions[nextIndex],
//                currentQuestionIndex = nextIndex,
//                hasAnswered = false,
//                showAnswer = false,
//                userGuessLocation = null,
//                targetCountryLocation = null,
//                lastAnswerDistance = null,
//                lastScore = 0
//            )
//        }
//    }
//
//    fun restartGame() {
//        startNewGame()
//    }
//
//    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
//        val earthRadius = 6371 // Radius of the earth in km
//
//        val lat1Rad = Math.toRadians(point1.latitude)
//        val lat2Rad = Math.toRadians(point2.latitude)
//        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
//        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)
//
//        val a = sin(deltaLatRad / 2) * sin(deltaLatRad / 2) +
//                cos(lat1Rad) * cos(lat2Rad) *
//                sin(deltaLngRad / 2) * sin(deltaLngRad / 2)
//        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
//
//        return earthRadius * c // Distance in km
//    }
//
//    private fun calculateScore(distanceKm: Double, difficulty: QuestionDifficulty): Int {
//        val baseScore = when (difficulty) {
//            QuestionDifficulty.EASY -> 100
//            QuestionDifficulty.MEDIUM -> 150
//            QuestionDifficulty.HARD -> 200
//        }
//
//        return when {
//            distanceKm < 100 -> baseScore
//            distanceKm < 500 -> (baseScore * 0.8).toInt()
//            distanceKm < 1000 -> (baseScore * 0.6).toInt()
//            distanceKm < 2000 -> (baseScore * 0.4).toInt()
//            distanceKm < 5000 -> (baseScore * 0.2).toInt()
//            else -> (baseScore * 0.1).toInt()
//        }
//    }
//}