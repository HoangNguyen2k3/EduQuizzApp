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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.eduquizz.R
import com.example.quizapp.data.Subject
import com.example.quizapp.ui.theme.QuizAppTheme

@Composable
fun MainScreen(
    onNavigateToEnglish:() -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabBackgroundColors = listOf(
        colorResource(id = R.color.bg_coral), // Tab Home
        colorResource(id = R.color.secondary_blue), // Tab Courses
        colorResource(id = R.color.bg_coral), // Tab Statistics
        colorResource(id = R.color.math_light_purple) // Tab Profile
    )

    // System UI Controller
    val systemUiController = rememberSystemUiController()
    val topBarColor = colorResource(id = R.color.white)
    val bottomBarColor = colorResource(id = R.color.status_bar_color) // Màu của BottomNavigationBar
    val selectedTabColor = tabBackgroundColors[selectedTab] // Màu dựa trên tab được chọn

    LaunchedEffect(selectedTab) {
        // Đồng bộ Status Bar và Navigation Bar với màu của tab hoặc BottomNavigationBar
        systemUiController.setStatusBarColor(
            color = topBarColor,
            darkIcons = selectedTabColor.luminance() < 0.5f
        )
        systemUiController.setNavigationBarColor(
            color = bottomBarColor,
            darkIcons = selectedTabColor.luminance() > 0.5f
        )
    }

    val subjects = listOf(
        Subject(
            id = "english",
            name = stringResource(id = R.string.subject_english),
            iconRes = R.drawable.eng,
            progress = 1,
            totalQuestions = 10,
            completedQuestions = 0,
            totalLessons = 6,
            gradientColors = listOf(
                colorResource(id = R.color.english_red),
                colorResource(id = R.color.english_coral),
                colorResource(id = R.color.english_pink)
            ),
            isRecommended = true
        ),
        Subject(
            id = "math",
            name = stringResource(id = R.string.subject_math),
            iconRes = R.drawable.math,
            progress = 0,
            totalQuestions = 0,
            completedQuestions = 0,
            totalLessons = 8,
            gradientColors = listOf(
                colorResource(id = R.color.math_blue),
                colorResource(id = R.color.math_purple),
                colorResource(id = R.color.math_light_purple)
            )
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HeaderSection()
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = {selectedTab = it},
                backgroundColor = colorResource(id = R.color.status_bar_color)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
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
                    .padding(paddingValues)
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
}

@Composable
private fun HomeContent(
    subjects: List<Subject>,
    onSubjectClick: (Subject) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dimensionResource(id = R.dimen.spacing_xl)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_xl)),
        contentPadding = PaddingValues(vertical = dimensionResource(id = R.dimen.spacing_xl))
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
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colorResource(id = R.color.secondary_blue).copy(alpha = 0.1f),
                        colorResource(id = R.color.primary_purple).copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = stringResource(id = R.string.courses_icon_desc),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_xl)),
                tint = colorResource(id = R.color.primary_purple)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))
            Text(
                text = stringResource(id = R.string.courses_title),
                fontSize = dimensionResource(id = R.dimen.text_xl).value.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark)
            )
            Text(
                text = stringResource(id = R.string.courses_subtitle),
                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                color = colorResource(id = R.color.text_secondary_gray),
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_medium))
            )
        }
    }
}

@Composable
private fun StatisticsContent(){
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

@Composable
private fun ProfileContent(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        colorResource(id = R.color.math_light_purple).copy(alpha = 0.1f),
                        colorResource(id = R.color.secondary_blue).copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.profile_icon_desc),
                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_xl)),
                tint = colorResource(id = R.color.math_light_purple)
            )
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))
            Text(
                text = stringResource(id = R.string.profile_title),
                fontSize = dimensionResource(id = R.dimen.text_xl).value.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.text_primary_dark)
            )
            Text(
                text = stringResource(id = R.string.profile_subtitle),
                fontSize = dimensionResource(id = R.dimen.text_normal).value.sp,
                color = colorResource(id = R.color.text_secondary_gray),
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.spacing_medium))
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab : Int,
    onTabSelected: (Int) -> Unit,
    backgroundColor: Color
){
    NavigationBar(
        containerColor = backgroundColor,
        tonalElevation = dimensionResource(id = R.dimen.nav_bar_elevation),
        windowInsets = WindowInsets(0),
        modifier = Modifier.fillMaxWidth()
    ) {
        val navItems = listOf(
            Triple(Icons.Filled.Home, Icons.Outlined.Home, stringResource(id = R.string.nav_home)),
            Triple(Icons.Filled.List, Icons.Outlined.List, stringResource(id = R.string.nav_courses)),
            Triple(Icons.Filled.Settings, Icons.Outlined.Settings, stringResource(id = R.string.nav_statistics)),
            Triple(Icons.Filled.Person, Icons.Outlined.Person, stringResource(id = R.string.nav_profile))
        )

        navItems.forEachIndexed { index, (selectedIcon, unselectedIcon, label) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedTab == index) selectedIcon else unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_normal))
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = dimensionResource(id = R.dimen.text_tiny).value.sp,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = colorResource(id = R.color.white_60),
                    unselectedTextColor = colorResource(id = R.color.white_60),
                    indicatorColor = colorResource(id = R.color.white_20)
                )
            )
        }
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
                    .padding(horizontal = dimensionResource(id = R.dimen.spacing_xl)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.level_gradient_start),
                                    colorResource(id = R.color.level_gradient_end)
                                )
                            ),
                            RoundedCornerShape(dimensionResource(id = R.dimen.corner_large))
                        )
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.badge_horizontal_padding),
                            vertical = dimensionResource(id = R.dimen.badge_vertical_padding)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.star_icon_desc),
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    Text(
                        text = stringResource(id = R.string.level_value),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_medium).value.sp
                    )
                }

                // Coins badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(id = R.color.coin_gradient_start),
                                    colorResource(id = R.color.coin_gradient_end)
                                )
                            ),
                            RoundedCornerShape(dimensionResource(id = R.dimen.corner_large))
                        )
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.badge_horizontal_padding),
                            vertical = dimensionResource(id = R.dimen.badge_vertical_padding)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(id = R.string.star_icon_desc),
                        tint = Color.White,
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_medium))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    Text(
                        text = stringResource(id = R.string.coins_value),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_medium).value.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
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
            .height(dimensionResource(id = R.dimen.subject_card_height))
            .clickable { onClick() },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.subject_card_corner)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.subject_card_elevation)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = subject.gradientColors,
                        startX = 0f,
                        endX = 1000f
                    )
                )
        ) {
            // Overlay pattern
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colorResource(id = R.color.white_10),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.spacing_xxl)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject icon
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_subject))
                        .background(
                            colorResource(id = R.color.white_20),
                            RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = subject.iconRes),
                        contentDescription = stringResource(id = R.string.subject_icon_desc),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_large))
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_xxl)))

                // Subject info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subject.name,
                        color = Color.White,
                        fontSize = dimensionResource(id = R.dimen.text_large).value.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_normal)))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    colorResource(id = R.color.white_20),
                                    RoundedCornerShape(dimensionResource(id = R.dimen.corner_small))
                                )
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.badge_small_horizontal_padding),
                                    vertical = dimensionResource(id = R.dimen.badge_small_vertical_padding)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_tiny)))
                            Text(
                                text = "${subject.progress}/${subject.totalQuestions}",
                                color = Color.White,
                                fontSize = dimensionResource(id = R.dimen.text_small).value.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_normal)))

                        // Lessons indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    colorResource(id = R.color.white_20),
                                    RoundedCornerShape(dimensionResource(id = R.dimen.corner_small))
                                )
                                .padding(
                                    horizontal = dimensionResource(id = R.dimen.badge_small_horizontal_padding),
                                    vertical = dimensionResource(id = R.dimen.badge_small_vertical_padding)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(dimensionResource(id = R.dimen.icon_small))
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_tiny)))
                            Text(
                                text = "${subject.completedQuestions}/6",
                                color = Color.White,
                                fontSize = dimensionResource(id = R.dimen.text_small).value.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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