package com.example.eduquizz.features.home.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R

@Composable
fun SettingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colorResource(id = R.color.bg_coral).copy(alpha = 0.1f),
                        colorResource(id = R.color.accent_pink).copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(id = R.string.statistics_icon_desc),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_xl)),
                tint = colorResource(id = R.color.bg_coral)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))
            Text(
                text = stringResource(id = R.string.statistics_title),
                fontSize = dimensionResource(id = R.dimen.text_xl).value.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark)
            )
            Text(
                text = stringResource(id = R.string.statistics_subtitle),
                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                color = colorResource(id = R.color.text_secondary_gray),
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_medium))
            )
        }
    }
}