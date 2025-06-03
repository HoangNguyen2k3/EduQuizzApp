package com.example.quizapp.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.quizapp.data.Subject
import com.example.quizapp.ui.theme.QuizAppTheme

@Composable
fun MainScreen(
    onNavigateToEnglish:() -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val subjects = listOf(
        Subject(
            id = "english",
            name = "Tiếng Anh",
            iconRes = R.drawable.eng,
            progress = 1,
            totalQuestions = 10,
            completedQuestions = 0,
            totalLessons = 6,
            gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00)),
            isRecommended = true
        ),
        Subject(
            id = "math",
            name = "Toán học",
            iconRes = R.drawable.math,
            progress = 0,
            totalQuestions = 0,
            completedQuestions = 0,
            totalLessons = 8,
            gradientColors = listOf(Color(0xFFDA70D6), Color(0xFF9370DB))
        )

    )
    Scaffold(
        topBar = {
            HeaderSection()
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {selectedTab = it}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFD6EFFF))
        ) {
            when (selectedTab) {
                0 -> {
                    HomeContent(
                        subjects = subjects,
                        onSubjectClick = {subject ->
                            if(subject.id == "english"){
                                onNavigateToEnglish()
                            }
                        }
                    )
                }

                1 -> {
                    CoursesContent()
                }
                2 -> {
                    StatisticsContent()
                }
                3 -> {
                    ProfileContent()
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    subjects: List<Subject>,
    onSubjectClick: (Subject) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(subjects) { subject ->
            SubjectCard(
                subject = subject,
                onClick = { onSubjectClick(subject) }
            )
        }
    }
}

@Composable
private fun CoursesContent(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Khóa học",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1890FF)
        )
    }
}

@Composable
private fun StatisticsContent(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Thống kê",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1890FF)
        )
    }
}

@Composable
private fun ProfileContent(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Hồ sơ cá nhân",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1890FF)
        )
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab : Int,
    onTabSelected: (Int) -> Unit
){
    NavigationBar(
        containerColor = Color(0xFFE6F7FF),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Trang chủ"
                )
            },
            label = { Text("Trang chủ") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1890FF),
                selectedTextColor = Color(0xFF1890FF),
                unselectedIconColor = Color(0xFF8C8C8C),
                unselectedTextColor = Color(0xFF8C8C8C),
                indicatorColor = Color(0xFFB3D9FF)
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (selectedTab == 1) Icons.Filled.List else Icons.Outlined.List,
                    contentDescription = "Khóa học"
                )
            },
            label = { Text("Khóa học") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1890FF),
                selectedTextColor = Color(0xFF1890FF),
                unselectedIconColor = Color(0xFF8C8C8C),
                unselectedTextColor = Color(0xFF8C8C8C),
                indicatorColor = Color(0xFFB3D9FF)
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Thống kê"
                )
            },
            label = { Text("Thống kê") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1890FF),
                selectedTextColor = Color(0xFF1890FF),
                unselectedIconColor = Color(0xFF8C8C8C),
                unselectedTextColor = Color(0xFF8C8C8C),
                indicatorColor = Color(0xFFB3D9FF)
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Hồ sơ"
                )
            },
            label = { Text("Hồ sơ") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF1890FF),
                selectedTextColor = Color(0xFF1890FF),
                unselectedIconColor = Color(0xFF8C8C8C),
                unselectedTextColor = Color(0xFF8C8C8C),
                indicatorColor = Color(0xFFB3D9FF)
            )
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderSection() {
    TopAppBar(
        title = {},
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Color(0xFFFF8C00),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "15",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Coins
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Color(0xFFFFF8DC),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "456",
                        color = Color(0xFF8B4513),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFF5F5F5)
        )
    )
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(subject.gradientColors)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject icon
                Image(
                    painter = painterResource(id = subject.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Subject info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subject.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${subject.progress}/${subject.totalQuestions}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "${subject.completedQuestions}/6",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    QuizAppTheme {
        MainScreen()
    }
}