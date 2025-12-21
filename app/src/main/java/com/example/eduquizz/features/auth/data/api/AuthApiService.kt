package com.example.eduquizz.features.auth.data.api

import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/user/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<UserResponse>

    @PUT("api/auth/user/{username}")
    suspend fun updateProfile(
        @Path("username") username: String,
        @Body request: UpdateProfileRequest
    ): Response<AuthResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @GET("api/auth/check-username/{username}")
    suspend fun checkUsername(@Path("username") username: String): Response<Map<String, Boolean>>

    @GET("api/auth/check-email/{email}")
    suspend fun checkEmail(@Path("email") email: String): Response<Map<String, Boolean>>

    @PUT("api/auth/profile/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: Long,
        @Body request: UserProfileRequest
    ): Response<UserProfileResponse>

    @GET("api/auth/profile-status/{userId}")
    suspend fun checkProfileCompletion(@Path("userId") userId: Long): Response<UserProfileResponse>
}

// Request Models
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String
)

data class LoginRequest(
    val usernameOrEmail: String,
    val password: String
)

data class UpdateProfileRequest(
    val fullName: String?,
    val phoneNumber: String?,
    val profileImageUrl: String?
)

data class ChangePasswordRequest(
    val username: String,
    val oldPassword: String,
    val newPassword: String
)

// Response Models
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse?
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String = "USER",  // NEW: Added role field with default value
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val createdAt: String?,
    val lastLogin: String?
){
    fun isAdmin(): Boolean = role == "ADMIN"
    fun isUser(): Boolean = role == "USER"
}

data class MessageResponse(
    val success: Boolean,
    val message: String
)