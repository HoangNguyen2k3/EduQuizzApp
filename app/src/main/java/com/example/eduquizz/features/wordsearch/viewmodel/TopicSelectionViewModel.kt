package com.example.eduquizz.features.wordsearch.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.wordsearch.repository.WordSearchRepository
import com.example.eduquizz.features.wordsearch.screens.Topic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.async
import javax.inject.Inject

@HiltViewModel
class TopicSelectionViewModel @Inject constructor(
    private val repository: WordSearchRepository
) : ViewModel() {

    private val _topics = mutableStateOf<List<Topic>>(emptyList())
    val topics: State<List<Topic>> get() = _topics

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> get() = _error

    fun loadTopics(userName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {

                val completionsDeferred = async { repository.getAllTopicCompletions(userName) }
                val topicsDeferred = async { repository.getAllTopics() }

                val completionsResult = completionsDeferred.await()
                val topicsResult = topicsDeferred.await()

                topicsResult.onSuccess { topicsFromDb ->
                    completionsResult.onSuccess { completions ->
                        _topics.value = topicsFromDb.map { topicData ->
                            Topic(
                                id = topicData.topicId,
                                title = topicData.title,
                                icon = getIconForTopic(topicData.topicId),
                                wordCount = topicData.wordCount,
                                difficulty = topicData.difficulty,
                                isCompleted = completions[topicData.topicId] ?: false,
                                backgroundColor = getColorForTopic(topicData.topicId)
                            )
                        }
                    }.onFailure { exception ->
                        // If completions fail, still show topics but without completion status
                        _topics.value = topicsFromDb.map { topicData ->
                            Topic(
                                id = topicData.topicId,
                                title = topicData.title,
                                icon = getIconForTopic(topicData.topicId),
                                wordCount = topicData.wordCount,
                                difficulty = topicData.difficulty,
                                isCompleted = false,
                                backgroundColor = getColorForTopic(topicData.topicId)
                            )
                        }
                        _error.value = "Failed to load completion status: ${exception.message}"
                    }
                }.onFailure { exception ->
                    _error.value = "Failed to load topics: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getIconForTopic(topicId: String): ImageVector {
        return when (topicId.lowercase()) {
            "travel" -> Icons.Default.Flight
            "funandgames", "fun", "games" -> Icons.Default.SportsEsports
            "studywork", "work", "study" -> Icons.Default.Work
            "school", "education" -> Icons.Default.School
            "technology", "tech", "computer" -> Icons.Default.Computer
            else -> Icons.Default.Category
        }
    }

    private fun getColorForTopic(topicId: String): Color {
        return when (topicId.lowercase()) {
            "travel" -> Color(0xFF4FC3F7) // Light Blue
            "funandgames", "fun", "games" -> Color(0xFF66BB6A) // Light Green
            "studywork", "work", "study" -> Color(0xFFFF7043) // Deep Orange
            "school", "education" -> Color(0xFF9C27B0) // Purple
            "technology", "tech", "computer" -> Color(0xFF2196F3) // Blue
            "sports" -> Color(0xFF4CAF50) // Green
            "food" -> Color(0xFFFF9800) // Orange
            "animals" -> Color(0xFF795548) // Brown
            "nature" -> Color(0xFF8BC34A) // Light Green
            "music" -> Color(0xFFE91E63) // Pink
            else -> Color(0xFF607D8B) // Blue Grey
        }
    }
}