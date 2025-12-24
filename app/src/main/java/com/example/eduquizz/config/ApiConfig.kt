package com.example.eduquizz.config

/**
 * ApiConfig - Cấu hình API tập trung
 * 
 * ⚠️ ĐỔI URL TẠI ĐÂY KHI THAY ĐỔI SERVER:
 * - Ngrok HTTPS: "https://xxx.ngrok-free.dev/"
 * - Local network (HTTP): "http://192.168.1.16:8080/"
 * - Production: "https://your-domain.com/"
 */
object ApiConfig {
    
    /**
     * Base URL for all API calls
     * Đang dùng Ngrok HTTPS tunnel
     */
    const val BASE_URL = "https://ferrous-bolometrically-leslie.ngrok-free.dev/"
    
    // Local network (backup)
    // const val BASE_URL = "http://192.168.1.16:8080/"
    
    /**
     * API Endpoints
     */
    object Endpoints {
        const val AUTH = "api/auth/"
        const val SCENE = "api/scene/"
        const val SOUND = "api/sound/"
        const val BATCHU = "api/batchu/"
        const val WORDSEARCH = "api/wordsearch/"
        const val MATCH = "api/match/"
        const val MAPPING = "api/mapping/"
        const val CONTEST = "api/contest/"
        const val ADMIN = "api/admin/"
    }
    
    /**
     * Full URLs (BASE_URL + Endpoint)
     */
    fun getFullUrl(endpoint: String): String = BASE_URL + endpoint
}
