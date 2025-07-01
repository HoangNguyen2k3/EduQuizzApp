package com.example.eduquizz.features.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoadingState(
    val isLoading: Boolean = false,
    val loadingText: String = "Đang tải...",
    val progress: Float = 0f,
    val showProgress: Boolean = false,
    val error: String? = null
)

class LoadingViewModel : ViewModel() {
    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    fun showLoading(
        text: String = "Đang tải...",
        showProgress: Boolean = false
    ) {
        _loadingState.value = LoadingState(
            isLoading = true,
            loadingText = text,
            showProgress = showProgress
        )
    }

    fun updateProgress(progress: Float, text: String? = null) {
        _loadingState.value = _loadingState.value.copy(
            progress = progress,
            loadingText = text ?: _loadingState.value.loadingText
        )
    }

    fun hideLoading() {
        _loadingState.value = LoadingState(isLoading = false)
    }

    fun showError(error: String) {
        _loadingState.value = LoadingState(
            isLoading = false,
            error = error
        )
    }

    // Simulate loading with progress
    fun simulateLoadingWithProgress() {
        viewModelScope.launch {
            showLoading("Đang khởi tạo...", showProgress = true)

            for (i in 1..100) {
                delay(50)
                val progress = i / 100f
                val text = when {
                    i < 30 -> "Đang tải cấu hình..."
                    i < 60 -> "Đang tải dữ liệu..."
                    i < 90 -> "Đang xử lý..."
                    else -> "Hoàn thành..."
                }
                updateProgress(progress, text)
            }

            delay(500)
            hideLoading()
        }
    }
}