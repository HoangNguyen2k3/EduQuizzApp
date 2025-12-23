package com.example.eduquizz.features.auth.utils

object PasswordStrengthValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>,
        val strength: PasswordStrength
    )
    
    enum class PasswordStrength {
        WEAK, MEDIUM, STRONG
    }
    
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < 8) {
            errors.add("Phải có ít nhất 8 ký tự")
        }
        
        if (!password.any { it.isUpperCase() }) {
            errors.add("Phải có ít nhất 1 chữ in hoa")
        }
        
        if (!password.any { it.isDigit() }) {
            errors.add("Phải có ít nhất 1 số")
        }
        
        if (!password.any { it in "!@#\$%^&*()_+-=[]{}';:\"|,.<>/?" }) {
            errors.add("Phải có ít nhất 1 ký tự đặc biệt")
        }
        
        val strength = when {
            errors.isEmpty() && password.length >= 12 -> PasswordStrength.STRONG
            errors.size <= 1 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
        
        return ValidationResult(errors.isEmpty(), errors, strength)
    }
    
    /**
     * Get password strength score (0-100)
     */
    fun getStrengthScore(password: String): Int {
        var score = 0
        
        // Length score (max 40 points)
        score += when {
            password.length >= 12 -> 40
            password.length >= 8 -> 25
            password.length >= 6 -> 15
            else -> 5
        }
        
        // Uppercase letters (15 points)
        if (password.any { it.isUpperCase() }) score += 15
        
        // Lowercase letters (15 points)
        if (password.any { it.isLowerCase() }) score += 15
        
        // Numbers (15 points)
        if (password.any { it.isDigit() }) score += 15
        
        // Special characters (15 points)
        if (password.any { it in "!@#\$%^&*()_+-=[]{}';:\"|,.<>/?" }) score += 15
        
        return score.coerceIn(0, 100)
    }
}
