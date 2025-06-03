package com.example.eduquizz.repository

import com.example.eduquizz.model.DataOrException
import com.example.eduquizz.model.QuestionItem
import com.example.eduquizz.network.QuestionApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
class QuestionRepository @Inject constructor(private val api:QuestionApi) {
    private val dataOrException= DataOrException<ArrayList<QuestionItem>,Boolean,Exception>()
    suspend fun getAllQuestion():
    DataOrException<ArrayList<QuestionItem>,Boolean,Exception>{
        try{
            dataOrException.loading = true
            dataOrException.data = api.getAllQuestions()
            var limit : Int = 1000
            dataOrException.data = dataOrException.data?.shuffled()?.take(limit)?.let { ArrayList(it) }
            if(dataOrException.data.toString().isEmpty()){
                dataOrException.loading=false
            }
            if (!dataOrException.data.isNullOrEmpty()) {
                uploadQuestionsToFirebaseRealtime(dataOrException.data!!)
            }

        }catch (exception:Exception){
            dataOrException.e = exception
        }
       return dataOrException
    }
    fun uploadQuestionsToFirebaseRealtime(questions: List<QuestionItem>) {
        val db = FirebaseDatabase.getInstance()
        val ref = db.getReference("Quiz")

        val filteredQuestions = questions.filter { isValidQuestion(it) }

        for ((index, question) in filteredQuestions.withIndex()) {
            ref.child("question_${index + 1}").setValue(question)
                .addOnSuccessListener {
                    println("✅ Uploaded question_${index + 1}")
                }
                .addOnFailureListener { e ->
                    println("❌ Failed to upload: $e")
                }
        }
    }

    fun isValidQuestion(q: QuestionItem): Boolean {
        return q.choices.size == 4 && q.answer.isNotBlank() && q.question.isNotBlank()
    }

}*/
class QuestionRepository @Inject constructor() {

    private var cachedQuestions: ArrayList<QuestionItem>? = null

    suspend fun getAllQuestion(): DataOrException<ArrayList<QuestionItem>, Boolean, Exception> {
        val dataOrException = DataOrException<ArrayList<QuestionItem>, Boolean, Exception>()

        try {
            // Nếu đã cache thì trả về luôn, không gọi Firebase nữa
            cachedQuestions?.let {
                dataOrException.data = it
                dataOrException.loading = false
                return dataOrException
            }

            dataOrException.loading = true

            val ref = FirebaseDatabase.getInstance().getReference("Quiz")
            val questionList = suspendCoroutine<ArrayList<QuestionItem>> { cont ->
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = arrayListOf<QuestionItem>()
                        for (child in snapshot.children) {
                            val question = child.getValue(QuestionItem::class.java)
                            question?.let { list.add(it) }
                        }
                        cont.resume(list)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cont.resumeWithException(error.toException())
                    }
                })
            }
            val limitedList = questionList.shuffled().take(150)

// Cache 150 câu này
            cachedQuestions = ArrayList(limitedList)
            // Cache toàn bộ câu hỏi lấy về

            dataOrException.data = questionList
            dataOrException.loading = false

        } catch (exception: Exception) {
            dataOrException.e = exception
            dataOrException.loading = false
        }

        return dataOrException
    }

    // Hàm lấy random n câu hỏi từ cache
    fun getRandomQuestions(count: Int): ArrayList<QuestionItem> {
        val cached = cachedQuestions ?: return arrayListOf()
        return cached.shuffled().take(count).let { ArrayList(it) }
    }
}


