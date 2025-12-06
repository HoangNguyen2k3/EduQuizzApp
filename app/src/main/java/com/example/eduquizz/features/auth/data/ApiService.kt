package com.example.eduquizz.features.auth.data

import com.example.eduquizz.features.admin.data.AdminCheckResponse
import com.example.eduquizz.features.admin.data.AdminDashboardStats
import com.example.eduquizz.features.admin.data.AdminResponse
import com.example.eduquizz.features.admin.data.GameLevel
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