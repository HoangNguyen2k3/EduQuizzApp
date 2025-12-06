package com.example.eduquizz.features.admin.data

import java.util.UUID

/**
 * Mock data cho Question và Contest management
 * Sử dụng trong khi chờ backend implement các API endpoints
 */
object AdminMockData {
    
    private val mockQuestions = mutableListOf<QuestionItem>()
    private val mockContests = mutableListOf<Contest>()
    
    init {
        // Initialize với sample data
        initializeMockQuestions()
        initializeMockContests()
    }
    
    private fun initializeMockQuestions() {
        mockQuestions.addAll(listOf(
            QuestionItem(
                id = "q1",
                questionText = "Thủ đô của Việt Nam là gì?",
                choices = listOf(
                    QuestionChoice("A", "Hà Nội", true),
                    QuestionChoice("B", "TP. Hồ Chí Minh", false),
                    QuestionChoice("C", "Đà Nẵng", false),
                    QuestionChoice("D", "Huế", false)
                ),
                difficulty = "Easy",
                category = "Địa lý",
                points = 10,
                timeLimit = 30,
                createdAt = System.currentTimeMillis()
            ),
            QuestionItem(
                id = "q2",
                questionText = "Ai là tác giả của tác phẩm 'Truyện Kiều'?",
                choices = listOf(
                    QuestionChoice("A", "Hồ Xuân Hương", false),
                    QuestionChoice("B", "Nguyễn Du", true),
                    QuestionChoice("C", "Nguyễn Trãi", false),
                    QuestionChoice("D", "Hồ Chí Minh", false)
                ),
                difficulty = "Medium",
                category = "Văn học",
                points = 20,
                timeLimit = 45,
                createdAt = System.currentTimeMillis()
            ),
            QuestionItem(
                id = "q3",
                questionText = "2 + 2 = ?",
                choices = listOf(
                    QuestionChoice("A", "3", false),
                    QuestionChoice("B", "4", true),
                    QuestionChoice("C", "5", false),
                    QuestionChoice("D", "6", false)
                ),
                difficulty = "Easy",
                category = "Toán học",
                points = 10,
                timeLimit = 20,
                createdAt = System.currentTimeMillis()
            ),
            QuestionItem(
                id = "q4",
                questionText = "Công thức hóa học của nước là gì?",
                choices = listOf(
                    QuestionChoice("A", "H2O", true),
                    QuestionChoice("B", "CO2", false),
                    QuestionChoice("C", "O2", false),
                    QuestionChoice("D", "H2", false)
                ),
                difficulty = "Easy",
                category = "Hóa học",
                points = 10,
                timeLimit = 30,
                createdAt = System.currentTimeMillis()
            ),
            QuestionItem(
                id = "q5",
                questionText = "Định luật Newton thứ 2 phát biểu về điều gì?",
                choices = listOf(
                    QuestionChoice("A", "Lực và gia tốc", true),
                    QuestionChoice("B", "Năng lượng", false),
                    QuestionChoice("C", "Ánh sáng", false),
                    QuestionChoice("D", "Điện từ", false)
                ),
                difficulty = "Hard",
                category = "Vật lý",
                points = 30,
                timeLimit = 60,
                createdAt = System.currentTimeMillis()
            )
        ))
    }
    
    private fun initializeMockContests() {
        val currentTime = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        mockContests.addAll(listOf(
            Contest(
                id = "c1",
                title = "Cuộc thi Toán học Tháng 12",
                description = "Kiểm tra kiến thức toán học cơ bản",
                startTime = currentTime + oneDay, // Bắt đầu sau 1 ngày
                endTime = currentTime + (oneDay * 2), // Kết thúc sau 2 ngày
                duration = 30, // 30 phút
                totalQuestions = 10,
                questionIds = listOf("q3"),
                status = "scheduled",
                participantCount = 0,
                maxParticipants = 100,
                createdBy = "admin",
                createdAt = currentTime
            ),
            Contest(
                id = "c2",
                title = "Cuộc thi Văn học Việt Nam",
                description = "Kiểm tra kiến thức về văn học Việt Nam",
                startTime = currentTime - oneDay, // Đã bắt đầu từ 1 ngày trước
                endTime = currentTime + oneDay, // Kết thúc sau 1 ngày
                duration = 45,
                totalQuestions = 15,
                questionIds = listOf("q2"),
                status = "live",
                participantCount = 45,
                maxParticipants = 100,
                createdBy = "admin",
                createdAt = currentTime - oneDay
            ),
            Contest(
                id = "c3",
                title = "Cuộc thi Khoa học Tổng hợp",
                description = "Thi đấu kiến thức đa môn học",
                startTime = currentTime - (oneDay * 5), // Đã kết thúc
                endTime = currentTime - (oneDay * 4),
                duration = 60,
                totalQuestions = 20,
                questionIds = listOf("q1", "q3", "q4", "q5"),
                status = "ended",
                participantCount = 89,
                maxParticipants = 100,
                createdBy = "admin",
                createdAt = currentTime - (oneDay * 6)
            )
        ))
    }
    
    // ============ Question Management Functions ============
    
    fun getQuestions(filter: QuestionFilter? = null): List<QuestionItem> {
        var filtered = mockQuestions.toList()
        
        filter?.let {
            // Filter by category
            it.category?.let { cat ->
                if (cat.isNotEmpty()) {
                    filtered = filtered.filter { q -> q.category == cat }
                }
            }
            
            // Filter by difficulty
            it.difficulty?.let { diff ->
                if (diff.isNotEmpty()) {
                    filtered = filtered.filter { q -> q.difficulty == diff }
                }
            }
            
            // Filter by search query
            it.searchQuery?.let { query ->
                if (query.isNotEmpty()) {
                    filtered = filtered.filter { q ->
                        q.questionText.contains(query, ignoreCase = true) ||
                        q.category.contains(query, ignoreCase = true)
                    }
                }
            }
        }
        
        return filtered.sortedByDescending { it.createdAt }
    }
    
    fun getQuestionById(id: String): QuestionItem? {
        return mockQuestions.find { it.id == id }
    }
    
    fun createQuestion(request: QuestionCreateRequest): QuestionItem {
        val newQuestion = QuestionItem(
            id = UUID.randomUUID().toString(),
            questionText = request.questionText,
            choices = request.choices,
            difficulty = request.difficulty,
            category = request.category,
            points = request.points,
            timeLimit = request.timeLimit,
            createdAt = System.currentTimeMillis()
        )
        mockQuestions.add(newQuestion)
        return newQuestion
    }
    
    fun updateQuestion(request: QuestionUpdateRequest): QuestionItem? {
        val index = mockQuestions.indexOfFirst { it.id == request.id }
        if (index != -1) {
            val updated = QuestionItem(
                id = request.id,
                questionText = request.questionText,
                choices = request.choices,
                difficulty = request.difficulty,
                category = request.category,
                points = request.points,
                timeLimit = request.timeLimit,
                createdAt = mockQuestions[index].createdAt
            )
            mockQuestions[index] = updated
            return updated
        }
        return null
    }
    
    fun deleteQuestion(id: String): Boolean {
        return mockQuestions.removeIf { it.id == id }
    }
    
    fun bulkImportQuestions(questions: List<QuestionCreateRequest>): BulkImportResult {
        val created = mutableListOf<QuestionItem>()
        val failed = mutableListOf<String>()
        
        questions.forEach { request ->
            try {
                val newQuestion = createQuestion(request)
                created.add(newQuestion)
            } catch (e: Exception) {
                failed.add("Failed to import: ${request.questionText}")
            }
        }
        
        return BulkImportResult(
            totalImported = created.size,
            successCount = created.size,
            failureCount = failed.size,
            errors = failed
        )
    }
    
    // ============ Contest Management Functions ============
    
    fun getContests(): List<Contest> {
        // Update status based on current time
        val currentTime = System.currentTimeMillis()
        mockContests.forEach { contest ->
            contest.status = when {
                currentTime < contest.startTime -> "scheduled"
                currentTime in contest.startTime..contest.endTime -> "live"
                else -> "ended"
            }
        }
        return mockContests.sortedByDescending { it.createdAt }
    }
    
    fun getContestById(id: String): Contest? {
        return mockContests.find { it.id == id }
    }
    
    fun createContest(request: ContestCreateRequest): Contest {
        val newContest = Contest(
            id = UUID.randomUUID().toString(),
            title = request.title,
            description = request.description,
            startTime = request.startTime,
            endTime = request.endTime,
            duration = request.duration,
            totalQuestions = request.questionIds.size,
            questionIds = request.questionIds,
            status = if (System.currentTimeMillis() < request.startTime) "scheduled" else "live",
            participantCount = 0,
            maxParticipants = request.maxParticipants,
            createdBy = request.createdBy,
            createdAt = System.currentTimeMillis()
        )
        mockContests.add(newContest)
        return newContest
    }
    
    fun updateContest(request: ContestUpdateRequest): Contest? {
        val index = mockContests.indexOfFirst { it.id == request.id }
        if (index != -1) {
            val existing = mockContests[index]
            val updated = Contest(
                id = request.id,
                title = request.title,
                description = request.description,
                startTime = request.startTime,
                endTime = request.endTime,
                duration = request.duration,
                totalQuestions = request.questionIds.size,
                questionIds = request.questionIds,
                status = existing.status,
                participantCount = existing.participantCount,
                maxParticipants = request.maxParticipants,
                createdBy = existing.createdBy,
                createdAt = existing.createdAt
            )
            mockContests[index] = updated
            return updated
        }
        return null
    }
    
    fun deleteContest(id: String): Boolean {
        return mockContests.removeIf { it.id == id }
    }
    
    fun getContestStats(id: String): ContestStats? {
        val contest = getContestById(id) ?: return null
        
        // Generate mock leaderboard
        val leaderboard = List(5) { index ->
            LeaderboardEntry(
                rank = index + 1,
                username = "User${index + 1}",
                score = 100 - (index * 10),
                completionTime = 15 + (index * 3)
            )
        }
        
        return ContestStats(
            contestId = contest.id,
            totalParticipants = contest.participantCount,
            averageScore = 75.5,
            highestScore = 100,
            lowestScore = 50,
            completionRate = 85.0,
            leaderboard = leaderboard
        )
    }
    
    // Helper: Get all categories
    fun getAllCategories(): List<String> {
        return mockQuestions.map { it.category }.distinct().sorted()
    }
}
