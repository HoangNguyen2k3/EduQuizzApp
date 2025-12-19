 # HƯỚNG DẪN TRIỂN KHAI CÁC TÍNH NĂNG BẢO MẬT

## PHÂN TÍCH HIỆN TRẠNG

### ✅ Đã có:
- ✅ Đăng ký/Đăng nhập cơ bản
- ✅ Đổi mật khẩu
- ✅ Google Sign-In
- ⚠️ Validation mật khẩu cơ bản (chỉ kiểm tra >= 6 ký tự)

### ❌ Chưa có:
- ❌ Quên mật khẩu qua mã PIN
- ❌ Chống brute-force đăng nhập
- ❌ Captcha sau nhiều lần đăng nhập sai
- ❌ Ghi log IP đăng nhập sai
- ❌ Kiểm tra độ mạnh mật khẩu đầy đủ (8 ký tự, 1 in hoa, 1 đặc biệt, 1 số)

---

## 1️⃣ QUÊN MẬT KHẨU QUA MÃ PIN

### **BACKEND (Spring Boot)**

#### 1.1. Tạo Entity cho PIN Reset

Tạo file: `src/main/java/com/example/backend/entity/PasswordResetPin.java`

```java
package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "password_reset_pins")
public class PasswordResetPin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false, length = 6)
    private String pin;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean used = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusMinutes(15); // PIN hết hạn sau 15 phút
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
```

#### 1.2. Repository

Tạo file: `src/main/java/com/example/backend/repository/PasswordResetPinRepository.java`

```java
package com.example.backend.repository;

import com.example.backend.entity.PasswordResetPin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetPinRepository extends JpaRepository<PasswordResetPin, Long> {
    Optional<PasswordResetPin> findByEmailAndPinAndUsedFalse(String email, String pin);
    void deleteByEmail(String email);
}
```

#### 1.3. Service

Thêm vào `AuthService.java`:

```java
@Autowired
private PasswordResetPinRepository pinRepository;

@Autowired
private JavaMailSender mailSender;

public String generateResetPin(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Email not found"));
    
    // Xóa PIN cũ nếu có
    pinRepository.deleteByEmail(email);
    
    // Tạo PIN 6 số ngẫu nhiên
    String pin = String.format("%06d", new Random().nextInt(999999));
    
    PasswordResetPin resetPin = new PasswordResetPin();
    resetPin.setEmail(email);
    resetPin.setPin(pin);
    pinRepository.save(resetPin);
    
    // Gửi email
    sendPinEmail(email, pin);
    
    return "PIN sent to your email";
}

private void sendPinEmail(String email, String pin) {
    try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset PIN");
        message.setText("Your password reset PIN is: " + pin + "\n\nThis PIN will expire in 15 minutes.");
        mailSender.send(message);
    } catch (Exception e) {
        throw new RuntimeException("Failed to send email");
    }
}

public String verifyPinAndResetPassword(String email, String pin, String newPassword) {
    PasswordResetPin resetPin = pinRepository.findByEmailAndPinAndUsedFalse(email, pin)
        .orElseThrow(() -> new RuntimeException("Invalid or expired PIN"));
    
    if (resetPin.isExpired()) {
        throw new RuntimeException("PIN has expired");
    }
    
    // Đổi mật khẩu
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found"));
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    
    // Đánh dấu PIN đã dùng
    resetPin.setUsed(true);
    pinRepository.save(resetPin);
    
    return "Password reset successfully";
}
```

#### 1.4. Controller

Thêm vào `AuthController.java`:

```java
@PostMapping("/forgot-password")
public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
    try {
        String message = authService.generateResetPin(request.get("email"));
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}

@PostMapping("/verify-pin")
public ResponseEntity<?> verifyPinAndResetPassword(@RequestBody Map<String, String> request) {
    try {
        String message = authService.verifyPinAndResetPassword(
            request.get("email"),
            request.get("pin"),
            request.get("newPassword")
        );
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

#### 1.5. application.properties

```properties
# Email configuration (Gmail example)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **FRONTEND (Android)**

#### 1.6. API Service

Thêm vào `AuthApiService.kt`:

```kotlin
@POST("api/auth/forgot-password")
suspend fun forgotPassword(@Body request: Map<String, String>): Response<MessageResponse>

@POST("api/auth/verify-pin")
suspend fun verifyPinAndResetPassword(@Body request: Map<String, String>): Response<MessageResponse>
```

#### 1.7. Repository

Thêm vào `AuthRepository.kt`:

```kotlin
suspend fun forgotPassword(email: String): AuthResult<String> {
    return try {
        val response = apiService.forgotPassword(mapOf("email" to email))
        if (response.isSuccessful && response.body()?.success == true) {
            AuthResult.Success(response.body()?.message ?: "PIN sent")
        } else {
            AuthResult.Error(response.body()?.message ?: "Failed to send PIN")
        }
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Network error")
    }
}

suspend fun verifyPinAndResetPassword(email: String, pin: String, newPassword: String): AuthResult<String> {
    return try {
        val response = apiService.verifyPinAndResetPassword(
            mapOf("email" to email, "pin" to pin, "newPassword" to newPassword)
        )
        if (response.isSuccessful && response.body()?.success == true) {
            AuthResult.Success(response.body()?.message ?: "Password reset successfully")
        } else {
            AuthResult.Error(response.body()?.message ?: "Failed to reset password")
        }
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Network error")
    }
}
```

#### 1.8. ViewModel

Thêm vào `AuthViewModel.kt`:

```kotlin
fun forgotPassword(email: String) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        when (val result = repository.forgotPassword(email)) {
            is AuthResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = result.data
                )
            }
            is AuthResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }
}

fun verifyPinAndResetPassword(email: String, pin: String, newPassword: String) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        when (val result = repository.verifyPinAndResetPassword(email, pin, newPassword)) {
            is AuthResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = result.data
                )
            }
            is AuthResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }
}
```

#### 1.9. UI Screen

Tạo file: `ForgotPasswordScreen.kt`

```kotlin
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: email, 2: pin & password
    
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (step == 1) {
            // Step 1: Nhập email
            Text("Enter your email to receive PIN", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.forgotPassword(email)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send PIN")
            }
            
            // Hiển thị success message và chuyển sang step 2
            LaunchedEffect(uiState.successMessage) {
                if (uiState.successMessage != null) {
                    step = 2
                }
            }
        } else {
            // Step 2: Nhập PIN và mật khẩu mới
            Text("Enter PIN and new password", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("6-digit PIN") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (newPassword == confirmPassword) {
                        viewModel.verifyPinAndResetPassword(email, pin, newPassword)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Password")
            }
        }
        
        // Error/Success messages
        uiState.errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
        uiState.successMessage?.let {
            Text(it, color = Color.Green, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
```

---

## 2️⃣ CHỐNG BRUTE-FORCE & CAPTCHA

### **BACKEND (Spring Boot)**

#### 2.1. Entity cho Login Attempts

```java
@Entity
@Data
@Table(name = "login_attempts")
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private LocalDateTime attemptTime;
    
    @Column(nullable = false)
    private boolean success;
}
```

#### 2.2. Repository

```java
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    List<LoginAttempt> findByIpAddressAndSuccessFalseAndAttemptTimeAfter(
        String ipAddress, LocalDateTime since);
    
    List<LoginAttempt> findByUsernameAndSuccessFalseAndAttemptTimeAfter(
        String username, LocalDateTime since);
}
```

#### 2.3. Service

```java
@Service
public class LoginAttemptService {
    @Autowired
    private LoginAttemptRepository attemptRepository;
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_MINUTES = 30;
    
    public void logAttempt(String ipAddress, String username, boolean success) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setIpAddress(ipAddress);
        attempt.setUsername(username);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setSuccess(success);
        attemptRepository.save(attempt);
    }
    
    public boolean isBlocked(String ipAddress, String username) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(COOLDOWN_MINUTES);
        
        List<LoginAttempt> ipAttempts = attemptRepository
            .findByIpAddressAndSuccessFalseAndAttemptTimeAfter(ipAddress, since);
        
        List<LoginAttempt> userAttempts = attemptRepository
            .findByUsernameAndSuccessFalseAndAttemptTimeAfter(username, since);
        
        return ipAttempts.size() >= MAX_ATTEMPTS || userAttempts.size() >= MAX_ATTEMPTS;
    }
    
    public int getRemainingAttempts(String ipAddress, String username) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(COOLDOWN_MINUTES);
        
        List<LoginAttempt> attempts = attemptRepository
            .findByIpAddressAndSuccessFalseAndAttemptTimeAfter(ipAddress, since);
        
        return Math.max(0, MAX_ATTEMPTS - attempts.size());
    }
}
```

#### 2.4. Cập nhật AuthController

```java
@Autowired
private LoginAttemptService loginAttemptService;

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    String ipAddress = getClientIP(httpRequest);
    
    // Kiểm tra block
    if (loginAttemptService.isBlocked(ipAddress, request.getUsernameOrEmail())) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(Map.of(
                "success", false,
                "message", "Too many failed attempts. Please try again in 30 minutes.",
                "requiresCaptcha", true
            ));
    }
    
    try {
        User user = authService.login(request.getUsernameOrEmail(), request.getPassword());
        loginAttemptService.logAttempt(ipAddress, request.getUsernameOrEmail(), true);
        return ResponseEntity.ok(Map.of("success", true, "user", user));
    } catch (Exception e) {
        loginAttemptService.logAttempt(ipAddress, request.getUsernameOrEmail(), false);
        int remaining = loginAttemptService.getRemainingAttempts(ipAddress, request.getUsernameOrEmail());
        
        return ResponseEntity.badRequest().body(Map.of(
            "success", false,
            "message", e.getMessage(),
            "remainingAttempts", remaining,
            "requiresCaptcha", remaining <= 2
        ));
    }
}

private String getClientIP(HttpServletRequest request) {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
        return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
}
```

### **FRONTEND (Android)**

#### 2.5. Thêm Google reCAPTCHA

Thêm dependency vào `build.gradle.kts`:

```kotlin
implementation("com.google.android.gms:play-services-safetynet:18.0.1")
```

#### 2.6. Cập nhật AuthViewModel

```kotlin
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAdmin: Boolean = false,
    val remainingAttempts: Int? = null,  // NEW
    val requiresCaptcha: Boolean = false  // NEW
)

fun login(usernameOrEmail: String, password: String, captchaToken: String? = null) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        when (val result = repository.login(usernameOrEmail, password, captchaToken)) {
            is AuthResult.Success -> {
                val user = result.data
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = user,
                    successMessage = "Login successful!",
                    remainingAttempts = null,
                    requiresCaptcha = false
                )
            }
            is AuthResult.Error -> {
                // Parse remaining attempts from error message if available
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                    // Backend sẽ trả về requiresCaptcha và remainingAttempts
                )
            }
        }
    }
}
```

---

## 3️⃣ KIỂM TRA ĐỘ MẠNH MẬT KHẨU

### **BACKEND (Spring Boot)**

#### 3.1. Password Validator

Tạo file: `PasswordValidator.java`

```java
@Component
public class PasswordValidator {
    
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        
        // Constructor, getters, setters
    }
    
    public ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least 1 uppercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            errors.add("Password must contain at least 1 number");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            errors.add("Password must contain at least 1 special character");
        }
        
        ValidationResult result = new ValidationResult();
        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        return result;
    }
}
```

#### 3.2. Sử dụng trong Service

```java
@Autowired
private PasswordValidator passwordValidator;

public User register(RegisterRequest request) {
    PasswordValidator.ValidationResult validation = passwordValidator.validate(request.getPassword());
    
    if (!validation.isValid()) {
        throw new RuntimeException("Password requirements not met: " + 
            String.join(", ", validation.getErrors()));
    }
    
    // Tiếp tục đăng ký...
}
```

### **FRONTEND (Android)**

#### 3.3. Password Strength Validator

Tạo file: `PasswordStrengthValidator.kt`

```kotlin
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
}
```

#### 3.4. Cập nhật RegisterScreen

```kotlin
var passwordStrength by remember { mutableStateOf<PasswordStrength?>(null) }
var passwordErrors by remember { mutableStateOf<List<String>>(emptyList()) }

OutlinedTextField(
    value = password,
    onValueChange = {
        password = it
        val validation = PasswordStrengthValidator.validate(it)
        passwordStrength = validation.strength
        passwordErrors = validation.errors
    },
    label = { Text("Password") },
    modifier = Modifier.fillMaxWidth(),
    visualTransformation = PasswordVisualTransformation(),
    isError = passwordErrors.isNotEmpty(),
    supportingText = {
        Column {
            // Hiển thị độ mạnh
            when (passwordStrength) {
                PasswordStrength.WEAK -> Text("Yếu", color = Color.Red)
                PasswordStrength.MEDIUM -> Text("Trung bình", color = Color(0xFFFFA500))
                PasswordStrength.STRONG -> Text("Mạnh", color = Color.Green)
                null -> {}
            }
            
            // Hiển thị lỗi
            passwordErrors.forEach { error ->
                Text("• $error", color = Color.Red, fontSize = 12.sp)
            }
        }
    }
)

// Indicator bar
LinearProgressIndicator(
    progress = when (passwordStrength) {
        PasswordStrength.WEAK -> 0.33f
        PasswordStrength.MEDIUM -> 0.66f
        PasswordStrength.STRONG -> 1f
        null -> 0f
    },
    modifier = Modifier.fillMaxWidth(),
    color = when (passwordStrength) {
        PasswordStrength.WEAK -> Color.Red
        PasswordStrength.MEDIUM -> Color(0xFFFFA500)
        PasswordStrength.STRONG -> Color.Green
        null -> Color.Gray
    }
)
```

---

## TÓM TẮT CÁC BƯỚC TRIỂN KHAI

### BACKEND:
1. ✅ Tạo entities: `PasswordResetPin`, `LoginAttempt`
2. ✅ Tạo repositories tương ứng
3. ✅ Implement services: PIN reset, login attempt tracking, password validation
4. ✅ Cập nhật controllers với endpoints mới
5. ✅ Cấu hình email trong `application.properties`

### FRONTEND:
1. ✅ Thêm API endpoints vào `AuthApiService`
2. ✅ Implement logic trong `AuthRepository` và `AuthViewModel`
3. ✅ Tạo `ForgotPasswordScreen` cho quên mật khẩu
4. ✅ Thêm password strength validator
5. ✅ Cập nhật `RegisterScreen` với password validation UI
6. ✅ Thêm captcha handling trong `LoginScreen`

### DATABASE:
```sql
-- Chạy migrations
CREATE TABLE password_reset_pins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    pin VARCHAR(6) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    username VARCHAR(255) NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    success BOOLEAN NOT NULL,
    INDEX idx_ip_time (ip_address, attempt_time),
    INDEX idx_user_time (username, attempt_time)
);
```
