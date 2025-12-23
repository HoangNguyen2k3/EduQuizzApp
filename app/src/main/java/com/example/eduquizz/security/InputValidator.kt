package com.example.eduquizz.security

import android.util.Patterns

/**
 * Input Validator để bảo vệ ứng dụng khỏi các cuộc tấn công injection
 * 
 * Chức năng:
 * - Chống SQL Injection
 * - Chống XSS (Cross-Site Scripting)
 * - Validate email, username, password
 * - Sanitize input trước khi hiển thị hoặc lưu trữ
 */
object InputValidator {

    // === SQL Injection Patterns ===
    private val SQL_INJECTION_PATTERNS = listOf(
        Regex("('|--|;|/\\*|\\*/)", RegexOption.IGNORE_CASE),
        Regex("(\\bOR\\b|\\bAND\\b)\\s*['\"]?\\s*\\d+\\s*['\"]?\\s*=", RegexOption.IGNORE_CASE),
        Regex("(\\bUNION\\b|\\bSELECT\\b|\\bINSERT\\b|\\bUPDATE\\b|\\bDELETE\\b|\\bDROP\\b|\\bCREATE\\b|\\bALTER\\b|\\bTRUNCATE\\b)", RegexOption.IGNORE_CASE),
        Regex("(\\bEXEC\\b|\\bEXECUTE\\b|\\bxp_|\\bsp_)", RegexOption.IGNORE_CASE)
    )

    // === XSS Patterns ===
    private val XSS_PATTERNS = listOf(
        Regex("<script[^>]*>", RegexOption.IGNORE_CASE),
        Regex("</script>", RegexOption.IGNORE_CASE),
        Regex("javascript:", RegexOption.IGNORE_CASE),
        Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE),  // onclick=, onload=, etc.
        Regex("<iframe", RegexOption.IGNORE_CASE),
        Regex("<object", RegexOption.IGNORE_CASE),
        Regex("<embed", RegexOption.IGNORE_CASE),
        Regex("<link", RegexOption.IGNORE_CASE),
        Regex("<meta", RegexOption.IGNORE_CASE),
        Regex("expression\\s*\\(", RegexOption.IGNORE_CASE),
        Regex("vbscript:", RegexOption.IGNORE_CASE)
    )

    // === Validation Results ===
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val errorMessages: List<String>) : ValidationResult()
        
        fun isValid() = this is Valid
        fun getErrors(): List<String> = when (this) {
            is Valid -> emptyList()
            is Invalid -> errorMessages
        }
    }

    // === Check for SQL Injection ===
    fun containsSqlInjection(input: String): Boolean {
        return SQL_INJECTION_PATTERNS.any { it.containsMatchIn(input) }
    }

    // === Check for XSS ===
    fun containsXss(input: String): Boolean {
        return XSS_PATTERNS.any { it.containsMatchIn(input) }
    }

    // === Check for any malicious content ===
    fun isMalicious(input: String): Boolean {
        return containsSqlInjection(input) || containsXss(input)
    }

    // === Sanitize HTML entities ===
    fun sanitizeHtml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;")
    }

    // === Remove all HTML tags ===
    fun stripHtml(input: String): String {
        return input.replace(Regex("<[^>]*>"), "")
    }

    // === Validate and sanitize general input ===
    fun validateInput(input: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (containsSqlInjection(input)) {
            errors.add("Input chứa ký tự không hợp lệ")
        }

        if (containsXss(input)) {
            errors.add("Input chứa mã độc hại")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    // === Validate Username ===
    fun validateUsername(username: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (username.isBlank()) {
            errors.add("Username không được để trống")
        }

        if (username.length < 3) {
            errors.add("Username phải có ít nhất 3 ký tự")
        }

        if (username.length > 30) {
            errors.add("Username không được vượt quá 30 ký tự")
        }

        // Chỉ cho phép chữ cái, số, underscore
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            errors.add("Username chỉ được chứa chữ cái, số và dấu gạch dưới")
        }

        if (isMalicious(username)) {
            errors.add("Username chứa ký tự không hợp lệ")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    // === Validate Email ===
    fun validateEmail(email: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (email.isBlank()) {
            errors.add("Email không được để trống")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add("Email không đúng định dạng")
        }

        if (isMalicious(email)) {
            errors.add("Email chứa ký tự không hợp lệ")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    // === Validate Password ===
    fun validatePassword(password: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < 8) {
            errors.add("Mật khẩu phải có ít nhất 8 ký tự")
        }

        if (!password.any { it.isUpperCase() }) {
            errors.add("Mật khẩu phải có ít nhất 1 chữ hoa")
        }

        if (!password.any { it.isLowerCase() }) {
            errors.add("Mật khẩu phải có ít nhất 1 chữ thường")
        }

        if (!password.any { it.isDigit() }) {
            errors.add("Mật khẩu phải có ít nhất 1 số")
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Mật khẩu phải có ít nhất 1 ký tự đặc biệt")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    // === Validate Chat Message ===
    fun validateChatMessage(message: String): ValidationResult {
        val errors = mutableListOf<String>()

        if (message.isBlank()) {
            errors.add("Tin nhắn không được để trống")
        }

        if (message.length > 1000) {
            errors.add("Tin nhắn không được vượt quá 1000 ký tự")
        }

        if (containsXss(message)) {
            errors.add("Tin nhắn chứa nội dung không hợp lệ")
        }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    // === Sanitize chat message for display ===
    fun sanitizeChatMessage(message: String): String {
        return stripHtml(message.trim())
            .take(1000) // Max 1000 chars
    }

    // === Validate and return clean input ===
    fun getCleanInput(input: String): String? {
        return if (validateInput(input).isValid()) {
            sanitizeHtml(input.trim())
        } else {
            null
        }
    }
}
