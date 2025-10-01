package com.example.eduquizz.features.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.edit

class QuizWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            val widgets = listOf(
                "📖 Word of the Day" to 1,
                "❓ Quick Quiz" to 2,
                "🔥 Streak" to 3,
                "🎮 Mini Game" to 4,
                "🗂️ Topic Word" to 5
            )
            val pagerState = rememberPagerState(pageCount = { widgets.size })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Pager hiển thị preview
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = widgets[page].first)
                    }
                }

                // Indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    repeat(widgets.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = if (pagerState.currentPage == index)
                                        androidx.compose.ui.graphics.Color.Blue
                                    else
                                        androidx.compose.ui.graphics.Color.Gray
                                )
                            }
                        }
                    }
                }

                // Button chọn widget
                Button(
                    onClick = {
                        val type = widgets[pagerState.currentPage].second
                        onTypeSelected(type)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("➕ Thêm tiện ích")
                }
            }
        }
    }

    private fun onTypeSelected(type: Int) {
        // Lưu cấu hình đơn giản theo appWidgetId
        val prefs = getSharedPreferences("quiz_widget_prefs", MODE_PRIVATE)
        prefs.edit { putInt("widget_type_$appWidgetId", type) }

        // Trả kết quả OK cho host
        val result = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, result)

        finish()
    }
}
