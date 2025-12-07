package com.example.eduquizz.features.admin.data

import com.example.eduquizz.features.auth.data.ApiService
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho Admin features
 * 
 * Hiện tại sử dụng MOCK DATA cho Question và Contest management
 * vì backend chưa implement các endpoints này.
 * 
 * Khi backend đã sẵn sàng:
 * 1. Set USE_MOCK_DATA = false
 * 2. Uncomment các endpoint trong ApiService.kt
 * 3. Test kết nối với backend thật
 */
@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        // Backend PHẢI chạy cho: Login, Check Admin
        // Mock data CHỈ dùng cho: Questions và Contests management
        // Set to false khi backend đã implement đầy đủ Question/Contest APIs
        private const val USE_MOCK_DATA = false  // false = kết nối backend thật
    }
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
            android.util.Log.d("AdminRepository", "Loading dashboard stats for: $username")
            val response = apiService.getAdminDashboard(username)
            android.util.Log.d("AdminRepository", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("AdminRepository", "Response body: $body")
                
                if (body?.success == true && body.stats != null) {
                    android.util.Log.d("AdminRepository", "Dashboard loaded successfully")
                    Result.success(body.stats)
                } else {
                    val errorMsg = body?.message ?: "Access denied"
                    android.util.Log.e("AdminRepository", "Dashboard error: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                android.util.Log.e("AdminRepository", "Failed to load dashboard: ${response.code()}")
                Result.failure(Exception("Failed to load dashboard: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AdminRepository", "Dashboard exception: ${e.message}", e)
            Result.failure(Exception("Cannot connect to server. Please check: 1) Backend is running, 2) Network connection. Error: ${e.message}"))
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

    // Question Management Functions
    suspend fun getQuestions(
        username: String,
        filter: QuestionFilter
    ): Result<List<QuestionItem>> {
        return if (USE_MOCK_DATA) {
            // Sử dụng mock data
            delay(300) // Simulate network delay
            try {
                Result.success(AdminMockData.getQuestions(filter))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Sử dụng real API (cần uncomment trong ApiService.kt)
            try {
                // val response = apiService.getQuestions(username, filter)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception("Access denied"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to load questions"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getQuestionById(username: String, questionId: String): Result<QuestionItem> {
        return if (USE_MOCK_DATA) {
            delay(200)
            try {
                val question = AdminMockData.getQuestionById(questionId)
                if (question != null) {
                    Result.success(question)
                } else {
                    Result.failure(Exception("Question not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.getQuestionById(username, questionId)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception("Question not found"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to load question"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createQuestion(
        username: String,
        request: QuestionCreateRequest
    ): Result<QuestionItem> {
        return if (USE_MOCK_DATA) {
            delay(400)
            try {
                val newQuestion = AdminMockData.createQuestion(request)
                Result.success(newQuestion)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.createQuestion(username, request)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception(body?.message ?: "Failed to create question"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to create question"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateQuestion(
        username: String,
        request: QuestionUpdateRequest
    ): Result<QuestionItem> {
        return if (USE_MOCK_DATA) {
            delay(400)
            try {
                val updated = AdminMockData.updateQuestion(request)
                if (updated != null) {
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Question not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.updateQuestion(username, request)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception(body?.message ?: "Failed to update question"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to update question"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteQuestion(username: String, questionId: String): Result<Boolean> {
        return if (USE_MOCK_DATA) {
            delay(300)
            try {
                val deleted = AdminMockData.deleteQuestion(questionId)
                Result.success(deleted)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.deleteQuestion(username, questionId)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     Result.success(body?.success ?: false)
                // } else {
                //     Result.failure(Exception("Failed to delete question"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun bulkImportQuestions(
        username: String,
        questions: List<QuestionCreateRequest>
    ): Result<BulkImportResult> {
        return if (USE_MOCK_DATA) {
            delay(800)
            try {
                val result = AdminMockData.bulkImportQuestions(questions)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.bulkImportQuestions(username, questions)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception(body?.message ?: "Import failed"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to import questions"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // Contest Management Functions
    suspend fun getContests(username: String): Result<List<Contest>> {
        return if (USE_MOCK_DATA) {
            delay(300)
            try {
                Result.success(AdminMockData.getContests())
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.getContests(username)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception("Access denied"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to load contests"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getContestById(username: String, contestId: String): Result<Contest> {
        return if (USE_MOCK_DATA) {
            delay(200)
            try {
                val contest = AdminMockData.getContestById(contestId)
                if (contest != null) {
                    Result.success(contest)
                } else {
                    Result.failure(Exception("Contest not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.getContestById(username, contestId)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception("Contest not found"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to load contest"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createContest(
        username: String,
        request: ContestCreateRequest
    ): Result<Contest> {
        return if (USE_MOCK_DATA) {
            delay(400)
            try {
                val newContest = AdminMockData.createContest(request)
                Result.success(newContest)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.createContest(username, request)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception(body?.message ?: "Failed to create contest"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to create contest"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateContest(
        username: String,
        request: ContestUpdateRequest
    ): Result<Contest> {
        return if (USE_MOCK_DATA) {
            delay(400)
            try {
                val updated = AdminMockData.updateContest(request)
                if (updated != null) {
                    Result.success(updated)
                } else {
                    Result.failure(Exception("Contest not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.updateContest(username, request)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception(body?.message ?: "Failed to update contest"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to update contest"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteContest(username: String, contestId: String): Result<Boolean> {
        return if (USE_MOCK_DATA) {
            delay(300)
            try {
                val deleted = AdminMockData.deleteContest(contestId)
                Result.success(deleted)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.deleteContest(username, contestId)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     Result.success(body?.success ?: false)
                // } else {
                //     Result.failure(Exception("Failed to delete contest"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getContestStats(username: String, contestId: String): Result<ContestStats> {
        return if (USE_MOCK_DATA) {
            delay(300)
            try {
                val stats = AdminMockData.getContestStats(contestId)
                if (stats != null) {
                    Result.success(stats)
                } else {
                    Result.failure(Exception("Contest not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            try {
                // val response = apiService.getContestStats(username, contestId)
                // if (response.isSuccessful) {
                //     val body = response.body()
                //     if (body?.success == true && body.data != null) {
                //         Result.success(body.data)
                //     } else {
                //         Result.failure(Exception("Failed to load stats"))
                //     }
                // } else {
                //     Result.failure(Exception("Failed to load stats"))
                // }
                Result.failure(Exception("API not implemented yet"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}