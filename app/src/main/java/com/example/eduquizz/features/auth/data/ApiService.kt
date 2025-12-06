package com.example.eduquizz.features.auth.data

import com.example.eduquizz.features.admin.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Existing auth endpoints...
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("api/auth/user/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<UserResponse>

    // Admin endpoints
    @GET("api/auth/check-admin/{username}")
    suspend fun checkAdminStatus(@Path("username") username: String): Response<AdminCheckResponse>

    @GET("api/admin/dashboard/{username}")
    suspend fun getAdminDashboard(
        @Path("username") username: String
    ): Response<AdminResponse<AdminDashboardStats>>

    @GET("api/admin/wordsearch/{username}")
    suspend fun getWordSearchLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    @GET("api/admin/batchu/{username}")
    suspend fun getBatChuLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    @GET("api/admin/matchgame/{username}")
    suspend fun getMatchGameLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    @GET("api/admin/quiz/{username}")
    suspend fun getQuizLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    @GET("api/admin/scene/{username}")
    suspend fun getSceneLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    @GET("api/admin/sound/{username}")
    suspend fun getSoundLevels(
        @Path("username") username: String
    ): Response<AdminResponse<List<GameLevel>>>

    // // Question Management Endpoints
    // @GET("api/admin/questions/{username}")
    // suspend fun getQuestions(
    //     @Path("username") username: String,
    //     @Body filter: QuestionFilter
    // ): Response<AdminResponse<List<QuestionItem>>>

    // @GET("api/admin/questions/{username}/{questionId}")
    // suspend fun getQuestionById(
    //     @Path("username") username: String,
    //     @Path("questionId") questionId: String
    // ): Response<AdminResponse<QuestionItem>>

    // @POST("api/admin/questions/{username}")
    // suspend fun createQuestion(
    //     @Path("username") username: String,
    //     @Body request: QuestionCreateRequest
    // ): Response<AdminResponse<QuestionItem>>

    // @PUT("api/admin/questions/{username}")
    // suspend fun updateQuestion(
    //     @Path("username") username: String,
    //     @Body request: QuestionUpdateRequest
    // ): Response<AdminResponse<QuestionItem>>

    // @DELETE("api/admin/questions/{username}/{questionId}")
    // suspend fun deleteQuestion(
    //     @Path("username") username: String,
    //     @Path("questionId") questionId: String
    // ): Response<AdminResponse<Boolean>>

    // @POST("api/admin/questions/{username}/bulk")
    // suspend fun bulkImportQuestions(
    //     @Path("username") username: String,
    //     @Body questions: List<QuestionCreateRequest>
    // ): Response<AdminResponse<BulkImportResult>>

    // // Contest Management Endpoints
    // @GET("api/admin/contests/{username}")
    // suspend fun getContests(
    //     @Path("username") username: String
    // ): Response<AdminResponse<List<Contest>>>

    // @GET("api/admin/contests/{username}/{contestId}")
    // suspend fun getContestById(
    //     @Path("username") username: String,
    //     @Path("contestId") contestId: String
    // ): Response<AdminResponse<Contest>>

    // @POST("api/admin/contests/{username}")
    // suspend fun createContest(
    //     @Path("username") username: String,
    //     @Body request: ContestCreateRequest
    // ): Response<AdminResponse<Contest>>

    // @PUT("api/admin/contests/{username}")
    // suspend fun updateContest(
    //     @Path("username") username: String,
    //     @Body request: ContestUpdateRequest
    // ): Response<AdminResponse<Contest>>

    // @DELETE("api/admin/contests/{username}/{contestId}")
    // suspend fun deleteContest(
    //     @Path("username") username: String,
    //     @Path("contestId") contestId: String
    // ): Response<AdminResponse<Boolean>>

    // @GET("api/admin/contests/{username}/{contestId}/stats")
    // suspend fun getContestStats(
    //     @Path("username") username: String,
    //     @Path("contestId") contestId: String
    // ): Response<AdminResponse<ContestStats>>
}

// Existing data classes
data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserData?
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: UserData?
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val createdAt: String?,
    val lastLogin: String?
)

data class UserData(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val createdAt: String?,
    val lastLogin: String?
)