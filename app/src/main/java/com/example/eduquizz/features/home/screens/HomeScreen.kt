package com.example.eduquizz.features.home.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.quizapp.data.Subject
import com.example.quizapp.ui.components.SubjectCard
import com.example.quizapp.ui.main.MainScreen
import com.example.quizapp.ui.theme.QuizAppTheme

@Composable
fun HomeScreen(
    subjects: List<Subject>,
    onSubjectClick: (Subject) -> Unit
) {
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