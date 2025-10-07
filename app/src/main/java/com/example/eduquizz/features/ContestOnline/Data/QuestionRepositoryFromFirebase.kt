package com.example.eduquizz.features.ContestOnline.Data

import com.example.eduquizz.features.ContestOnline.Model.QuestionItemContest
import com.google.firebase.database.*
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QuestionRepositoryFromFirebase @Inject constructor() {

    suspend fun getQuestionsFromFirebase(path: String): List<QuestionItemContest> {
        val ref = FirebaseDatabase.getInstance().getReference(path)

        return suspendCancellableCoroutine { cont ->
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = arrayListOf<QuestionItemContest>()
                    for (child in snapshot.children) {
                        val question = child.child("question").getValue(String::class.java) ?: ""
                        val answer = child.child("answer").getValue(String::class.java) ?: ""
                        val category = child.child("category").getValue(String::class.java) ?: ""
                        val image = child.child("image").getValue(String::class.java)
                        val choices = child.child("choices").children.mapNotNull {
                            it.getValue(String::class.java)
                        }
                        if (question.isNotBlank() && answer.isNotBlank()) {
                            list += QuestionItemContest(
                                question = question,
                                answer = answer,
                                category = category,
                                image = image,
                                choices = choices
                            )
                        }
                    }

                    // Lấy ngẫu nhiên 20 câu (nếu có đủ)
                    val limited = if (list.size > 20) list.shuffled().take(20) else list
                    cont.resume(limited)
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(error.toException())
                }
            })
        }
    }
}
