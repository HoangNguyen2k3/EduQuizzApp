package com.example.eduquizz.features.home.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.eduquizz.data_save.AudioManager
import com.example.quizapp.ui.theme.QuizAppTheme

@Composable
fun SettingScreen() {
    var isBgmEnabled by remember { mutableStateOf(true) }
    var sfxVolume by remember { mutableFloatStateOf(1f) }
    var selectedLanguage by remember { mutableStateOf("Tiếng Việt") }
    var showLanguageDialog by remember { mutableStateOf(false) }

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
                text = stringResource(id = R.string.settings_title),
                fontSize = dimensionResource(id = R.dimen.text_xl).value.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark),
                modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.spacing_xl))
            )

            // Audio Section
            SettingsSection(
                title = stringResource(id = R.string.audio_title),
                icon = Icons.Default.VolumeUp,
                iconBackgroundGradient = listOf(
                    colorResource(id = R.color.english_red),
                    colorResource(id = R.color.english_coral)
                )
            ) {
                // Background Music Toggle
                SettingToggleItem(
                    icon = R.drawable.musicnote,
                    title = stringResource(id = R.string.background_music),
                    checked = isBgmEnabled,
                    onCheckedChange = {
                        isBgmEnabled = it
                        AudioManager.setBgmEnabled(it)
                    }
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                // SFX Volume
                SettingSliderItem(
                    icon = R.drawable.volumeup,
                    title = stringResource(id = R.string.sound_effects),
                    value = sfxVolume,
                    onValueChange = {
                        sfxVolume = it
                        AudioManager.setSfxVolume(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))

            // Language 
            SettingsSection(
                title = stringResource(id = R.string.language_title),
                icon = Icons.Default.Language,
                iconBackgroundGradient = listOf(
                    colorResource(id = R.color.math_blue),
                    colorResource(id = R.color.math_purple)
                )
            ) {
                SettingClickableItem(
                    icon = R.drawable.language,
                    title = stringResource(id = R.string.current_language),
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xl)))

            // Information Section
            SettingsSection(
                title = stringResource(id = R.string.information_title),
                icon = Icons.Default.Info,
                iconBackgroundGradient = listOf(
                    colorResource(id = R.color.coin_gradient_start),
                    colorResource(id = R.color.coin_gradient_end)
                )
            ) {
                SettingClickableItem(
                    icon = R.drawable.info,
                    title = stringResource(id = R.string.version_title),
                    subtitle = "1.0.0"
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                SettingClickableItem(
                    icon = R.drawable.security,
                    title = stringResource(id = R.string.privacy_policy),
                    onClick = { /* TODO */ },
                    showArrow = true
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_medium)))

                SettingClickableItem(
                    icon = R.drawable.description,
                    title = stringResource(id = R.string.terms_of_use),
                    onClick = { /* TODO */ },
                    showArrow = true
                )
            }
        }

        // Language Selection Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = {
                    Text(
                        stringResource(id = R.string.select_language),
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    TextButton(
                        onClick = { showLanguageDialog = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = colorResource(id = R.color.english_red)
                        )
                    ) {
                        Text(stringResource(id = R.string.close))
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_large))
            )
        }
    }
}

@Composable
private fun SettingsSection(
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
private fun SettingToggleItem(
    icon: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium)),
            tint = colorResource(id = R.color.text_secondary_gray)
        )

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.spacing_large)),
            fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
            color = colorResource(id = R.color.text_primary_dark)
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.english_red),
                uncheckedThumbColor = colorResource(id = R.color.bg_light_gray)
            )
        )
    }
}

@Composable
private fun SettingClickableItem(
    icon: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    showArrow: Boolean = false
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
                color = colorResource(id = R.color.text_primary_dark)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = dimensionResource(id = R.dimen.text_small).value.sp,
                    color = colorResource(id = R.color.text_secondary_gray),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        if (showArrow) {
            Icon(
                painter = painterResource(id = R.drawable.chevronright),
                contentDescription = "More",
                tint = colorResource(id = R.color.text_secondary_gray),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
            )
        }
    }
}

@Composable
private fun LanguageOption(
    language: String,
    selectedLanguage: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.corner_small)))
            .clickable { onSelect(language) }
            .padding(vertical = dimensionResource(id = R.dimen.spacing_medium)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = language == selectedLanguage,
            onClick = { onSelect(language) },
            colors = RadioButtonDefaults.colors(
                selectedColor = colorResource(id = R.color.english_red),
                unselectedColor = colorResource(id = R.color.text_secondary_gray)
            )
        )
        Text(
            text = language,
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.spacing_medium)),
            fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
            color = colorResource(id = R.color.text_primary_dark)
        )
    }
}


@Composable
private fun SettingSliderItem(
    icon: Int,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium)),
            tint = colorResource(id = R.color.text_secondary_gray)
        )

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = dimensionResource(id = R.dimen.spacing_large)),
            fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
            color = colorResource(id = R.color.text_primary_dark)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(120.dp),
            colors = SliderDefaults.colors(
                thumbColor = colorResource(id = R.color.english_red),
                activeTrackColor = colorResource(id = R.color.english_coral),
                inactiveTrackColor = colorResource(id = R.color.bg_light_gray)
            )
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SettingScreenPreview() {
    SettingScreen()
=======
@Preview
@Composable
fun SettingScreenPreview(){
    QuizAppTheme {
        SettingScreen()
    }
}