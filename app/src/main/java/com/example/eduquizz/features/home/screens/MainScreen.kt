package com.example.eduquizz.features.home.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.data.local.UserViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.eduquizz.R
import com.example.eduquizz.data.models.Subject
import com.example.eduquizz.features.ThongKe.ThongKe
import com.example.quizapp.ui.theme.QuizAppTheme

@Composable
fun MainScreen(
    onNavigateToEnglish:() -> Unit = {},
    onNavigateToMath:() -> Unit = {},
    dataviewModel: DataViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
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
    val bottomBarColor = colorResource(id = R.color.status_bar_color) // Màu của BottomNavigationBar
    val selectedTabColor = tabBackgroundColors[selectedTab] // Màu dựa trên tab được chọn

    LaunchedEffect(selectedTab) {
        // Đồng bộ Status Bar và Navigation Bar với màu của tab hoặc BottomNavigationBar
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = selectedTabColor.luminance() > 0.5f
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = bottomBarColor.luminance() > 0.5f
        )

//        systemUiController.isStatusBarContrastEnforced = false
        systemUiController.isNavigationBarContrastEnforced = false
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
            HeaderSection(dataviewModel, userViewModel)
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
                        HomeScreen(
                            subjects = subjects,
                            onSubjectClick = {subject ->
                                if(subject.id == "english"){
                                    onNavigateToEnglish()
                                }
                                if(subject.id == "math"){
                                    onNavigateToMath()
                                }
                            }
                        )
                    }

                    1 -> {
                        //CoursesScreen()
                        ThongKe()
                    }
                    2 -> {
                        SettingScreen()
                    }
                    3 -> {
                        ProfileScreen()
                    }
                }
            }
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
private fun HeaderSection(dataviewModel: DataViewModel, userViewModel: UserViewModel) {
    val userName by dataviewModel.playerName.observeAsState("")

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
                // User name badge (thay thế Level badge)
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
                        .weight(1f, fill = false)
                        .widthIn(max = 200.dp) // Giới hạn chiều rộng tối đa
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    Text(
                        //text = if (userName.isNotEmpty()) userName else "User",
                        text = if (userName.isNotEmpty()) userName else "User",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_medium).value.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                    Image(
                        painter = painterResource(id = R.drawable.coinimg),
                        contentDescription = "Ảnh PNG",
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    dataviewModel.updateGold(1000)
                    val gold by dataviewModel.gold.observeAsState(initial = 0)
                    Text(
                        text = "$gold",
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    QuizAppTheme {
        MainScreen()
    }
}