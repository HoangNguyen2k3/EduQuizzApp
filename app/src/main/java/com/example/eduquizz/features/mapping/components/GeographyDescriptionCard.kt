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
fun GeographyDescriptionCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "How to Play",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            GameFeature(
                icon = "🗺️",
                title = "Interactive Maps",
                description = "Explore world maps and learn country locations"
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameFeature(
                icon = "🏴",
                title = "Country Flags",
                description = "Test your knowledge of flags from around the world"
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameFeature(
                icon = "🏙️",
                title = "Capitals Quiz",
                description = "Match countries with their capital cities"
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameFeature(
                icon = "🏔️",
                title = "Geography Facts",
                description = "Learn about landmarks, rivers, mountains and more"
            )
        }
    }
}

@Composable
private fun GameFeature(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 16.dp, top = 2.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp
                ),
                color = Color(0xFF4CAF50),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GeographyDescriptionCardPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFE8F5E8))
                .padding(16.dp)
        ) {
            GeographyDescriptionCard()
        }
    }
}