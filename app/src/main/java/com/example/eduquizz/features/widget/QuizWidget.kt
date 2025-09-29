package com.example.eduquizz.features.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle


class QuizWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent()
        }
    }
}

@Composable
fun WidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            //.background(ColorProvider(R.color.widget_bg))
            .padding(16.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        // Tiêu đề
        Text(
            text = "📖 Word of the Day",
            style = TextStyle(
                //color = ColorProvider(R.color.widget_text_primary),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Nội dung từ vựng
        Text(
            text = "🍎 apple — quả táo",
            style = TextStyle(
                //color = ColorProvider(R.color.widget_text_secondary)
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // Streak
        Text(
            text = "🔥 Streak: 5 days",
            //style = TextStyle(color = ColorProvider(R.color.widget_accent))
        )
    }
}

