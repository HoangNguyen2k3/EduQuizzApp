package com.example.eduquizz.features.mapping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GeographyStatisticsRow(
    countryCount: Int,
    continentCount: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticCard(
            icon = "🏛️",
            value = countryCount.toString(),
            label = "Countries",
            modifier = Modifier.weight(1f)
        )

        StatisticCard(
            icon = "🌍",
            value = continentCount.toString(),
            label = "Continents",
            modifier = Modifier.weight(1f)
        )

        StatisticCard(
            icon = "❓",
            value = "${totalQuestions}+",
            label = "Questions",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticCard(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeographyStatisticsRowPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFE8F5E8))
                .padding(16.dp)
        ) {
            GeographyStatisticsRow(
                countryCount = 195,
                continentCount = 7,
                totalQuestions = 999
            )
        }
    }
}