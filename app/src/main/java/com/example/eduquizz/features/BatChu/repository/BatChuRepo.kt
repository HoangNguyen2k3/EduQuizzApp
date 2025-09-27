package com.example.eduquizz.features.BatChu.repository

import android.util.Log
import com.example.eduquizz.features.BatChu.model.DataBatChu
import com.example.eduquizz.features.wordsearch.repository.WordSearchData
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class BatChuRepo

@Singleton
class BatChuRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance().getReference("English/BatChu")

    suspend fun getQuestionsByLevel(level: String): Result<List<DataBatChu>> {
        return try {
            val snapshot = database.child(level).get().await()
            if (snapshot.exists()) {
                val questions = snapshot.children.mapNotNull { snap ->
                    Log.d("BatChuRepo", "Raw snapshot: ${snap.value}")
                    val data = snap.getValue(DataBatChu::class.java)
                    Log.d("BatChuRepo", "Parsed data: $data")

                    data?.let {
                        try {
                            val answerChars = it.answer.uppercase().toList()
                            val alphabet = ('A'..'Z').toList()
                            val extraLetters = (alphabet - answerChars).shuffled().take(8)
                            val shuffled = (answerChars + extraLetters).shuffled()

                            it.copy(
                                question = "What is this?",
                                shuffledLetters = shuffled
                            )
                        } catch (e: Exception) {
                            Log.e("BatChuRepo", "Error while processing question: ${e.message}", e)
                            null // return null if error
                        }
                    }
                }
                Result.success(questions)
            } else {
                Result.failure(Exception("No questions found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}