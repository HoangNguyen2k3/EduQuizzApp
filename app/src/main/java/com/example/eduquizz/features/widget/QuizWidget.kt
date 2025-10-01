// kotlin
package com.example.eduquizz.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.compose.ui.unit.dp

class QuizWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val prefs = currentState<Preferences>()
                val widgetType = prefs[WidgetKeys.WIDGET_TYPE] ?: 1
                WidgetCard {
                    WidgetContent(widgetType)
                }
            }
        }
    }
}

// ---- Wrapper: card container ----
@Composable
fun WidgetCard(content: @Composable () -> Unit) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        content()
    }
}

// ---- Render by type ----
@Composable
fun WidgetContent(type: Int) {
    when (type) {
        1 -> WordOfDayWidget()
        2 -> QuickQuizWidget()
        3 -> StreakWidget()
        4 -> MiniGameWidget()
        5 -> TopicWordWidget()
        else -> WordOfDayWidget()
    }
}

// ---- Widgets ----
@Composable
fun WordOfDayWidget() {
    Text(
        text = "📖 Word of the Day",
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(text = "🍎 apple — quả táo", style = TextStyle(color = GlanceTheme.colors.onBackground))
}

@Composable
fun QuickQuizWidget() {
    Text(
        text = "❓ Quick Quiz",
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(
        text = "What is 'apple' in Vietnamese?",
        style = TextStyle(color = GlanceTheme.colors.onBackground)
    )
    Spacer(modifier = GlanceModifier.height(12.dp))
    Text(text = "A. Quả táo", style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold))
    Text(text = "B. Quả cam", style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold))
}

@Composable
fun StreakWidget() {
    Text(
        text = "🔥 Streak",
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(
        text = "You’ve studied 5 days in a row!",
        style = TextStyle(color = GlanceTheme.colors.onBackground)
    )
}

@Composable
fun MiniGameWidget() {
    Text(
        text = "🎮 Mini Game",
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(text = "Guess the missing word!", style = TextStyle(color = GlanceTheme.colors.onBackground))
}

@Composable
fun TopicWordWidget() {
    Text(
        text = "🗂️ Topic Word",
        style = TextStyle(color = GlanceTheme.colors.onBackground, fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(text = "Topic: Food", style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold))
    Spacer(modifier = GlanceModifier.height(8.dp))
    Text(text = "🍞 bread — bánh mì", style = TextStyle(color = GlanceTheme.colors.onBackground))
}

// ---- Previews ----
@Preview(showBackground = true, name = "Word of the Day")
@Composable
fun PreviewWordOfDay() {
    GlanceTheme { WidgetCard { WidgetContent(1) } }
}

@Preview(showBackground = true, name = "Quick Quiz")
@Composable
fun PreviewQuickQuiz() {
    GlanceTheme { WidgetCard { WidgetContent(2) } }
}

@Preview(showBackground = true, name = "Streak")
@Composable
fun PreviewStreak() {
    GlanceTheme { WidgetCard { WidgetContent(3) } }
}

@Preview(showBackground = true, name = "Mini Game")
@Composable
fun PreviewMiniGame() {
    GlanceTheme { WidgetCard { WidgetContent(4) } }
}

@Preview(showBackground = true, name = "Topic Word")
@Composable
fun PreviewTopicWord() {
    GlanceTheme { WidgetCard { WidgetContent(5) } }
}