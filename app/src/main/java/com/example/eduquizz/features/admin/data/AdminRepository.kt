package com.example.eduquizz.features.admin.data

import com.example.eduquizz.features.auth.data.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun checkAdminStatus(username: String): Result<Boolean> {
        return try {
            val response = apiService.checkAdminStatus(username)
            if (response.isSuccessful) {
                Result.success(response.body()?.isAdmin ?: false)
            } else {
                Result.failure(Exception("Failed to check admin status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(username: String): Result<AdminDashboardStats> {
        return try {
            val response = apiService.getAdminDashboard(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load dashboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWordSearchLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getWordSearchLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBatChuLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getBatChuLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchGameLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getMatchGameLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuizLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getQuizLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSceneLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getSceneLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSoundLevels(username: String): Result<List<GameLevel>> {
        return try {
            val response = apiService.getSoundLevels(username)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception("Access denied"))
                }
            } else {
                Result.failure(Exception("Failed to load levels"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}