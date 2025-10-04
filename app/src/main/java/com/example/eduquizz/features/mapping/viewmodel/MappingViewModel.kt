package com.example.eduquizz.features.mapping.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.mapping.model.SceneLevel
import com.example.eduquizz.features.mapping.model.SceneLocation
import com.example.eduquizz.features.mapping.model.SampleLocationData
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import com.example.eduquizz.features.widget.WidgetUpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MappingViewModel @Inject constructor(
    private val repository: SceneRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MappingViewModel"
    }

    private val _currentLevel = MutableStateFlow<SceneLevel?>(null)
    val currentLevel: StateFlow<SceneLevel?> = _currentLevel.asStateFlow()

    private val _availableLevels = MutableStateFlow<List<SceneLevel>>(emptyList())
    val availableLevels: StateFlow<List<SceneLevel>> = _availableLevels.asStateFlow()

    private val _currentQuestions = MutableStateFlow<List<SceneLocation>>(emptyList())
    val currentQuestions: StateFlow<List<SceneLocation>> = _currentQuestions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Statistics
    private val _countryCount = mutableStateOf(0)
    val countryCount: State<Int> = _countryCount

    private val _continentCount = mutableStateOf(3) // Vietnam has 3 main regions
    val continentCount: State<Int> = _continentCount

    private val _totalQuestions = mutableStateOf(0)
    val totalQuestions: State<Int> = _totalQuestions

    init {
        // Load levels on initialization
        loadAllLevels()
    }

    fun loadAllLevels() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Starting to load all levels...")
                val levels = repository.getAllLevels()
                Log.d(TAG, "Repository returned ${levels.size} levels")

                if (levels.isNotEmpty()) {
                    _availableLevels.value = levels
                    Log.d(TAG, "Successfully loaded ${levels.size} levels from database")

                    // Update statistics
                    _totalQuestions.value = levels.sumOf { it.questionCount }
                    _countryCount.value = levels.flatMap { it.locations }.distinctBy { it.locationName }.size

                    Log.d(TAG, "Updated statistics: ${_totalQuestions.value} questions, ${_countryCount.value} countries")

                    // Log each level for debugging
                    levels.forEach { level ->
                        Log.d(TAG, "Level: ${level.levelId} - ${level.title} (${level.difficulty}) with ${level.locations.size} locations")
                    }
                } else {
                    Log.w(TAG, "No levels found in database, using fallback sample data")
                    loadSampleData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading levels", e)
                _errorMessage.value = "Failed to load levels: ${e.message}"
                loadSampleData()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadSampleData() {
        Log.d(TAG, "Loading sample data as fallback...")
        val sampleLevel = SampleLocationData.createSampleLevel()
        _availableLevels.value = listOf(sampleLevel)

        // Update statistics with sample data
        _totalQuestions.value = sampleLevel.questionCount
        _countryCount.value = sampleLevel.locations.distinctBy { it.country }.size

        Log.d(TAG, "Sample data loaded: ${_totalQuestions.value} questions, ${_countryCount.value} countries")
    }

    fun loadLevelData(levelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                Log.d(TAG, "Loading level data for levelId: $levelId")
                val levelData = repository.getSceneLevel(levelId)

                if (levelData != null) {
                    _currentLevel.value = levelData
                    _currentQuestions.value = levelData.locations
                    Log.d(TAG, "Successfully loaded level: ${levelData.title} with ${levelData.locations.size} locations")

                    // Log locations for debugging
                    levelData.locations.forEach { location ->
                        Log.d(TAG, "Location: ${location.locationName} at (${location.latitude}, ${location.longitude})")
                    }
                } else {
                    Log.w(TAG, "Level $levelId not found, using sample data")
                    val sampleLevel = SampleLocationData.createSampleLevel()
                    _currentLevel.value = sampleLevel
                    _currentQuestions.value = sampleLevel.locations
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading level data for $levelId", e)
                _errorMessage.value = "Failed to load level: ${e.message}"

                // Fallback to sample data
                val sampleLevel = SampleLocationData.createSampleLevel()
                _currentLevel.value = sampleLevel
                _currentQuestions.value = sampleLevel.locations
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStatistics() {
        Log.d(TAG, "Loading statistics...")
        if (_availableLevels.value.isEmpty()) {
            loadAllLevels() // This will update statistics as well
        } else {
            // Update statistics from existing data
            val levels = _availableLevels.value
            _totalQuestions.value = levels.sumOf { it.questionCount }
            _countryCount.value = levels.flatMap { it.locations }.distinctBy { it.locationName }.size
            Log.d(TAG, "Statistics updated from existing data: ${_totalQuestions.value} questions, ${_countryCount.value} countries")
        }
    }

    suspend fun submitGuess(imageId: String, guessLat: Double, guessLon: Double): Double? {
        return try {
            Log.d(TAG, "Submitting guess for image: $imageId")
            repository.submitGuess(imageId, guessLat, guessLon)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting guess", e)
            null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Debug function to test data loading
    fun testDataLoading() {
        viewModelScope.launch {
            Log.d(TAG, "=== Testing Data Loading ===")
            try {
                val levels = repository.getAllLevels()
                Log.d(TAG, "Test result: ${levels.size} levels loaded")
                levels.forEach { level ->
                    Log.d(TAG, "Test level: ${level.levelId} - ${level.title}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Test failed", e)
            }
        }
    }

    fun notifyWidgetUpdate(context: Context) {
        WidgetUpdateManager.updateAllWidgets(context)
    }
}