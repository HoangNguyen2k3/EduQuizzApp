package com.example.eduquizz.features.wordsearch.repository

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
class WordSearchRepository @Inject constructor(@ApplicationContext private val context: Context) {
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

    //Lưu state khi hoàn thành 1 chủ để
    suspend fun saveTopicCompletion(topicId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("topic_${topicId}_completed", isCompleted)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // lấy state hoàn thành của topic
    suspend fun getTopicCompletion(topicId: String): Result<Boolean> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            val isCompleted = sharedPreferences.getBoolean("topic_${topicId}_completed", false)
            Result.success(isCompleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllTopicCompletions(): Result<Map<String, Boolean>> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
            val completions = mutableMapOf<String, Boolean>()

            // Danh sách topics cố định
            val topics = listOf("Travel", "FunAndGames", "StudyWork")
            topics.forEach { topicId ->
                completions[topicId] = sharedPreferences.getBoolean("topic_${topicId}_completed", false)
            }

            Result.success(completions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

//    suspend fun getAllTopics(): Result<List<TopicData>> {
//        return try {
//            val snapshot = firestore
//                .collection("WordSearch")
//                .get()
//                .await()
//
//            val topics = snapshot.documents.mapNotNull { doc ->
//                try {
//                    val data = doc.toObject(WordSearchData::class.java)
//                    data?.let {
//                        TopicData(
//                            id = doc.id,
//                            title = it.title,
//                            difficulty = it.difficulty,
//                            wordCount = it.wordCount
//                        )
//                    }
//                } catch (e: Exception) {
//                    null
//                }
//            }
//
//            Result.success(topics)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
}

data class TopicData(
    val id: String,
    val title: String,
    val difficulty: String,
    val wordCount: Int
)