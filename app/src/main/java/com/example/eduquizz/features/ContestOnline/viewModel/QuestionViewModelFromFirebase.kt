package com.example.eduquizz.features.contest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.ContestOnline.Data.QuestionRepositoryFromFirebase
import com.example.eduquizz.features.ContestOnline.Model.QuestionItemContest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionUiState(
    val loading: Boolean = true,
    val questions: List<QuestionItemContest> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class QuestionViewModelFromFirebase @Inject constructor(
    private val repository: QuestionRepositoryFromFirebase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState

    fun loadQuestions(path: String) {
        viewModelScope.launch {
            _uiState.value = QuestionUiState(loading = true)
            try {
                val questions = repository.getQuestionsFromFirebase(path)
                _uiState.value = QuestionUiState(loading = false, questions = questions)
            } catch (e: Exception) {
                _uiState.value = QuestionUiState(
                    loading = false,
                    error = e.message ?: "Lỗi không xác định"
                )
            }
        }
    }
}
