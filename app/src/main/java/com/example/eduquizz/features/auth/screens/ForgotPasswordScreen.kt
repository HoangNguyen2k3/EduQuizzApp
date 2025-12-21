package com.example.eduquizz.features.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.auth.utils.PasswordStrengthValidator
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(1) } // 1: email, 2: pin & password
    
    // Password validation
    var passwordValidation by remember { mutableStateOf<PasswordStrengthValidator.ValidationResult?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Navigate back on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("successfully", ignoreCase = true) == true && step == 2) {
            kotlinx.coroutines.delay(1500)
            viewModel.clearMessages()
            onResetSuccess()
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

            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = if (step == 1) "Quên mật khẩu?" else "Đặt lại mật khẩu",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = if (step == 1) 
                    "Nhập email để nhận mã PIN" 
                else 
                    "Nhập mã PIN và mật khẩu mới",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    if (step == 1) {
                        // Step 1: Email input
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (email.isNotBlank()) {
                                    viewModel.forgotPassword(email)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isLoading && email.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Gửi mã PIN", fontSize = 16.sp)
                            }
                        }

                        // Success message to move to step 2
                        if (uiState.successMessage != null && !uiState.successMessage!!.contains("successfully")) {
                            LaunchedEffect(uiState.successMessage) {
                                kotlinx.coroutines.delay(1000)
                                step = 2
                                viewModel.clearMessages()
                            }
                        }

                    } else {
                        // Step 2: PIN and new password
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { 
                                if (it.length <= 6) pin = it 
                            },
                            label = { Text("Mã PIN (6 số)") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                passwordValidation = PasswordStrengthValidator.validate(it)
                                if (confirmPassword.isNotEmpty()) {
                                    confirmPasswordError = if (it != confirmPassword) 
                                        "Mật khẩu không khớp" else null
                                }
                            },
                            label = { Text("Mật khẩu mới") },
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
                            isError = passwordValidation?.isValid == false,
                            supportingText = {
                                Column {
                                    // Password strength indicator
                                    when (passwordValidation?.strength) {
                                        PasswordStrengthValidator.PasswordStrength.WEAK -> 
                                            Text("Yếu", color = Color.Red)
                                        PasswordStrengthValidator.PasswordStrength.MEDIUM -> 
                                            Text("Trung bình", color = Color(0xFFFFA500))
                                        PasswordStrengthValidator.PasswordStrength.STRONG -> 
                                            Text("Mạnh", color = Color.Green)
                                        null -> {}
                                    }
                                    
                                    // Validation errors
                                    passwordValidation?.errors?.forEach { error ->
                                        Text("• $error", color = Color.Red, fontSize = 12.sp)
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1)
                            )
                        )

                        // Password strength bar
                        if (newPassword.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = when (passwordValidation?.strength) {
                                    PasswordStrengthValidator.PasswordStrength.WEAK -> 0.33f
                                    PasswordStrengthValidator.PasswordStrength.MEDIUM -> 0.66f
                                    PasswordStrengthValidator.PasswordStrength.STRONG -> 1f
                                    null -> 0f
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = when (passwordValidation?.strength) {
                                    PasswordStrengthValidator.PasswordStrength.WEAK -> Color.Red
                                    PasswordStrengthValidator.PasswordStrength.MEDIUM -> Color(0xFFFFA500)
                                    PasswordStrengthValidator.PasswordStrength.STRONG -> Color.Green
                                    null -> Color.Gray
                                },
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                confirmPasswordError = if (it.isNotEmpty() && it != newPassword) 
                                    "Mật khẩu không khớp" else null
                            },
                            label = { Text("Xác nhận mật khẩu") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Ẩn mật khẩu"
                                        else "Hiện mật khẩu"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = confirmPasswordError != null,
                            supportingText = {
                                if (confirmPasswordError != null) {
                                    Text(confirmPasswordError!!, color = Color.Red, fontSize = 12.sp)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6366F1),
                                focusedLabelColor = Color(0xFF6366F1)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (pin.length == 6 && 
                                    newPassword == confirmPassword && 
                                    passwordValidation?.isValid == true) {
                                    viewModel.verifyPinAndResetPassword(email, pin, newPassword)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !uiState.isLoading && 
                                     pin.length == 6 && 
                                     newPassword.isNotBlank() && 
                                     newPassword == confirmPassword &&
                                     passwordValidation?.isValid == true,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text("Đặt lại mật khẩu", fontSize = 16.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Back to step 1
                        TextButton(
                            onClick = { 
                                step = 1
                                pin = ""
                                newPassword = ""
                                confirmPassword = ""
                                viewModel.clearMessages()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gửi lại mã PIN", color = Color(0xFF6366F1))
                        }
                    }

                    // Error/Success messages
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    uiState.errorMessage!!,
                                    color = Color(0xFFD32F2F),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (uiState.successMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF388E3C)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    uiState.successMessage!!,
                                    color = Color(0xFF388E3C),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
