package com.example.eduquizz.features.auth.data.api

/**
 * Request để cập nhật thông tin profile
 * Bao gồm cả thông tin nhạy cảm và không nhạy cảm
 * Dữ liệu được bảo vệ bởi HTTPS trong quá trình truyền tải
 */
data class UserProfileRequest(
    val fullName: String,
    val dateOfBirth: String, // Format: yyyy-MM-dd
    val gender: String, // Male, Female, Other
    val hometown: String,
    // Thông tin nhạy cảm - sẽ được mã hóa ở server
    val phoneNumber: String? = null,
    val cccd: String? = null,
    val cccdIssueDate: String? = null,
    val cccdIssuePlace: String? = null
)

/**
 * Response khi cập nhật profile thành công
 */
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val profileCompleted: Boolean = false // Flag để biết user đã hoàn thành profile chưa
)
