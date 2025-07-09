package com.example.eduquizz.features.bubbleshot.repository

import com.example.eduquizz.features.bubbleshot.model.MathQuestion
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ShotQuestionRepository {
    private val database = FirebaseDatabase.getInstance()
    private val questionsRef = database.getReference("Math/BubbleShot")

    fun getQuestions(): Flow<List<MathQuestion>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val questions = mutableListOf<MathQuestion>()
                for (childSnapshot in snapshot.children) {
                    val question = childSnapshot.child("question").getValue(String::class.java) ?: ""
                    val answer = childSnapshot.child("answer").getValue(String::class.java) ?: ""
                    questions.add(MathQuestion(question, answer))
                }
                trySend(questions)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        questionsRef.addValueEventListener(listener)

        awaitClose {
            questionsRef.removeEventListener(listener)
        }
    }

    suspend fun getQuestionsOnce(): List<MathQuestion> {
        return try {
            val snapshot = questionsRef.get().await()
            val questions = mutableListOf<MathQuestion>()
            for (childSnapshot in snapshot.children) {
                val question = childSnapshot.child("question").getValue(String::class.java) ?: ""
                val answer = childSnapshot.child("answer").getValue(String::class.java) ?: ""
                questions.add(MathQuestion(question, answer))
            }
            questions
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addQuestion(question: String, answer: String): Boolean {
        return try {
            val newQuestionRef = questionsRef.push()
            val questionData = mapOf(
                "question" to question,
                "answer" to answer
            )
            newQuestionRef.setValue(questionData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateQuestion(questionId: String, question: String, answer: String): Boolean {
        return try {
            val questionData = mapOf(
                "question" to question,
                "answer" to answer
            )
            questionsRef.child(questionId).setValue(questionData).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteQuestion(questionId: String): Boolean {
        return try {
            questionsRef.child(questionId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}