//package com.example.eduquizz.data.repository
//
//import com.example.eduquizz.features.quizzGame.model.QuestionItem
//import com.example.eduquizz.data.models.DataOrException
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import javax.inject.Inject
//import kotlin.coroutines.resume
//import kotlin.coroutines.resumeWithException
//import kotlinx.coroutines.suspendCancellableCoroutine
///*
//class QuestionRepository @Inject constructor(private val api:QuestionApi) {
//    private val dataOrException= DataOrException<ArrayList<QuestionItem>,Boolean,Exception>()
//    suspend fun getAllQuestion():
//    DataOrException<ArrayList<QuestionItem>,Boolean,Exception>{
//        try{
//            dataOrException.loading = true
//            dataOrException.data = api.getAllQuestions()
//            var limit : Int = 1000
//            dataOrException.data = dataOrException.data?.shuffled()?.take(limit)?.let { ArrayList(it) }
//            if(dataOrException.data.toString().isEmpty()){
//                dataOrException.loading=false
//            }
//            if (!dataOrException.data.isNullOrEmpty()) {
//                uploadQuestionsToFirebaseRealtime(dataOrException.data!!)
//            }
//
//        }catch (exception:Exception){
//            dataOrException.e = exception
//        }
//       return dataOrException
//    }
//    fun uploadQuestionsToFirebaseRealtime(questions: List<QuestionItem>) {
//        val db = FirebaseDatabase.getInstance()
//        val ref = db.getReference("Quiz")
//
//        val filteredQuestions = questions.filter { isValidQuestion(it) }
//
//        for ((index, question) in filteredQuestions.withIndex()) {
//            ref.child("question_${index + 1}").setValue(question)
//                .addOnSuccessListener {
//                    println("✅ Uploaded question_${index + 1}")
//                }
//                .addOnFailureListener { e ->
//                    println("❌ Failed to upload: $e")
//                }
//        }
//    }
//
//    fun isValidQuestion(q: QuestionItem): Boolean {
//        return q.choices.size == 4 && q.answer.isNotBlank() && q.question.isNotBlank()
//    }
//
//}*/
//class QuestionRepository @Inject constructor() {
//
//    private var cachedQuestions: ArrayList<QuestionItem>? = null
//
//    suspend fun getAllQuestionQuizGame(path:String)
//            : DataOrException<ArrayList<QuestionItem>, Boolean, Exception> {
//
//        val result = DataOrException<ArrayList<QuestionItem>, Boolean, Exception>()
//        result.loading = true
//
//        return try {
//            val ref = FirebaseDatabase.getInstance().getReference(path)
//
//            val questions: ArrayList<QuestionItem> = suspendCancellableCoroutine { cont ->
//                ref.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                       // if (!cont.isActive) return
//                        val list = arrayListOf<QuestionItem>()
//                        for (child in snapshot.children) {
//                            val questionText = child.child("question").getValue(String::class.java) ?: ""
//                            val answer      = child.child("answer").getValue(String::class.java) ?: ""
//                            val category    = child.child("category").getValue(String::class.java) ?: ""
//                            val imageUrl    = child.child("image").getValue(String::class.java)       // 🔑 key đúng
//                            val choices     = child.child("choices").children
//                                .mapNotNull { it.getValue(String::class.java) }
//                            list += QuestionItem(
//                                question = questionText,
//                                answer = answer,
//                                category = category,
//                                image = imageUrl,
//                                choices = choices,
//                                questionText = questionText
//                            )
//                        }
//                        if (cont.isActive) cont.resume(list)
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        if (cont.isActive) cont.resumeWithException(error.toException())
//                    }
//                })
//            }
//
//
//            //val limited = ArrayList(questions.take(150))
//            val limited = ArrayList(questions)
//
//          //  cachedQuestions = limited
//
//            result.apply {
//                data = limited
//                loading = false
//            }
//        } catch (e: Exception) {
//            result.apply {
//                this.e = e
//                loading = false
//            }
//        }
//    }
//
//
///*    // Hàm lấy random n câu hỏi từ cache
//    fun getRandomQuestions(count: Int): ArrayList<QuestionItem> {
//        val cached = cachedQuestions ?: return arrayListOf()
//        return cached.shuffled().take(count).let { ArrayList(it) }
//    }*/
//}
//
//
