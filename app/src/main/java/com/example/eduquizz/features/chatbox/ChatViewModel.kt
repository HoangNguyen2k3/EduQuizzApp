package com.example.eduquizz.features.chatbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.0-flash-lite",
        apiKey = "AIzaSyDUg10h7dIec2Fvj99FfKvtmzIQ0SLjcHs"
    )

    private val chat = generativeModel.startChat(history = listOf())

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        Log.d("GeminiChat", "Người dùng gửi tin nhắn: $userMessage")

        // Thêm tin nhắn người dùng vào danh sách
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(userMessage, isUser = true))
        _messages.value = currentMessages

        _uiState.value = ChatUiState.Loading

        viewModelScope.launch {
            try {
                Log.d("GeminiChat", "Đang gửi tin nhắn đến Gemini API...")

                val response = chat.sendMessage(userMessage)
                val botMessage = response.text ?: "Xin lỗi, tôi không hiểu câu hỏi của bạn."

                Log.d("GeminiChat", "Phản hồi từ Gemini: $botMessage")

                val updatedMessages = _messages.value.toMutableList()
                updatedMessages.add(ChatMessage(botMessage, isUser = false))
                _messages.value = updatedMessages

                _uiState.value = ChatUiState.Success(_messages.value)

                Log.d("GeminiChat", "Tin nhắn AI đã được thêm vào danh sách hiển thị.")
            } catch (e: Exception) {
                Log.e("GeminiChat", "Lỗi khi gửi tin nhắn: ${e.message}", e)

                _uiState.value = ChatUiState.Error(e.message ?: "Đã xảy ra lỗi")
                val errorMessages = _messages.value.toMutableList()
                errorMessages.add(
                    ChatMessage(
                        "Xin lỗi, đã có lỗi xảy ra: ${e.message}",
                        isUser = false
                    )
                )
                _messages.value = errorMessages
            }
        }
    }

    fun clearChat() {
        Log.d("GeminiChat", "Đang xóa lịch sử chat...")
        _messages.value = emptyList()
        _uiState.value = ChatUiState.Idle
    }
}
