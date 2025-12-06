# HƯỚNG DẪN KẾT NỐI BACKEND CHO ADMIN PANEL

## Tình trạng hiện tại

### ✅ Backend đã có (hoạt động tốt):
- `AdminDashboardController.java` - Quản lý dashboard và game levels
  - `/api/admin/dashboard/{username}` - Dashboard stats
  - `/api/admin/wordsearch/{username}` - Word search levels
  - `/api/admin/batchu/{username}` - Bat chu levels
  - `/api/admin/matchgame/{username}` - Match game levels
  - `/api/admin/quiz/{username}` - Quiz levels
  - `/api/admin/scene/{username}` - Scene levels
  - `/api/admin/sound/{username}` - Sound levels

### ❌ Backend chưa có (cần implement):
- **Question Management API** - Quản lý câu hỏi
- **Contest Management API** - Quản lý cuộc thi

## Giải pháp tạm thời

### Frontend đang sử dụng MOCK DATA

File: `AdminRepository.kt`
```kotlin
companion object {
    // TODO: Đổi thành false khi backend đã implement
    private const val USE_MOCK_DATA = true
}
```

Khi `USE_MOCK_DATA = true`:
- ✅ Tất cả UI screens hoạt động bình thường
- ✅ Có thể test đầy đủ chức năng
- ✅ Dữ liệu được lưu trong memory (mất khi restart app)
- ⚠️ Không đồng bộ với database backend

File mock data: `AdminMockData.kt`
- 5 câu hỏi mẫu (Địa lý, Văn học, Toán học, Hóa học, Vật lý)
- 3 cuộc thi mẫu (scheduled, live, ended)
- Tất cả CRUD operations

## Các bước để kết nối backend thật

### Bước 1: Implement Backend Controllers

Đã tạo sẵn 2 file mẫu trong project:
1. **BACKEND_QuestionController.java** - Question Management API
2. **BACKEND_ContestController.java** - Contest Management API

Copy 2 file này vào backend project:
```
spring-boot-backend/
  src/main/java/com/springboot/admin/controller/
    ├── AdminDashboardController.java (đã có)
    ├── QuestionController.java (thêm mới)
    └── ContestController.java (thêm mới)
```

### Bước 2: Tạo Database Schema

#### 2.1. Questions Table
```sql
CREATE TABLE questions (
    id VARCHAR(36) PRIMARY KEY,
    question_text TEXT NOT NULL,
    difficulty VARCHAR(20) NOT NULL, -- Easy, Medium, Hard
    category VARCHAR(100) NOT NULL,
    points INT NOT NULL DEFAULT 10,
    time_limit INT NOT NULL DEFAULT 30, -- seconds
    created_at BIGINT NOT NULL,
    INDEX idx_category (category),
    INDEX idx_difficulty (difficulty)
);

CREATE TABLE question_choices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id VARCHAR(36) NOT NULL,
    choice_label VARCHAR(5) NOT NULL, -- A, B, C, D
    choice_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);
```

#### 2.2. Contests Table
```sql
CREATE TABLE contests (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time BIGINT NOT NULL,
    end_time BIGINT NOT NULL,
    duration INT NOT NULL, -- minutes
    total_questions INT NOT NULL,
    status VARCHAR(20) NOT NULL, -- scheduled, live, ended
    participant_count INT NOT NULL DEFAULT 0,
    max_participants INT NOT NULL DEFAULT 100,
    created_by VARCHAR(100) NOT NULL,
    created_at BIGINT NOT NULL,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);

CREATE TABLE contest_questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contest_id VARCHAR(36) NOT NULL,
    question_id VARCHAR(36) NOT NULL,
    question_order INT NOT NULL,
    FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);

CREATE TABLE contest_participants (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contest_id VARCHAR(36) NOT NULL,
    username VARCHAR(100) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    completion_time INT, -- seconds
    submitted_at BIGINT,
    FOREIGN KEY (contest_id) REFERENCES contests(id) ON DELETE CASCADE,
    UNIQUE KEY unique_participant (contest_id, username)
);
```

### Bước 3: Tạo Entity Classes

#### Question.java
```java
package com.springboot.admin.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    private String id;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionChoice> choices;
    
    @Column(nullable = false)
    private String difficulty;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Integer points;
    
    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit;
    
    @Column(name = "created_at", nullable = false)
    private Long createdAt;
    
    // Getters & Setters
}

@Entity
@Table(name = "question_choices")
public class QuestionChoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(name = "choice_label", length = 5, nullable = false)
    private String choiceLabel;
    
    @Column(name = "choice_text", columnDefinition = "TEXT", nullable = false)
    private String choiceText;
    
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
    
    // Getters & Setters
}
```

#### Contest.java
```java
package com.springboot.admin.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "contests")
public class Contest {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "start_time", nullable = false)
    private Long startTime;
    
    @Column(name = "end_time", nullable = false)
    private Long endTime;
    
    @Column(nullable = false)
    private Integer duration;
    
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;
    
    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private Long createdAt;
    
    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContestQuestion> contestQuestions;
    
    // Getters & Setters
}
```

### Bước 4: Tạo Service Classes

Tạo logic xử lý trong:
- `QuestionService.java` - CRUD operations cho questions
- `ContestService.java` - CRUD operations cho contests

### Bước 5: Test Backend APIs

Sử dụng file `request.http` trong backend project:

```http
### Test Question APIs

# Get all questions (with filter)
POST http://localhost:8080/api/admin/questions/admin/filter
Content-Type: application/json

{
  "category": "",
  "difficulty": "",
  "searchQuery": ""
}

# Create question
POST http://localhost:8080/api/admin/questions/admin
Content-Type: application/json

{
  "questionText": "Thủ đô của Việt Nam là gì?",
  "choices": [
    {"choiceLabel": "A", "choiceText": "Hà Nội", "isCorrect": true},
    {"choiceLabel": "B", "choiceText": "TP.HCM", "isCorrect": false}
  ],
  "difficulty": "Easy",
  "category": "Địa lý",
  "points": 10,
  "timeLimit": 30
}

### Test Contest APIs

# Get all contests
GET http://localhost:8080/api/admin/contests/admin

# Create contest
POST http://localhost:8080/api/admin/contests/admin
Content-Type: application/json

{
  "title": "Cuộc thi Toán học",
  "description": "Test kiến thức toán",
  "startTime": 1733500000000,
  "endTime": 1733600000000,
  "duration": 30,
  "questionIds": ["q1", "q2"],
  "maxParticipants": 100,
  "createdBy": "admin"
}
```

### Bước 6: Kết nối Frontend

Sau khi backend APIs hoạt động:

1. **Uncomment API endpoints trong ApiService.kt**
```kotlin
// Bỏ comment các endpoints trong ApiService.kt
@GET("api/admin/questions/{username}/filter")
suspend fun getQuestions(...)

@POST("api/admin/contests/{username}")
suspend fun createContest(...)
// ... các endpoints khác
```

2. **Đổi USE_MOCK_DATA = false trong AdminRepository.kt**
```kotlin
companion object {
    private const val USE_MOCK_DATA = false // Đổi thành false
}
```

3. **Rebuild app và test**
```bash
# Clean build
./gradlew clean
./gradlew build

# Run app
./gradlew installDebug
```

### Bước 7: Verify Integration

Test từng chức năng:
- ✅ Login với admin account
- ✅ Vào Admin Dashboard
- ✅ Vào Question Management → Create/Edit/Delete questions
- ✅ Vào Contest Management → Create/Edit/Delete contests
- ✅ Check dữ liệu trong database backend

## Cấu trúc Package Backend (Gợi ý)

```
com/springboot/admin/
├── controller/
│   ├── AdminDashboardController.java (đã có)
│   ├── QuestionController.java (thêm mới)
│   └── ContestController.java (thêm mới)
├── entity/
│   ├── Question.java (thêm mới)
│   ├── QuestionChoice.java (thêm mới)
│   ├── Contest.java (thêm mới)
│   └── ContestQuestion.java (thêm mới)
├── repository/
│   ├── QuestionRepository.java (thêm mới)
│   ├── QuestionChoiceRepository.java (thêm mới)
│   ├── ContestRepository.java (thêm mới)
│   └── ContestQuestionRepository.java (thêm mới)
├── service/
│   ├── QuestionService.java (thêm mới)
│   └── ContestService.java (thêm mới)
└── dto/
    ├── QuestionCreateRequest.java (thêm mới)
    ├── QuestionUpdateRequest.java (thêm mới)
    ├── QuestionFilter.java (thêm mới)
    ├── ContestCreateRequest.java (thêm mới)
    ├── ContestUpdateRequest.java (thêm mới)
    ├── BulkImportResult.java (thêm mới)
    └── ContestStats.java (thêm mới)
```

## API Response Format

Backend cần trả về đúng format:

### Success Response
```json
{
  "success": true,
  "data": {
    // Dữ liệu trả về
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message here"
}
```

## Checklist Implementation

### Backend Team:
- [ ] Tạo database schema (questions, contests tables)
- [ ] Tạo Entity classes
- [ ] Tạo Repository interfaces
- [ ] Implement QuestionService.java
- [ ] Implement ContestService.java
- [ ] Add QuestionController.java (sử dụng file mẫu)
- [ ] Add ContestController.java (sử dụng file mẫu)
- [ ] Test tất cả endpoints với Postman/request.http
- [ ] Commit và push lên branch `thong`

### Frontend Team (sau khi backend xong):
- [ ] Uncomment các API endpoints trong ApiService.kt
- [ ] Đổi USE_MOCK_DATA = false trong AdminRepository.kt
- [ ] Test login với admin account
- [ ] Test Question Management (CRUD)
- [ ] Test Contest Management (CRUD)
- [ ] Test trên nhiều thiết bị
- [ ] Fix bugs nếu có

## Contact & Support

Nếu có vấn đề khi implement:
1. Check logs trong Android Studio (Logcat)
2. Check backend console logs
3. Verify database connections
4. Test endpoints riêng lẻ với Postman
5. Review code trong BACKEND_QuestionController.java và BACKEND_ContestController.java

## Notes

- Backend URL: `http://10.0.2.2:8080` (Android emulator)
- Cho thiết bị thật: Dùng IP máy chủ thực (VD: `http://192.168.1.100:8080`)
- Package backend: `com.springboot` (giữ nguyên)
- Database: PostgreSQL (theo pom.xml hiện tại)
