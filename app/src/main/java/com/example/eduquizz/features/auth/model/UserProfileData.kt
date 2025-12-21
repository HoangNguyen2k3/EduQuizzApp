package com.example.eduquizz.features.auth.model

/**
 * Data class cho thông tin profile của user
 * Bao gồm cả thông tin nhạy cảm và không nhạy cảm
 */
data class UserProfileData(
    // Thông tin không nhạy cảm (sẽ gửi lên server)
    val fullName: String = "",
    val dateOfBirth: String = "", // Format: yyyy-MM-dd
    val gender: String = "", // Male, Female, Other
    val hometown: String = "",
    
    // Thông tin nhạy cảm (chỉ lưu local, không gửi lên server)
    val phoneNumber: String = "",
    val cccd: String = "", // Số căn cước công dân
    val cccdIssueDate: String = "", // Ngày cấp CCCD
    val cccdIssuePlace: String = "" // Nơi cấp CCCD
)

/**
 * Enum cho giới tính
 */
enum class Gender(val displayName: String) {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác")
}

/**
 * Helper functions để ẩn thông tin nhạy cảm
 */
fun String.maskSensitiveData(): String {
    if (this.length <= 3) return this
    val lastThree = this.takeLast(3)
    val masked = "*".repeat(this.length - 3)
    return masked + lastThree
}
