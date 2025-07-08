package com.example.eduquizz.features.wordsearch.repository

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class WordSearchData(
    val difficulty: String = "",
    val gridSize: Int = 8,
    val title: String = "",
    val wordCount: Int = 0,
    val words: List<String> = emptyList()
)

@Singleton
class WordSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val database = FirebaseDatabase.getInstance().getReference("English/WordSearch")

    suspend fun getWordsByTopic(topicId: String): Result<WordSearchData> {
        return try {
            val snapshot = database.child(topicId).get().await()
            if (snapshot.exists()) {
                val data = snapshot.getValue(WordSearchData::class.java)
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Failed to parse data"))
                }
            } else {
                Result.failure(Exception("Topic not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveTopicCompletion(userName: String, topicId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("user_${userName}_topic_${topicId}_completed", isCompleted)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTopicCompletion(userName: String, topicId: String): Result<Boolean> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            val isCompleted = sharedPreferences.getBoolean("user_${userName}_topic_${topicId}_completed", false)
            Result.success(isCompleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTopicCompletions(userName: String): Result<Map<String, Boolean>> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            val completions = mutableMapOf<String, Boolean>()
            val topics = listOf("Travel", "FunAndGames", "StudyWork")
            topics.forEach { topicId ->
                completions[topicId] = sharedPreferences.getBoolean("user_${userName}_topic_${topicId}_completed", false)
            }
            Result.success(completions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class TopicData(
    val id: String,
    val title: String,
    val difficulty: String,
    val wordCount: Int
)