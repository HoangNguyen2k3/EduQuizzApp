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
import androidx.compose.ui.graphics.Color
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

    init {
        loadTopics()
    }

    private fun loadTopics() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val completionsResult = repository.getAllTopicCompletions()
                completionsResult.onSuccess { completions ->
                    val topicIds = listOf("Travel", "FunAndGames", "StudyWork")
                    _topics.value = topicIds.map { id ->
                        Topic(
                            id = id,
                            title = id.replaceFirstChar { it.uppercase() },
                            icon = when (id) {
                                "Travel" -> Icons.Default.Flight
                                "FunAndGames" -> Icons.Default.SportsEsports
                                "StudyWork" -> Icons.Default.Work
                                else -> Icons.Default.Flight
                            },
                            wordCount = 25, // Giả định, có thể lấy từ repository
                            difficulty = "Easy",
                            isCompleted = completions[id] ?: false,
                            backgroundColor = when (id) {
                                "Travel" -> Color(0xFF4FC3F7)
                                "FunAndGames" -> Color(0xFF66BB6A)
                                "StudyWork" -> Color(0xFFFF7043)
                                else -> Color.Gray
                            }
                        )
                    }
                }.onFailure { exception ->
                    _error.value = "Failed to load topic completions: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}