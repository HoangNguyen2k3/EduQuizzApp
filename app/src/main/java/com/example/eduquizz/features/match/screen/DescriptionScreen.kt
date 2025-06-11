package com.example.quizapp.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.tooling.preview.Preview
import com.example.eduquizz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDescriptionScreen(
    onPlayClick: () -> Unit,
    subject: String = "English" // Default value for preview
) {
    val (title, image, description) = when (subject.lowercase()) {
        "english" -> Triple(
            "English Vocabulary",
            R.drawable.english,
            "Improve your English vocabulary through an engaging word matching game. Connect words with their correct definitions to enhance your language skills."
        )
        "math" -> Triple(
            "Mathematics",
            R.drawable.math,
            "Test your mathematical knowledge by matching equations with their solutions. Perfect for practicing basic to advanced math concepts."
        )
        "physics" -> Triple(
            "Physics",
            R.drawable.physics,
            "Explore physics concepts through interactive matching. Connect physical phenomena with their explanations and formulas."
        )
        "chemistry" -> Triple(
            "Chemistry",
            R.drawable.chemistry,
            "Learn chemistry through an engaging matching game. Connect elements, compounds, and reactions with their properties and definitions."
        )
        else -> Triple(
            "Word Matching Game",
            R.drawable.english,
            "Test your knowledge by matching related pairs. This game helps improve memory and understanding of concepts."
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
        ) {
            // Subject Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = title,
                    modifier = Modifier
                        .size(120.dp)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("10", "Questions")
                StatItem("20", "Played")
                StatItem("16", "Favourited")
                StatItem("10", "Shared")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description
            Text(
                "Description",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(description)

            Spacer(modifier = Modifier.height(24.dp))

            // Questions Preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Sample Questions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* View all questions */ }) {
                    Text("View All")
                }
            }

            // Sample questions list
            val sampleQuestions = getSampleQuestions(subject)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                items(sampleQuestions) { (question, answer) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(question, fontWeight = FontWeight.Bold)
                                Text(answer, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Play Button
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text("START GAME", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            count,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

private fun getSampleQuestions(subject: String): List<Pair<String, String>> {
    return when (subject.lowercase()) {
        "english" -> listOf(
            "Apple" to "A round fruit with red or green skin",
            "Book" to "A written or printed work consisting of pages",
            "Cat" to "A small domesticated carnivorous mammal"
        )
        "math" -> listOf(
            "2 + 2" to "4",
            "5 × 6" to "30",
            "Square root of 16" to "4"
        )
        "physics" -> listOf(
            "Force" to "Mass × Acceleration",
            "Energy" to "Ability to do work",
            "Velocity" to "Speed in a given direction"
        )
        "chemistry" -> listOf(
            "H2O" to "Water molecule",
            "NaCl" to "Table salt",
            "CO2" to "Carbon dioxide"
        )
        else -> listOf(
            "Question 1" to "Answer 1",
            "Question 2" to "Answer 2",
            "Question 3" to "Answer 3"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DescriptionScreenPreview() {
    MaterialTheme {
        GameDescriptionScreen(onPlayClick = {})
    }
}