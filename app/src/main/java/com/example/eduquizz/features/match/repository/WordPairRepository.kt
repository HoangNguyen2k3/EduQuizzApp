package com.example.eduquizz.features.match.repository

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.example.eduquizz.features.match.model.WordPair
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class WordMatchData(
    val difficulty: String = "",
    val levelCount: Int = 4,
    val title: String = "",
    val pairCount: Int = 0,
    val wordPairs: List<WordPair> = emptyList()
)

data class LevelData(
    val id: String,
    val title: String,
    val difficulty: String,
    val pairCount: Int,
    val isCompleted: Boolean = false
)

@Singleton
class WordPairRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) {
    private val database = FirebaseDatabase.getInstance().getReference("English/MatchGame")

    suspend fun getWordPairsByTopic(topicId: String): Result<WordMatchData> {
        return try {
            val snapshot = database.child(topicId).get().await()
            if (snapshot.exists()) {
                val wordPairs = parseWordPairsFromSnapshot(snapshot)
                val data = WordMatchData(
                    difficulty = "Medium",
                    levelCount = 4,
                    title = topicId,
                    pairCount = wordPairs.size,
                    wordPairs = wordPairs
                )
                Result.success(data)
            } else {
                Result.failure(Exception("Topic not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllWordPairs(): List<WordPair> {
        return try {
            // Lấy từ Firebase Realtime Database
            val snapshot = database.get().await()
            if (snapshot.exists()) {
                parseWordPairsFromSnapshot(snapshot)
            } else {
                getWordPairsFromFirestore()
            }
        } catch (e: Exception) {
            try {
                getWordPairsFromFirestore()
            } catch (firestoreException: Exception) {
                getDefaultWordPairs()
            }
        }
    }

    // Hàm mới để parse dữ liệu từ Firebase Realtime Database
    private fun parseWordPairsFromSnapshot(snapshot: DataSnapshot): List<WordPair> {
        val wordPairs = mutableListOf<WordPair>()

        // Duyệt qua tất cả các child (wordPair_1, wordPair_2, ...)
        snapshot.children.forEach { childSnapshot ->
            val key = childSnapshot.key
            if (key != null && key.startsWith("wordPair_")) {
                try {
                    val word = childSnapshot.child("word").getValue(String::class.java)
                    val definition = childSnapshot.child("definition").getValue(String::class.java)

                    if (word != null && definition != null) {
                        wordPairs.add(WordPair(word = word, definition = definition))
                    }
                } catch (e: Exception) {
                    // Bỏ qua nếu có lỗi với item này
                    println("Error parsing word pair ${key}: ${e.message}")
                }
            }
        }

        return wordPairs.ifEmpty { getDefaultWordPairs() }
    }

    // Hàm lấy dữ liệu từ Firestore (backup)
    private suspend fun getWordPairsFromFirestore(): List<WordPair> {
        return try {
            val englishDoc = firestore.collection("English")
                .document("MatchGame")
                .get()
                .await()

            val wordPairs = mutableListOf<WordPair>()
            val data = englishDoc.data

            data?.forEach { (key, value) ->
                if (key.startsWith("wordPair_")) {
                    val pairData = value as? Map<String, Any>
                    val word = pairData?.get("word") as? String
                    val definition = pairData?.get("definition") as? String

                    if (word != null && definition != null) {
                        wordPairs.add(WordPair(word = word, definition = definition))
                    }
                }
            }

            wordPairs.ifEmpty { getDefaultWordPairs() }
        } catch (e: Exception) {
            getDefaultWordPairs()
        }
    }

    suspend fun getWordPairsByLevel(level: Int): List<WordPair> {
        return try {
            val allPairs = getAllWordPairs()
            val start = level * 5
            val end = minOf((level + 1) * 5, allPairs.size)

            if (start < allPairs.size) {
                allPairs.subList(start, end)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Fallback: lấy 5 cặp từ dữ liệu mặc định
            getDefaultWordPairs().drop(level * 5).take(5)
        }
    }

    suspend fun saveLevelCompletion(userName: String, levelId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("user_${userName}_level_${levelId}_completed", isCompleted)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLevelCompletion(userName: String, levelId: String): Result<Boolean> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
            val isCompleted = sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
            Result.success(isCompleted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLevelCompletions(userName: String): Result<Map<String, Boolean>> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
            val completions = mutableMapOf<String, Boolean>()
            val levels = listOf("level_1", "level_2", "level_3", "level_4")
            levels.forEach { levelId ->
                completions[levelId] = sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
            }
            Result.success(completions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUserProgress(userName: String, level: Int, totalRight: Int, totalQuestions: Int): Result<Unit> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putInt("user_${userName}_current_level", level)
                .putInt("user_${userName}_total_right", totalRight)
                .putInt("user_${userName}_total_questions", totalQuestions)
                .apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProgress(userName: String): Result<Triple<Int, Int, Int>> {
        return try {
            val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
            val currentLevel = sharedPreferences.getInt("user_${userName}_current_level", 0)
            val totalRight = sharedPreferences.getInt("user_${userName}_total_right", 0)
            val totalQuestions = sharedPreferences.getInt("user_${userName}_total_questions", 0)
            Result.success(Triple(currentLevel, totalRight, totalQuestions))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableLevels(): Result<List<LevelData>> {
        return try {
            val snapshot = database.child("levels").get().await()
            if (snapshot.exists()) {
                val levels = mutableListOf<LevelData>()
                snapshot.children.forEach { levelSnapshot ->
                    val levelData = levelSnapshot.getValue(LevelData::class.java)
                    if (levelData != null) {
                        levels.add(levelData)
                    }
                }
                Result.success(levels)
            } else {
                // Trả về levels mặc định
                Result.success(getDefaultLevels())
            }
        } catch (e: Exception) {
            Result.success(getDefaultLevels())
        }
    }

    private fun getDefaultLevels(): List<LevelData> {
        return listOf(
            LevelData("level_1", "Beginner", "Easy", 5),
            LevelData("level_2", "Intermediate", "Medium", 5),
            LevelData("level_3", "Advanced", "Hard", 5),
            LevelData("level_4", "Expert", "Expert", 5)
        )
    }

    private fun getDefaultWordPairs(): List<WordPair> {
        return listOf(
            WordPair("Apple", "a round fruit with firm, white flesh and a green, red, or yellow skin"),
            WordPair("Dog", "A domestic animal"),
            WordPair("Sun", "The star at the center of our solar system"),
            WordPair("Book", "A collection of pages"),
            WordPair("Computer", "An electronic device for processing data"),
            WordPair("Flower", "A plant's reproductive organ"),
            WordPair("Tiger", "A big cat"),
            WordPair("River", "A natural water flow"),
            WordPair("Mountain", "A high landform"),
            WordPair("Car", "A road vehicle"),
            WordPair("Banana", "A yellow fruit"),
            WordPair("Cat", "A domestic feline"),
            WordPair("Moon", "Earth's natural satellite"),
            WordPair("Notebook", "A collection of blank pages"),
            WordPair("Phone", "A device for calling"),
            WordPair("Rose", "A type of flower"),
            WordPair("Lion", "The king of the jungle"),
            WordPair("Lake", "A body of water surrounded by land"),
            WordPair("Hill", "A small elevation of land"),
            WordPair("Bus", "A large passenger vehicle")
        )
    }
}