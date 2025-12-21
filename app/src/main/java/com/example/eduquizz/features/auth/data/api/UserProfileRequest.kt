package com.example.eduquizz.features.auth.data.api

/**
 * Request để cập nhật thông tin profile
 * CHỈ chứa thông tin KHÔNG nhạy cảm
 */
data class UserProfileRequest(
    val fullName: String,
    val dateOfBirth: String, // Format: yyyy-MM-dd
    val gender: String, // Male, Female, Other
    val hometown: String
)

/**
 * Response khi cập nhật profile thành công
 */
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val profileCompleted: Boolean = false // Flag để biết user đã hoàn thành profile chưa
)
