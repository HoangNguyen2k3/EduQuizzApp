package com.example.eduquizz.features.home.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eduquizz.R
import com.example.eduquizz.features.auth.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogoutSuccess: () -> Unit = {}
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showFullNameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Use user data from AuthViewModel
    val username = currentUser?.username ?: "User123"
    val fullName = currentUser?.fullName ?: "Full Name"
    val email = currentUser?.email ?: "email@example.com"
    val phoneNumber = currentUser?.phoneNumber ?: "Not set"
    val profileImageUrl = currentUser?.profileImageUrl
    val role = currentUser?.role ?: "USER"
    val createdAt = currentUser?.createdAt ?: ""

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
        // TODO: Upload image to server and update profile
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.bg_very_light_gray),
                        colorResource(id = R.color.bg_light_gray),
                        colorResource(id = R.color.bg_darker_gray)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.spacing_xl))
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = stringResource(id = R.string.profile_title),
                fontSize = dimensionResource(id = R.dimen.text_xl).value.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark),
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_xl))
            )

            // Avatar Section
            ProfileSection(
                title = "Ảnh đại diện",
                icon = Icons.Default.Person,
                iconBackgroundGradient = listOf(
                    colorResource(id = R.color.math_light_purple),
                    colorResource(id = R.color.secondary_blue)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colorResource(id = R.color.math_light_purple).copy(alpha = 0.3f),
                                        colorResource(id = R.color.secondary_blue).copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .border(
                                width = 3.dp,
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colorResource(id = R.color.math_light_purple),
                                        colorResource(id = R.color.secondary_blue)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (!profileImageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Avatar",
                                modifier = Modifier.size(60.dp),
                                tint = colorResource(id = R.color.math_light_purple)
                            )
                        }

                        // Camera icon overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .background(
                                    color = colorResource(id = R.color.math_light_purple),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Chạm để thay đổi ảnh đại diện",
                    fontSize = dimensionResource(id = R.dimen.text_small).value.sp,
                    color = colorResource(id = R.color.text_secondary_gray),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = dimensionResource(id = R.dimen.spacing_medium))
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))

            // Personal Information Section
            ProfileSection(
                title = "Thông tin cá nhân",
                icon = Icons.Default.Edit,
                iconBackgroundGradient = listOf(
                    colorResource(id = R.color.english_red),
                    colorResource(id = R.color.english_coral)
                )
            ) {
                ProfileEditableItem(
                    icon = R.drawable.person,
                    title = "Tên đăng nhập",
                    value = username,
                    onClick = { /* Username usually cannot be changed */ },
                    isEditable = false
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                ProfileEditableItem(
                    icon = R.drawable.name,
                    title = "Họ và tên",
                    value = fullName,
                    onClick = { showFullNameDialog = true }
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                ProfileEditableItem(
                    icon = R.drawable.calendar,
                    title = "Email",
                    value = email,
                    onClick = { /* Email usually cannot be changed */ },
                    isEditable = false
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                ProfileEditableItem(
                    icon = R.drawable.calendar,
                    title = "Số điện thoại",
                    value = phoneNumber,
                    onClick = { showPhoneDialog = true }
                )

                if (role == "ADMIN") {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFFFF6F00),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Admin Account",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6F00)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))

            // Account Info Section
            if (createdAt.isNotEmpty()) {
                ProfileSection(
                    title = "Thông tin tài khoản",
                    icon = Icons.Default.Info,
                    iconBackgroundGradient = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF45A049)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Ngày tạo",
                                fontSize = 14.sp,
                                color = colorResource(id = R.color.text_secondary_gray)
                            )
                            Text(
                                text = formatDate(createdAt),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorResource(id = R.color.text_primary_dark)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))
            }

            // Logout Section
            ProfileSection(
                title = "Tài khoản",
                icon = Icons.Default.AccountCircle,
                iconBackgroundGradient = listOf(
                    Color(0xFFFF5252),
                    Color(0xFFFF1744)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium)))
                        .clickable { showLogoutDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.spacing_large)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Đăng xuất",
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium)),
                                tint = Color(0xFFFF1744)
                            )

                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_large)))

                            Text(
                                text = "Đăng xuất",
                                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                                color = Color(0xFFFF1744),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))
        }

        // Full Name Edit Dialog
        if (showFullNameDialog) {
            EditTextDialog(
                title = "Chỉnh sửa họ và tên",
                currentValue = fullName,
                onDismiss = { showFullNameDialog = false },
                placeholder = "Nhập họ và tên",
                onSave = { newValue ->
                    authViewModel.updateFullName(newValue)
                }
            )
        }

        // Phone Edit Dialog
        if (showPhoneDialog) {
            EditTextDialog(
                title = "Chỉnh sửa số điện thoại",
                currentValue = if (phoneNumber == "Not set") "" else phoneNumber,
                onDismiss = { showPhoneDialog = false },
                placeholder = "Nhập số điện thoại",
                keyboardType = KeyboardType.Phone,
                onSave = { newValue ->
                    authViewModel.updatePhoneNumber(newValue)
                }
            )
        }

        // Show success/error messages
        if (uiState.successMessage != null) {
            LaunchedEffect(uiState.successMessage) {
                kotlinx.coroutines.delay(2000)
                authViewModel.clearMessages()
            }

            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFF4CAF50)
            ) {
                Text(uiState.successMessage ?: "")
            }
        }

        if (uiState.errorMessage != null) {
            LaunchedEffect(uiState.errorMessage) {
                kotlinx.coroutines.delay(2000)
                authViewModel.clearMessages()
            }

            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFFF5252)
            ) {
                Text(uiState.errorMessage ?: "")
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = Color(0xFFFF1744),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Xác nhận đăng xuất",
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.text_primary_dark),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?",
                        textAlign = TextAlign.Center,
                        color = colorResource(id = R.color.text_secondary_gray)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            authViewModel.signOut()
                            onLogoutSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF1744)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Đăng xuất",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(id = R.color.text_secondary_gray)
                        )
                    ) {
                        Text("Hủy")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_large))
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackgroundGradient: List<Color>,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.subject_card_corner)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.subject_card_elevation)
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_xxl))
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_large))
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_large))
                        .background(
                            Brush.horizontalGradient(iconBackgroundGradient),
                            RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium))
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_large)))

                Text(
                    text = title,
                    fontSize = dimensionResource(id = R.dimen.text_large).value.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.text_primary_dark)
                )
            }

            content()
        }
    }
}

@Composable
private fun ProfileEditableItem(
    icon: Int,
    title: String,
    value: String,
    onClick: () -> Unit,
    isEditable: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium)))
            .clickable(enabled = isEditable, onClick = onClick)
            .padding(vertical = dimensionResource(id = R.dimen.spacing_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium)),
            tint = colorResource(id = R.color.text_secondary_gray)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.spacing_large))
        ) {
            Text(
                text = title,
                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                color = colorResource(id = R.color.text_secondary_gray)
            )
            Text(
                text = value,
                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                color = colorResource(id = R.color.text_primary_dark),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (isEditable) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = colorResource(id = R.color.english_red),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
            )
        }
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onSave: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark)
            )
        },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.english_red),
                    focusedLabelColor = colorResource(id = R.color.english_red),
                    cursorColor = colorResource(id = R.color.english_red)
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(textValue)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.english_red)
                )
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.text_secondary_gray)
                )
            ) {
                Text("Hủy")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_large))
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}