package com.example.quizapp.screen

import androidx.compose.foundation.clickable // Thêm import này
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.res.painterResource
import com.example.eduquizz.R

@OptIn(ExperimentalMaterial3Api::class) // Thêm annotation này để sử dụng API Material 3
@Composable
fun SettingsScreen(navController: NavController) {
    var bgmVolume by remember { mutableStateOf(0.7f) }
    var sfxVolume by remember { mutableStateOf(1f) }
    var selectedLanguage by remember { mutableStateOf("Tiếng Việt") }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar( // Sử dụng TopAppBar từ material3
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Sử dụng TopAppBarDefaults từ material3
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Âm thanh
            Text(
                text = "Âm thanh",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Nhạc nền
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.musicnote),
                    contentDescription = "BGM",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Nhạc nền",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                Slider(
                    value = bgmVolume,
                    onValueChange = { bgmVolume = it },
                    modifier = Modifier.width(150.dp)
                )
            }

            // Hiệu ứng âm thanh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.volumeup), // Thay đổi thành Icons.Filled.VolumeUp
                    contentDescription = "SFX",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Hiệu ứng âm thanh",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                Slider(
                    value = sfxVolume,
                    onValueChange = { sfxVolume = it },
                    modifier = Modifier.width(150.dp)
                )
            }

            HorizontalDivider()

            // Ngôn ngữ
            Text(
                text = "Ngôn ngữ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { showLanguageDialog = true }, // Thêm clickable ở đây nếu muốn cả hàng có thể click
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.language), // Thay đổi thành Icons.Filled.Language
                    contentDescription = "Language",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Ngôn ngữ hiện tại",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                TextButton(onClick = { showLanguageDialog = true }) {
                    Text(selectedLanguage)
                }
            }

            if (showLanguageDialog) {
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    title = { Text("Chọn ngôn ngữ") },
                    text = {
                        Column {
                            LanguageOption("Tiếng Việt", selectedLanguage) {
                                selectedLanguage = it
                                showLanguageDialog = false
                            }
                            LanguageOption("English", selectedLanguage) {
                                selectedLanguage = it
                                showLanguageDialog = false
                            }
                            LanguageOption("日本語", selectedLanguage) {
                                selectedLanguage = it
                                showLanguageDialog = false
                            }
                            LanguageOption("한국어", selectedLanguage) {
                                selectedLanguage = it
                                showLanguageDialog = false
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showLanguageDialog = false }) {
                            Text("Đóng")
                        }
                    }
                )
            }

            HorizontalDivider()

            // Thông tin khác
            Text(
                text = "Thông tin",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            SettingItem(
                icon = R.drawable.info, // Icons.Default.Info vẫn ổn
                title = "Phiên bản",
                subtitle = "1.0.0"
            )

            SettingItem(
                icon = R.drawable.security, // Thay đổi thành Icons.Filled.Security
                title = "Chính sách bảo mật",
                onClick = { /* TODO */ }
            )

            SettingItem(
                icon = R.drawable.description, // Thay đổi thành Icons.Filled.Description
                title = "Điều khoản sử dụng",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun LanguageOption(
    language: String,
    selectedLanguage: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect(language) }, // clickable đã được import
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = language == selectedLanguage,
            onClick = { onSelect(language) }
        )
        Text(
            text = language,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun SettingItem(
    icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // clickable đã được import
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(text = title)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        if (onClick != {}) { // Kiểm tra xem có onClick handler không để hiển thị mũi tên
            Icon(
                painter = painterResource(id = R.drawable.chevronright),
                contentDescription = "More",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(navController = rememberNavController())
}