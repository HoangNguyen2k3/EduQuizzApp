package com.example.eduquizz.features.home.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.example.eduquizz.R
import com.example.eduquizz.data.models.Subject
import com.example.quizapp.ui.components.SubjectCard

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