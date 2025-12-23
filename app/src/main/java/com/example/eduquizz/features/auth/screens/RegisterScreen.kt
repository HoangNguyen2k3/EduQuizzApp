package com.example.eduquizz.features.auth.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.R
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
private fun PasswordRequirement(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isMet) Color(0xFF16A34A) else Color(0xFF6B7280)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    // Chỉ validation format cơ bản, không kiểm tra availability
    var emailFormatError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    
    // Parse backend error để hiển thị đúng field
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            val error = uiState.errorMessage!!.lowercase()
            when {
                error.contains("username") && error.contains("exist") -> {
                    usernameError = "Username đã tồn tại"
                }
                error.contains("email") && error.contains("exist") -> {
                    emailFormatError = "Email đã được sử dụng"
                }
                error.contains("password") -> {
                    passwordError = uiState.errorMessage
                }
            }
        }
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.signInWithGoogle(account)
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    // Navigate to login on success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            android.util.Log.d("RegisterScreen", "Registration successful, navigating to login")
            kotlinx.coroutines.delay(300)
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6),
                        Color(0xFFA855F7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo and Title
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Logo",
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Sign up to get started!",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full Name Field
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { 
                            fullName = it
                            // Validate full name
                            fullNameError = when {
                                it.isBlank() -> "Họ tên không được để trống"
                                it.length < 2 -> "Họ tên phải có ít nhất 2 ký tự"
                                else -> null
                            }
                        },
                        label = { Text("Họ và tên") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Badge, 
                                contentDescription = null,
                                tint = if (fullNameError != null) Color(0xFFDC2626) else Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (fullNameError != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = fullNameError != null,
                        supportingText = {
                            if (fullNameError != null) {
                                Text(
                                    text = fullNameError ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (fullNameError != null) Color(0xFFDC2626) else Color(0xFF6366F1),
                            unfocusedBorderColor = if (fullNameError != null) Color(0xFFDC2626) else Color.Gray,
                            focusedLabelColor = if (fullNameError != null) Color(0xFFDC2626) else Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username Field - không validate availability
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            username = it
                            // Clear backend error khi user sửa
                            usernameError = when {
                                it.isBlank() -> "Username không được để trống"
                                it.length < 3 -> "Username phải có ít nhất 3 ký tự"
                                !it.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username chỉ chứa chữ, số và dấu gạch dưới"
                                else -> null
                            }
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null,
                                tint = if (usernameError != null) Color(0xFFDC2626) else Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (usernameError != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = usernameError != null,
                        supportingText = {
                            if (usernameError != null) {
                                Text(
                                    text = usernameError ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (usernameError != null) Color(0xFFDC2626) else Color(0xFF6366F1),
                            unfocusedBorderColor = if (usernameError != null) Color(0xFFDC2626) else Color.Gray,
                            focusedLabelColor = if (usernameError != null) Color(0xFFDC2626) else Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Field - chỉ validate format
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            // Chỉ kiểm tra format email
                            emailFormatError = when {
                                it.isBlank() -> "Email không được để trống"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() -> "Email không hợp lệ"
                                else -> null
                            }
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, 
                                contentDescription = null,
                                tint = if (emailFormatError != null) Color(0xFFDC2626) else Color.Gray
                            )
                        },
                        trailingIcon = {
                            if (emailFormatError != null) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = emailFormatError != null,
                        supportingText = {
                            if (emailFormatError != null) {
                                Text(
                                    text = emailFormatError ?: "", 
                                    color = Color(0xFFDC2626), 
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (emailFormatError != null) Color(0xFFDC2626) else Color(0xFF6366F1),
                            unfocusedBorderColor = if (emailFormatError != null) Color(0xFFDC2626) else Color.Gray,
                            focusedLabelColor = if (emailFormatError != null) Color(0xFFDC2626) else Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            // Không set error ở đây, để hiển thị checklist
                            // Kiểm tra khớp với confirm password
                            if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                                confirmPasswordError = "Mật khẩu không khớp"
                            } else if (confirmPassword.isNotEmpty()) {
                                confirmPasswordError = null
                            }
                        },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ẩn mật khẩu"
                                    else "Hiện mật khẩu"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedLabelColor = Color(0xFF6366F1)
                        )
                    )
                    
                    // Password requirements checklist
                    if (password.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp)
                        ) {
                            PasswordRequirement(
                                text = "Ít nhất 8 ký tự",
                                isMet = password.length >= 8
                            )
                            PasswordRequirement(
                                text = "Có ít nhất 1 chữ hoa",
                                isMet = password.any { it.isUpperCase() }
                            )
                            PasswordRequirement(
                                text = "Có ít nhất 1 chữ số",
                                isMet = password.any { it.isDigit() }
                            )
                            PasswordRequirement(
                                text = "Có ít nhất 1 ký tự đặc biệt (!@#\$%^&*...)",
                                isMet = password.any { "!@#\$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            // Kiểm tra khớp với password
                            confirmPasswordError = when {
                                it.isBlank() -> "Vui lòng xác nhận mật khẩu"
                                it != password -> "Mật khẩu không khớp"
                                else -> null
                            }
                        },
                        label = { Text("Xác nhận mật khẩu") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, 
                                contentDescription = null,
                                tint = if (confirmPasswordError != null) Color(0xFFDC2626) else Color.Gray
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (confirmPasswordError != null) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                } else if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Match",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu"
                                        else "Hiện mật khẩu"
                                    )
                                }
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = confirmPasswordError != null,
                        supportingText = {
                            if (confirmPasswordError != null) {
                                Text(
                                    text = confirmPasswordError ?: "", 
                                    color = Color(0xFFDC2626), 
                                    fontSize = 12.sp
                                )
                            } else if (confirmPassword.isNotEmpty() && confirmPassword == password) {
                                Text(
                                    text = "✓ Mật khẩu khớp", 
                                    color = Color(0xFF16A34A), 
                                    fontSize = 12.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (confirmPasswordError != null) Color(0xFFDC2626) 
                                                 else if (confirmPassword.isNotEmpty() && confirmPassword == password) Color(0xFF16A34A)
                                                 else Color(0xFF6366F1),
                            unfocusedBorderColor = if (confirmPasswordError != null) Color(0xFFDC2626) 
                                                   else if (confirmPassword.isNotEmpty() && confirmPassword == password) Color(0xFF16A34A)
                                                   else Color.Gray,
                            focusedLabelColor = if (confirmPasswordError != null) Color(0xFFDC2626) 
                                                else if (confirmPassword.isNotEmpty() && confirmPassword == password) Color(0xFF16A34A)
                                                else Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms and Conditions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it }
                        )
                        Text(
                            text = "Tôi đồng ý với Điều khoản & Điều kiện",
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Success Message
                    if (uiState.successMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFDCFCE7)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.successMessage ?: "",
                                    color = Color(0xFF16A34A),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Tổng hợp tất cả lỗi validation - MOVED HERE (phía dưới form)
                    val allErrors = mutableListOf<String>()
                    if (fullNameError != null) allErrors.add(fullNameError!!)
                    if (usernameError != null) allErrors.add(usernameError!!)
                    if (emailFormatError != null) allErrors.add(emailFormatError!!)
                    
                    // Password validation errors
                    if (password.isNotEmpty()) {
                        if (password.length < 8) allErrors.add("Mật khẩu phải có ít nhất 8 ký tự")
                        if (!password.any { it.isUpperCase() }) allErrors.add("Mật khẩu phải có ít nhất 1 chữ hoa")
                        if (!password.any { it.isDigit() }) allErrors.add("Mật khẩu phải có ít nhất 1 chữ số")
                        if (!password.any { "!@#\$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
                            allErrors.add("Mật khẩu phải có ít nhất 1 ký tự đặc biệt")
                        }
                    }
                    
                    if (confirmPasswordError != null) allErrors.add(confirmPasswordError!!)
                    if (!agreedToTerms && (fullName.isNotEmpty() || username.isNotEmpty())) {
                        allErrors.add("Vui lòng đồng ý với Điều khoản & Điều kiện")
                    }
                    
                    // Hiển thị tất cả lỗi validation
                    if (allErrors.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF2F2)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Vui lòng sửa các lỗi sau:",
                                        color = Color(0xFFDC2626),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                allErrors.forEach { error ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "•",
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = error,
                                            color = Color(0xFF991B1B),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Backend error (không parse được)
                    if (uiState.errorMessage != null && allErrors.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Register Button
                    // Validation đầy đủ - yêu cầu 8 ký tự
                    val isPasswordValid = password.length >= 8 &&
                            password.any { it.isUpperCase() } &&
                            password.any { it.isDigit() } &&
                            password.any { "!@#\$%^&*()_+-=[]{}|;:,.<>?".contains(it) }
                    
                    val isFormValid = fullName.isNotBlank() &&
                            fullNameError == null &&
                            username.length >= 3 &&
                            usernameError == null &&
                            email.contains("@") &&
                            emailFormatError == null &&
                            isPasswordValid &&
                            password == confirmPassword &&
                            confirmPasswordError == null &&
                            agreedToTerms

                    Button(
                        onClick = {
                            if (isFormValid) {
                                // Clear errors trước khi submit
                                fullNameError = null
                                usernameError = null
                                emailFormatError = null
                                confirmPasswordError = null
                                
                                // Gọi register - backend sẽ kiểm tra username/email đã tồn tại
                                viewModel.register(username, email, password, fullName)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        enabled = !uiState.isLoading && isFormValid
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  OR  ",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Divider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            val signInIntent = viewModel.getGoogleSignInClient().signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Google",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Login",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}