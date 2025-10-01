package com.example.quizapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.eduquizz.data.models.Subject

@Composable
fun SubjectCard(
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
