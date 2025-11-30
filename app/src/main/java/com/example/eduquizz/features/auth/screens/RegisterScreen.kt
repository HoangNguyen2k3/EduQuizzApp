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
import androidx.compose.ui.platform.LocalContext
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
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    dataViewModel: DataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    // Validation states
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

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

    // Navigate on success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            android.util.Log.d("RegisterScreen", "Registration successful, syncing user data")

            // Sync user info to UserViewModel and DataViewModel
            uiState.currentUser?.let { user ->
                android.util.Log.d("RegisterScreen", "New user: ${user.username}")

                userViewModel.setUserName(user.username)
                dataViewModel.updatePlayerName(user.fullName ?: user.username)

                // New user, so first time is false (they need to go to Ready screen)
                dataViewModel.setFirstTime(false)
            }

            // Small delay to ensure state updates
            kotlinx.coroutines.delay(300)

            android.util.Log.d("RegisterScreen", "Calling onRegisterSuccess")
            onRegisterSuccess()
        }
    }

    // Validate username availability
    LaunchedEffect(username) {
        if (username.length >= 3) {
            delay(500) // Debounce
            viewModel.checkUsernameAvailability(username) { isAvailable ->
                usernameError = if (!isAvailable) "Username already taken" else null
            }
        }
    }

    // Validate email availability
    LaunchedEffect(email) {
        if (email.contains("@")) {
            delay(500) // Debounce
            viewModel.checkEmailAvailability(email) { isAvailable ->
                emailError = if (!isAvailable) "Email already registered" else null
            }
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
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null)
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

                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (it.length < 3) {
                                usernameError = "Username must be at least 3 characters"
                            }
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        trailingIcon = {
                            if (username.length >= 3) {
                                if (usernameError != null) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = usernameError != null,
                        supportingText = {
                            if (usernameError != null) {
                                Text(usernameError ?: "", color = Color.Red, fontSize = 12.sp)
                            }
                        },
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

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() && it.isNotEmpty()) {
                                emailError = "Invalid email format"
                            } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                                emailError = null
                            }
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        trailingIcon = {
                            if (email.contains("@")) {
                                if (emailError != null) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = emailError != null,
                        supportingText = {
                            if (emailError != null) {
                                Text(emailError ?: "", color = Color.Red, fontSize = 12.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
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
                        onValueChange = {
                            password = it
                            if (it.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                            } else {
                                passwordError = null
                            }
                            if (confirmPassword.isNotEmpty() && it != confirmPassword) {
                                confirmPasswordError = "Passwords do not match"
                            } else if (confirmPassword.isNotEmpty()) {
                                confirmPasswordError = null
                            }
                        },
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
                        isError = passwordError != null,
                        supportingText = {
                            if (passwordError != null) {
                                Text(passwordError ?: "", color = Color.Red, fontSize = 12.sp)
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (it != password && it.isNotEmpty()) {
                                confirmPasswordError = "Passwords do not match"
                            } else {
                                confirmPasswordError = null
                            }
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Hide password"
                                    else "Show password"
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
                                Text(confirmPasswordError ?: "", color = Color.Red, fontSize = 12.sp)
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
                            text = "I agree to Terms & Conditions",
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEE2E2)
                            )
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Register Button
                    val isFormValid = username.length >= 3 &&
                            email.contains("@") &&
                            password.length >= 6 &&
                            password == confirmPassword &&
                            fullName.isNotBlank() &&
                            agreedToTerms &&
                            usernameError == null &&
                            emailError == null

                    Button(
                        onClick = {
                            if (isFormValid) {
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