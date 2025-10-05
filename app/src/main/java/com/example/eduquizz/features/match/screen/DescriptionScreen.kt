//package com.example.eduquizz.features.match.screen
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.slideInVertically
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.eduquizz.R
//import com.example.eduquizz.features.match.repository.WordPairRepository
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun GameDescriptionScreen(
//    onPlayClick: () -> Unit,
//    onBackPressed: () -> Unit = {},
//    subject: String = "English",
//    wordPairRepository: WordPairRepository
//) {
//    var isVisible by remember { mutableStateOf(false) }
//    var wordPairCount by remember { mutableStateOf(0) }
//    var levelCount by remember { mutableStateOf(0) }
//    var isLoading by remember { mutableStateOf(true) }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Load data from repository
//    LaunchedEffect(Unit) {
//        isVisible = true
//        coroutineScope.launch {
//            try {
//                // Get word pairs count
//                val wordPairs = wordPairRepository.getAllWordPairs()
//                wordPairCount = wordPairs.size
//
//                // Get levels count
//                wordPairRepository.getAvailableLevels().onSuccess { levels ->
//                    levelCount = levels.size
//                }.onFailure {
//                    // Fallback to default level count if API fails
//                    levelCount = 4
//                }
//            } catch (e: Exception) {
//                // Handle error - could show error state or use defaults
//                wordPairCount = 0
//                levelCount = 4
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        Color(0xFFFF5722),
//                        Color(0xFFFF9800),
//                        Color(0xFFFFC107),
//                        MaterialTheme.colorScheme.background
//                    )
//                )
//            )
//    ) {
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { },
//                    navigationIcon = {
//                        IconButton(onClick = onBackPressed) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Back",
//                                tint = Color.White
//                            )
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = Color.Transparent
//                    )
//                )
//            },
//            containerColor = Color.Transparent,
//            bottomBar = {
//                AnimatedVisibility(
//                    visible = isVisible,
//                    enter = slideInVertically(
//                        initialOffsetY = { it },
//                        animationSpec = tween(800, delayMillis = 1000, easing = FastOutSlowInEasing)
//                    ) + fadeIn(animationSpec = tween(800, delayMillis = 1000))
//                ) {
//                    PlayButton(
//                        onClick = onPlayClick,
//                        modifier = Modifier.padding(20.dp)
//                    )
//                }
//            }
//        ) { paddingValues ->
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//                    .padding(horizontal = 20.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Subject Image Card
//                    AnimatedVisibility(
//                        visible = isVisible,
//                        enter = slideInVertically(
//                            initialOffsetY = { -it },
//                            animationSpec = tween(800, easing = FastOutSlowInEasing)
//                        ) + fadeIn(animationSpec = tween(800))
//                    ) {
//                        SubjectImageCard(image = getImageResource(subject), title = getTitle(subject))
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    // Game Title
//                    AnimatedVisibility(
//                        visible = isVisible,
//                        enter = slideInVertically(
//                            initialOffsetY = { it },
//                            animationSpec = tween(
//                                800,
//                                delayMillis = 200,
//                                easing = FastOutSlowInEasing
//                            )
//                        ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
//                    ) {
//                        Text(
//                            text = getTitle(subject),
//                            style = MaterialTheme.typography.headlineLarge.copy(
//                                fontSize = 28.sp,
//                                fontWeight = FontWeight.Bold
//                            ),
//                            color = Color.White,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.padding(horizontal = 16.dp)
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    // Description Card
//                    AnimatedVisibility(
//                        visible = isVisible,
//                        enter = slideInVertically(
//                            initialOffsetY = { it },
//                            animationSpec = tween(
//                                800,
//                                delayMillis = 600,
//                                easing = FastOutSlowInEasing
//                            )
//                        ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
//                    ) {
//                        DescriptionCard(description = getDescription(subject))
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    // Sample Questions Card
//                    AnimatedVisibility(
//                        visible = isVisible,
//                        enter = slideInVertically(
//                            initialOffsetY = { it },
//                            animationSpec = tween(
//                                800,
//                                delayMillis = 800,
//                                easing = FastOutSlowInEasing
//                            )
//                        ) + fadeIn(animationSpec = tween(800, delayMillis = 800))
//                    ) {
//                        SampleQuestionsCard(subject = subject)
//                    }
//                    Spacer(modifier = Modifier.height(32.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SubjectImageCard(image: Int, title: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(200.dp)
//            .shadow(8.dp, RoundedCornerShape(18.dp)),
//        shape = RoundedCornerShape(18.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White.copy(alpha = 0.95f)
//        )
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = painterResource(id = image),
//                contentDescription = title,
//                modifier = Modifier
//                    .size(120.dp)
//                    .padding(16.dp),
//                contentScale = ContentScale.Fit
//            )
//        }
//    }
//}
//
//@Composable
//private fun DescriptionCard(description: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(8.dp, RoundedCornerShape(16.dp)),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White.copy(alpha = 0.95f)
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Text(
//                "Description",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//            Spacer(modifier = Modifier.height(12.dp))
//            Text(
//                description,
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
//                lineHeight = 24.sp
//            )
//        }
//    }
//}
//
//@Composable
//private fun SampleQuestionsCard(subject: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .shadow(8.dp, RoundedCornerShape(16.dp)),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White.copy(alpha = 0.95f)
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    "Sample Questions",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                TextButton(onClick = { /* View all questions */ }) {
//                    Text(
//                        "View All",
//                        color = Color(0xFF4A85F5),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            val sampleQuestions = getSampleQuestions(subject)
//            sampleQuestions.forEachIndexed { index, (question, answer) ->
//                if (index > 0) {
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = Color(0xFFF8F9FA)
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp)
//                    ) {
//                        Text(
//                            question,
//                            fontWeight = FontWeight.Medium,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            answer,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
//                            style = MaterialTheme.typography.bodyMedium
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun PlayButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier
//            .fillMaxWidth()
//            .height(56.dp)
//            .shadow(8.dp, RoundedCornerShape(28.dp)),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color(0xFF4A85F5)
//        ),
//        shape = RoundedCornerShape(28.dp)
//    ) {
//        Icon(
//            imageVector = Icons.Default.PlayArrow,
//            contentDescription = null,
//            modifier = Modifier.size(24.dp),
//            tint = Color.White
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = "START GAME",
//            style = MaterialTheme.typography.titleMedium.copy(
//                fontWeight = FontWeight.Bold,
//                fontSize = 18.sp
//            ),
//            color = Color.White
//        )
//    }
//}
//
//private fun getTitle(subject: String): String {
//    return when (subject.lowercase()) {
//        "english" -> "English Vocabulary"
//        "math" -> "Mathematics"
//        "physics" -> "Physics"
//        "chemistry" -> "Chemistry"
//        else -> "Word Matching Game"
//    }
//}
//
//private fun getImageResource(subject: String): Int {
//    return when (subject.lowercase()) {
//        "english" -> R.drawable.english
//        "math" -> R.drawable.math
//        "physics" -> R.drawable.physics
//        "chemistry" -> R.drawable.chemistry
//        else -> R.drawable.english
//    }
//}
//
//private fun getDescription(subject: String): String {
//    return when (subject.lowercase()) {
//        "english" -> "Improve your English vocabulary through an engaging word matching game. Connect words with their correct definitions to enhance your language skills."
//        "math" -> "Test your mathematical knowledge by matching equations with their solutions. Perfect for practicing basic to advanced math concepts."
//        "physics" -> "Explore physics concepts through interactive matching. Connect physical phenomena with their explanations and formulas."
//        "chemistry" -> "Learn chemistry through an engaging matching game. Connect elements, compounds, and reactions with their properties and definitions."
//        else -> "Test your knowledge by matching related pairs. This game helps improve memory and understanding of concepts."
//    }
//}
//
//private fun getSampleQuestions(subject: String): List<Pair<String, String>> {
//    return when (subject.lowercase()) {
//        "english" -> listOf(
//            "Apple" to "A round fruit with red or green skin",
//            "Book" to "A written or printed work consisting of pages",
//            "Cat" to "A small domesticated carnivorous mammal"
//        )
//        "math" -> listOf(
//            "2 + 2" to "4",
//            "5 × 6" to "30",
//            "Square root of 16" to "4"
//        )
//        "physics" -> listOf(
//            "Force" to "Mass × Acceleration",
//            "Energy" to "Ability to do work",
//            "Velocity" to "Speed in a given direction"
//        )
//        "chemistry" -> listOf(
//            "H2O" to "Water molecule",
//            "NaCl" to "Table salt",
//            "CO2" to "Carbon dioxide"
//        )
//        else -> listOf(
//            "Question 1" to "Answer 1",
//            "Question 2" to "Answer 2",
//            "Question 3" to "Answer 3"
//        )
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GameDescriptionScreenPreview() {
//    MaterialTheme {
//        GameDescriptionScreen(
//            onPlayClick = {},
//            onBackPressed = {},
//            wordPairRepository = WordPairRepository(context = android.content.ContextWrapper(androidx.compose.ui.platform.LocalContext.current))
//        )
//    }
//}