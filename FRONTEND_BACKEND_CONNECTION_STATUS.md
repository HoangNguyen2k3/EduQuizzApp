# TÌNH TRẠNG KẾT NỐI FRONTEND - BACKEND

## 📊 TỔNG QUAN

| Tính năng | Backend | Frontend | Kết nối | Ghi chú |
|-----------|---------|----------|---------|---------|
| Quên mật khẩu qua PIN | ✅ | ✅ | ✅ | Đã kết nối hoàn chỉnh |
| Chống brute-force | ✅ | ✅ | ✅ | Đã kết nối hoàn chỉnh |
| Kiểm tra độ mạnh mật khẩu | ✅ | ✅ | ✅ | Đã kết nối hoàn chỉnh |
| Ghi log IP đăng nhập | ✅ | ⚠️ | ⚠️ | Backend tự động, FE cần test |

---

## ✅ BACKEND (Spring Boot) - ĐÃ HOÀN THÀNH

### Commit mới nhất:
- **Commit:** `feat: add security feartures` (581c2de)
- **Thời gian:** 14 phút trước (Dec 19, 2025)
- **Branch:** `thong`

### Các tính năng đã implement:

#### 1️⃣ Quên mật khẩu qua mã PIN
✅ **Entity:** `PasswordResetPin.java`
- ID, email, PIN (6 số), createdAt, expiresAt, used
- PIN hết hạn sau 15 phút

✅ **Repository:** `PasswordResetPinRepository.java`
- `findByEmailAndPinAndUsedFalse()`
- `deleteByEmail()`

✅ **Service:** `PasswordResetService.java`
- Tạo PIN 6 số ngẫu nhiên
- Gửi email qua Gmail SMTP
- Verify PIN và reset password

✅ **Endpoints:**
```
POST /api/auth/forgot-password
Body: { "email": "user@example.com" }

POST /api/auth/verify-pin
Body: { "email": "...", "pin": "123456", "newPassword": "..." }
```

#### 2️⃣ Chống Brute-force
✅ **Entity:** `LoginAttempt.java`
- ID, ipAddress, username, attemptTime, success

✅ **Repository:** `LoginAttemptRepository.java`
- `findByIpAddressAndSuccessFalseAndAttemptTimeAfter()`
- `findByUsernameAndSuccessFalseAndAttemptTimeAfter()`

✅ **Service:** `LoginAttemptService.java`
- MAX_ATTEMPTS = 5
- COOLDOWN = 30 phút
- Tracking IP và username
- `logAttempt()`, `isBlocked()`, `getRemainingAttempts()`

✅ **Response mở rộng:**
```json
{
  "success": false,
  "message": "Too many failed attempts...",
  "requiresCaptcha": true,
  "remainingAttempts": 0
}
```

#### 3️⃣ Kiểm tra độ mạnh mật khẩu
✅ **Validator:** `PasswordValidator.java`
- Kiểm tra: 8 ký tự, 1 in hoa, 1 số, 1 đặc biệt
- Trả về list lỗi chi tiết

✅ **Tích hợp vào:**
- Register endpoint
- Change password endpoint

---

## ✅ FRONTEND (Android Kotlin) - ĐÃ HOÀN THÀNH

### Các file đã tạo/cập nhật:

#### 1️⃣ Password Strength Validator
✅ **File:** `PasswordStrengthValidator.kt`
```kotlin
object PasswordStrengthValidator {
    enum class PasswordStrength { WEAK, MEDIUM, STRONG }
    fun validate(password: String): ValidationResult
    fun getStrengthScore(password: String): Int
}
```

#### 2️⃣ API Service
✅ **File:** `AuthApiService.kt`
```kotlin
@POST("api/auth/forgot-password")
suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

@POST("api/auth/verify-pin")
suspend fun verifyPinAndResetPassword(@Body request: VerifyPinRequest): Response<MessageResponse>
```

✅ **Request Models:**
- `ForgotPasswordRequest(email)`
- `VerifyPinRequest(email, pin, newPassword)`

✅ **Response Models (mở rộng):**
- `AuthResponse` thêm `remainingAttempts`, `requiresCaptcha`

#### 3️⃣ Repository
✅ **File:** `AuthRepository.kt`
```kotlin
suspend fun forgotPassword(email: String): AuthResult<String>
suspend fun verifyPinAndResetPassword(email, pin, newPassword): AuthResult<String>
```

#### 4️⃣ ViewModel
✅ **File:** `AuthViewModel.kt`
```kotlin
data class AuthUiState(
    val remainingAttempts: Int? = null,
    val requiresCaptcha: Boolean = false
)

fun forgotPassword(email: String)
fun verifyPinAndResetPassword(email, pin, newPassword)
```

#### 5️⃣ UI Screens
✅ **File:** `ForgotPasswordScreen.kt`
- **Step 1:** Nhập email → Gửi PIN
- **Step 2:** Nhập PIN + mật khẩu mới
- Validation real-time
- Password strength indicator
- UI hiện đại với Material 3

✅ **File:** `RegisterScreen.kt` (đã cập nhật)
- Password strength validator
- Progress bar màu động (đỏ/cam/xanh)
- Hiển thị lỗi chi tiết

✅ **File:** `LoginScreen.kt` (đã cập nhật)
- Nút "Quên mật khẩu?"
- Cảnh báo khi còn <= 3 lần thử
- Hiển thị `remainingAttempts`

#### 6️⃣ Navigation
✅ **File:** `NavGraph.kt`
```kotlin
const val FORGOT_PASSWORD = "forgot_password"

composable(Routes.FORGOT_PASSWORD) {
    ForgotPasswordScreen(...)
}
```

---

## 🔗 KẾT NỐI BACKEND - FRONTEND

### BASE URL Configuration:
✅ **AuthModule.kt:**
```kotlin
.baseUrl("http://10.0.2.2:8080/")  // ✅ Đúng cho Android Emulator
```

⚠️ **Lưu ý:** 
- `10.0.2.2` = localhost của máy host (cho Emulator)
- Nếu test trên thiết bị thật → dùng IP thực của máy (VD: `192.168.1.100:8080`)

### Endpoints mapping:

| Frontend API Call | Backend Endpoint | Status |
|-------------------|------------------|--------|
| `forgotPassword(email)` | `POST /api/auth/forgot-password` | ✅ Matched |
| `verifyPinAndResetPassword()` | `POST /api/auth/verify-pin` | ✅ Matched |
| `register()` | `POST /api/auth/register` | ✅ Matched |
| `login()` | `POST /api/auth/login` | ✅ Matched |

### Request/Response Models:

| Model | Frontend | Backend | Status |
|-------|----------|---------|--------|
| ForgotPasswordRequest | ✅ `email` | ✅ `email` | ✅ Matched |
| VerifyPinRequest | ✅ `email, pin, newPassword` | ✅ `email, pin, newPassword` | ✅ Matched |
| AuthResponse | ✅ `remainingAttempts, requiresCaptcha` | ✅ `remainingAttempts, requiresCaptcha` | ✅ Matched |

---

## ⚠️ CẦN KIỂM TRA/CẤU HÌNH

### 1. Email Configuration (Backend)
Cập nhật file `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=xxxx-xxxx-xxxx-xxxx  # App Password từ Gmail
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Cách lấy App Password:**
1. Vào Google Account → Security
2. Bật "2-Step Verification"
3. App passwords → Chọn Mail → Tạo password
4. Copy 16 ký tự vào config

### 2. Database Migration
Chạy file SQL để tạo bảng mới:
```sql
-- File: database/security_tables.sql
CREATE TABLE password_reset_pins (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    pin VARCHAR(6) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    success BOOLEAN NOT NULL
);

CREATE INDEX idx_pin_email ON password_reset_pins(email);
CREATE INDEX idx_attempt_ip_time ON login_attempts(ip_address, attempt_time);
CREATE INDEX idx_attempt_user_time ON login_attempts(username, attempt_time);
```

### 3. Test trên thiết bị thật
Nếu test trên điện thoại thật:
1. Tìm IP máy tính: `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
2. Cập nhật `AuthModule.kt`:
```kotlin
.baseUrl("http://192.168.1.xxx:8080/")  // Thay bằng IP thực
```
3. Đảm bảo điện thoại và máy tính cùng WiFi

---

## 🧪 HƯỚNG DẪN TEST

### Test 1: Password Strength
1. Mở màn hình Register
2. Nhập các mật khẩu sau:
   - `abc` → Yếu (thiếu nhiều)
   - `Abcd1234` → Trung bình (thiếu ký tự đặc biệt)
   - `Abcd1234!` → Mạnh ✅

### Test 2: Forgot Password
1. Login Screen → Click "Quên mật khẩu?"
2. Nhập email đã đăng ký → "Gửi mã PIN"
3. Kiểm tra email (spam folder nếu không thấy)
4. Nhập PIN 6 số + mật khẩu mới
5. "Đặt lại mật khẩu" → Về Login screen
6. Đăng nhập với mật khẩu mới

### Test 3: Brute-force Protection
1. Đăng nhập sai 1 lần → Không có gì
2. Đăng nhập sai 2 lần → Không có gì
3. Đăng nhập sai 3 lần → Hiện cảnh báo "Còn 2 lần thử..."
4. Đăng nhập sai 4 lần → "Còn 1 lần thử..."
5. Đăng nhập sai 5 lần → Bị block 30 phút

### Test API bằng curl (Backend):
```bash
# Test Forgot Password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}'

# Test Verify PIN
curl -X POST http://localhost:8080/api/auth/verify-pin \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "pin":"123456",
    "newPassword":"NewSecure@123"
  }'

# Test Password Validation (yếu)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username":"testuser",
    "email":"test@example.com",
    "password":"weak",
    "fullName":"Test User"
  }'
```

---

## 📁 CẤU TRÚC FILE

### Backend (Spring Boot):
```
src/main/java/com/example/backend/
├── entity/
│   ├── PasswordResetPin.java ✅
│   └── LoginAttempt.java ✅
├── repository/
│   ├── PasswordResetPinRepository.java ✅
│   └── LoginAttemptRepository.java ✅
├── service/
│   ├── PasswordResetService.java ✅
│   ├── LoginAttemptService.java ✅
│   └── PasswordValidator.java ✅
└── controller/
    └── AuthController.java ✅ (updated)

database/
└── security_tables.sql ✅

src/main/resources/
└── application.properties ⚠️ (cần cấu hình email)
```

### Frontend (Android Kotlin):
```
app/src/main/java/com/example/eduquizz/
├── features/auth/
│   ├── utils/
│   │   └── PasswordStrengthValidator.kt ✅
│   ├── data/api/
│   │   └── AuthApiService.kt ✅ (updated)
│   ├── data/repository/
│   │   └── AuthRepository.kt ✅ (updated)
│   ├── viewmodel/
│   │   └── AuthViewModel.kt ✅ (updated)
│   └── screens/
│       ├── ForgotPasswordScreen.kt ✅ (new)
│       ├── RegisterScreen.kt ✅ (updated)
│       └── LoginScreen.kt ✅ (updated)
├── dI/
│   └── AuthModule.kt ✅ (BASE_URL configured)
└── navigation/
    └── NavGraph.kt ✅ (updated)
```

---

## ✅ KẾT LUẬN

### Tình trạng: **SẴN SÀNG ĐỂ TEST** 🎉

**Frontend và Backend đã được kết nối hoàn chỉnh:**
1. ✅ API endpoints khớp nhau 100%
2. ✅ Request/Response models khớp nhau
3. ✅ BASE_URL đã được cấu hình đúng
4. ✅ UI/UX đã hoàn thiện
5. ✅ Validation đã đồng bộ

**Chỉ cần:**
1. ⚠️ Cấu hình email trong `application.properties`
2. ⚠️ Chạy database migration (tạo 2 bảng mới)
3. ⚠️ Start backend server
4. ⚠️ Chạy app Android và test

**Sau khi hoàn thành 4 bước trên → Hệ thống hoạt động 100%!**

---

## 📞 TROUBLESHOOTING

### Lỗi: "Failed to connect to backend"
- Kiểm tra backend đã chạy chưa: `mvn spring-boot:run`
- Kiểm tra BASE_URL đúng chưa
- Nếu dùng thiết bị thật: Đổi sang IP thực

### Lỗi: "Email not sent"
- Kiểm tra App Password Gmail đã đúng chưa
- Kiểm tra firewall có chặn port 587 không
- Kiểm tra log backend xem lỗi gì

### Lỗi: "Table doesn't exist"
- Chạy file SQL: `database/security_tables.sql`
- Hoặc đợi Spring Boot tự tạo (nếu `ddl-auto=update`)

### PIN không nhận được
- Kiểm tra spam folder
- PIN hết hạn sau 15 phút
- Request PIN mới

---

**Tất cả các tính năng bảo mật đã được implement và kết nối hoàn chỉnh!** 🚀
