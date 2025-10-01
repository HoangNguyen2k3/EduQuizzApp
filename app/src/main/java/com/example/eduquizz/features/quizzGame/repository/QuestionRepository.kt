package com.example.eduquizz.data.repository

import com.example.eduquizz.data.models.DataOrException
import com.example.eduquizz.features.quizzGame.model.QuestionItem
import com.example.eduquizz.features.quizzGame.model.QuizGameLevelResponse
import com.example.eduquizz.features.quizzGame.network.QuizGameApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuestionRepository @Inject constructor(
    private val api: QuizGameApi
) {

    suspend fun getQuizGameLevel(levelId: String): DataOrException<ArrayList<QuestionItem>, Boolean, Exception> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getQuizGameLevel(levelId)
                if (response.isSuccessful) {
                    val levelData = response.body()
                    if (levelData != null) {
                        // Convert từ BackendQuizQuestion sang QuestionItem
                        val questions = ArrayList(levelData.questions.map { backendQuestion ->
                            QuestionItem(
                                questionText = backendQuestion.question,
                                answer = backendQuestion.answer,
                                category = backendQuestion.category,
                                choices = backendQuestion.choices
                            )
                        })
                        DataOrException(data = questions, loading = false)
                    } else {
                        DataOrException(data = null, loading = false, e = Exception("No data found"))
                    }
                } else {
                    DataOrException(
                        data = null,
                        loading = false,
                        e = Exception("API Error: ${response.code()} - ${response.message()}")
                    )
                }
            } catch (exception: Exception) {
                DataOrException(data = null, loading = false, e = exception)
            }
        }
    }

    suspend fun getAllQuestionQuizGame(path: String): DataOrException<ArrayList<QuestionItem>, Boolean, Exception> {
        // Map path thành levelId cho backend
        val levelId = when {
            path.contains("Easy", ignoreCase = true) -> "LevelEasy"
            path.contains("Normal", ignoreCase = true) -> "LevelNormal"
            path.contains("Hard", ignoreCase = true) -> "LevelHard"
            else -> "LevelEasy" // default
        }

        return getQuizGameLevel(levelId)
    }

    suspend fun getAllAvailableLevels(): DataOrException<List<QuizGameLevelResponse>, Boolean, Exception> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllLevels()
                if (response.isSuccessful) {
                    val levels = response.body()
                    DataOrException(data = levels, loading = false)
                } else {
                    DataOrException(
                        data = null,
                        loading = false,
                        e = Exception("API Error: ${response.code()}")
                    )
                }
            } catch (exception: Exception) {
                DataOrException(data = null, loading = false, e = exception)
            }
        }
    }
}
