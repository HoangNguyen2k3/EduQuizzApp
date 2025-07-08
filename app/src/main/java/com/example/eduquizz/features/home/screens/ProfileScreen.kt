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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eduquizz.R
import com.example.eduquizz.data_save.DataViewModel
import com.example.quizapp.ui.theme.QuizAppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    dataviewModel: DataViewModel = hiltViewModel(),
) {
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
  //  var username by remember { mutableStateOf("User123") }
   // var fullName by remember { mutableStateOf("Nguyễn Văn A") }
  //  var birthDate by remember { mutableStateOf("01/01/2000") }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showFullNameDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val  fullName by dataviewModel.playerHobbiesSubject.observeAsState("English")
    val username by dataviewModel.playerName.observeAsState("User123")
    val birthDate by dataviewModel.birthDay.observeAsState("01/01/2000")
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
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
                    onClick = { showUsernameDialog = true }
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
                    title = "Ngày sinh",
                    value = birthDate,
                    onClick = { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))
        }

        // Username Edit Dialog
        if (showUsernameDialog) {
            EditTextDialog(
                title = "Chỉnh sửa tên",
                currentValue = username,
                onValueChange = {  },
                onDismiss = { showUsernameDialog = false },
                placeholder = "Nhập tên",
                onSave = { dataviewModel.updatePlayerName(it) }
            )
        }

        // Full Name Edit Dialog
        if (showFullNameDialog) {
            EditTextDialog(
                title = "Chỉnh sửa môn học yêu thích",
                currentValue = fullName,
                onValueChange = {  },
                onDismiss = { showFullNameDialog = false },
                placeholder = "Nhập họ và tên",
                onSave = { dataviewModel.updatePlayerHobbiesSubject(it) }
            )
        }

        // Date Picker Dialog
//        if (showDatePicker) {
//            DatePickerDialog(
//                currentDate = birthDate,
//                onDateSelected = { birthDate = it },
//                onDismiss = { showDatePicker = false }
//            )
//        }
        if (showDatePicker) {
            DatePickerDialog(
                currentDate = birthDate,
                onDateSelected = {
                //    birthDate = it
                    dataviewModel.editBirthday(it)

/*                    val year = it.split("/").lastOrNull()?.toIntOrNull()
                    if (year != null) {
                        val age = Calendar.getInstance().get(Calendar.YEAR) - year
                        dataviewModel.updatePlayerAge(age)
                    }*/
                },
                onDismiss = { showDatePicker = false }
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium)))
            .clickable(onClick = onClick)
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

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = colorResource(id = R.color.english_red),
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
        )
    }
}

@Composable
private fun ProfileStatItem(
    icon: Int,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium))
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
        }

        Text(
            text = value,
            fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
            color = colorResource(id = R.color.text_primary_dark),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    placeholder: String,
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
                    onValueChange(textValue)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onDateSelected(formatter.format(Date(millis)))
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(id = R.color.english_red)
                )
            ) {
                Text("Chọn")
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
        colors = DatePickerDefaults.colors(
            containerColor = Color.White
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = colorResource(id = R.color.english_red),
                todayDateBorderColor = colorResource(id = R.color.english_red),
                todayContentColor = colorResource(id = R.color.english_red)
            )
        )
    }
}
@Preview
@Composable
fun ProfileScreenPreview(){
    QuizAppTheme {
        ProfileScreen()
    }
}