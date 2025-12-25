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
    
    // Security Features
    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/auth/verify-pin")
    suspend fun verifyPinAndResetPassword(@Body request: VerifyPinRequest): Response<MessageResponse>
    
    // JWT Token refresh
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>
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
    val password: String,
    val captchaToken: String? = null  // Optional reCAPTCHA token
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

// Security Request Models
data class ForgotPasswordRequest(
    val email: String
)

data class VerifyPinRequest(
    val email: String,
    val pin: String,
    val newPassword: String
)

// JWT Token Request
data class RefreshTokenRequest(
    val refreshToken: String
)

// Response Models
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserResponse?,
    // JWT Tokens
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val accessTokenExpiresIn: Long? = null,  // seconds
    val refreshTokenExpiresIn: Long? = null, // seconds
    // Brute-force protection fields
    val remainingAttempts: Int? = null,
    val requiresCaptcha: Boolean = false
)

// JWT Refresh Token Response
data class RefreshTokenResponse(
    val success: Boolean,
    val message: String? = null,
    val accessToken: String? = null,
    val accessTokenExpiresIn: Long? = null  // seconds
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String = "USER",
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