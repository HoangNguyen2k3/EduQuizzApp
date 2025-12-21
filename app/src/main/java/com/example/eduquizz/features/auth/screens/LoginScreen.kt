package com.example.eduquizz.features.auth.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import com.example.eduquizz.data.local.UserViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.features.auth.components.RecaptchaDialog
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    dataViewModel: DataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showCaptchaDialog by remember { mutableStateOf(false) }

    // Debug: Track state changes
    LaunchedEffect(uiState.remainingAttempts, uiState.requiresCaptcha, uiState.isLoading) {
        android.util.Log.d("LoginScreen", "═══ STATE CHANGED ═══")
        android.util.Log.d("LoginScreen", "remainingAttempts: ${uiState.remainingAttempts}")
        android.util.Log.d("LoginScreen", "requiresCaptcha: ${uiState.requiresCaptcha}")
        android.util.Log.d("LoginScreen", "isLoading: ${uiState.isLoading}")
        android.util.Log.d("LoginScreen", "errorMessage: ${uiState.errorMessage}")
        
        // Calculate button enable state
        val shouldEnable = !uiState.isLoading &&
                          usernameOrEmail.isNotBlank() &&
                          password.isNotBlank() &&
                          (uiState.remainingAttempts == null || uiState.remainingAttempts!! > 0) &&
                          !uiState.requiresCaptcha
        
        android.util.Log.d("LoginScreen", "Button SHOULD be enabled: $shouldEnable")
        android.util.Log.d("LoginScreen", "═══════════════════════")
    }

    // Google Sign-In Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "=== Google Sign-In Result ===")
        Log.d("LoginScreen", "Result code: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("LoginScreen", "✅ Got Google account: ${account.email}")
                viewModel.signInWithGoogle(account)
            } catch (e: ApiException) {
                Log.e("LoginScreen", "❌ Google Sign-In failed: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.w("LoginScreen", "⚠️ Google Sign-In cancelled or failed")
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.isLoggedIn) {
        Log.d("LoginScreen", "=== State Changed ===")
        Log.d("LoginScreen", "isLoggedIn: ${uiState.isLoggedIn}")
        Log.d("LoginScreen", "currentUser: ${uiState.currentUser?.username}")

        if (uiState.isLoggedIn) {
            Log.d("LoginScreen", "Login successful, syncing user data")

            // Sync user info to UserViewModel and DataViewModel
            uiState.currentUser?.let { user ->
                Log.d("LoginScreen", "User: ${user.username}, ${user.email}")

                userViewModel.setUserName(user.username)
                dataViewModel.updatePlayerName(user.fullName ?: user.username)

                // Mark as not first time if user has logged in before
                if (user.lastLogin != null) {
                    dataViewModel.updateFirstTime()
                }
            }

            // Small delay to ensure state updates
            kotlinx.coroutines.delay(300)

            Log.d("LoginScreen", "Calling onLoginSuccess")
            onLoginSuccess()
        }
    }

    // Show Captcha Dialog when required
    if (showCaptchaDialog) {
        Log.d("LoginScreen", "🔐 Showing reCAPTCHA dialog...")
        RecaptchaDialog(
            onDismiss = { 
                Log.d("LoginScreen", "❌ reCAPTCHA dialog dismissed by user")
                showCaptchaDialog = false 
            },
            onTokenReceived = { token ->
                Log.d("LoginScreen", "✅ reCAPTCHA token received: ${token.take(30)}...")
                Log.d("LoginScreen", "🔄 Attempting login with captcha token...")
                showCaptchaDialog = false
                viewModel.login(usernameOrEmail, password, token)
            }
        )
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
            Spacer(modifier = Modifier.height(60.dp))

            // Logo and Title
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EduQuizz",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Welcome back!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Card
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
                    // Username/Email Field
                    OutlinedTextField(
                        value = usernameOrEmail,
                        onValueChange = { usernameOrEmail = it },
                        label = { Text("Username or Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedLabelColor = Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password"
                                    else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Check brute-force protection before allowing login
                                val canLogin = usernameOrEmail.isNotBlank() && 
                                              password.isNotBlank() &&
                                              (uiState.remainingAttempts == null || uiState.remainingAttempts!! > 0) &&
                                              !uiState.requiresCaptcha &&
                                              !uiState.isLoading
                                
                                if (canLogin) {
                                    android.util.Log.d("LoginScreen", "Keyboard Done - Login triggered")
                                    viewModel.login(usernameOrEmail, password)
                                } else if (uiState.requiresCaptcha && uiState.remainingAttempts != 0) {
                                    android.util.Log.d("LoginScreen", "Keyboard Done - Opening reCAPTCHA dialog...")
                                    showCaptchaDialog = true
                                } else {
                                    android.util.Log.w("LoginScreen", "Keyboard Done - Login blocked! remainingAttempts: ${uiState.remainingAttempts}, requiresCaptcha: ${uiState.requiresCaptcha}")
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            focusedLabelColor = Color(0xFF6366F1)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it }
                            )
                            Text("Remember me", fontSize = 14.sp)
                        }

                        Text(
                            text = "Forgot Password?",
                            fontSize = 14.sp,
                            color = Color(0xFF6366F1),
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Message with Remaining Attempts
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
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
                                        text = uiState.errorMessage ?: "",
                                        color = Color(0xFFDC2626),
                                        fontSize = 14.sp
                                    )
                                }
                                
                                // Show remaining attempts if available
                                if (uiState.remainingAttempts != null && uiState.remainingAttempts!! > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚠️ Còn lại ${uiState.remainingAttempts} lần thử",
                                        color = if (uiState.remainingAttempts!! <= 2) Color(0xFFDC2626) else Color(0xFFF59E0B),
                                        fontSize = 12.sp,
                                        fontWeight = if (uiState.remainingAttempts!! <= 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                    // Show captcha warning if required
                                    if (uiState.requiresCaptcha) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🔐 Bạn cần xác thực reCAPTCHA để tiếp tục",
                                            color = Color(0xFFDC2626),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (uiState.remainingAttempts == 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "🔒 Tài khoản bị khóa. Vui lòng thử lại sau 30 phút.",
                                        color = Color(0xFFDC2626),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Login Button
                    Button(
                        onClick = {
                            android.util.Log.d("LoginScreen", "▶ Button CLICKED")
                            android.util.Log.d("LoginScreen", "  - remainingAttempts: ${uiState.remainingAttempts}")
                            android.util.Log.d("LoginScreen", "  - requiresCaptcha: ${uiState.requiresCaptcha}")
                            
                            if (usernameOrEmail.isNotBlank() && password.isNotBlank()) {
                                // Nếu cần captcha, hiển thị dialog
                                if (uiState.requiresCaptcha && uiState.remainingAttempts != 0) {
                                    android.util.Log.d("LoginScreen", "Opening reCAPTCHA dialog...")
                                    showCaptchaDialog = true
                                } else {
                                    // Login thông thường
                                    viewModel.login(usernameOrEmail, password)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        ),
                        enabled = !uiState.isLoading &&
                                usernameOrEmail.isNotBlank() &&
                                password.isNotBlank() &&
                                (uiState.remainingAttempts == null || uiState.remainingAttempts!! > 0) &&  // Enable if null or > 0
                                !uiState.requiresCaptcha  // Disable if captcha required
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Login",
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
                            Log.d("LoginScreen", "=== Google Sign-In Button Clicked ===")
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

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Sign Up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}