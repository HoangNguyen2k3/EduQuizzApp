package com.example.eduquizz.features.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.launch
import androidx.core.content.edit

class QuizWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lấy appWidgetId
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Chọn chế độ widget:")
                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { onTypeSelected(1) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("1 - Word of the Day")
                }
                Button(onClick = { onTypeSelected(2) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("2 - Quick Quiz")
                }
                Button(onClick = { onTypeSelected(3) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("3 - Streak")
                }
                Button(onClick = { onTypeSelected(4) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("4 - Mini Game")
                }
                Button(onClick = { onTypeSelected(5) }, modifier = Modifier.fillMaxWidth()) {
                    Text("5 - Topic Word")
                }
            }
        }
    }

    private fun onTypeSelected(type: Int) {
        // vì updateAppWidgetState và update(...) là suspend, dùng lifecycleScope
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@QuizWidgetConfigureActivity)
            val glanceIds = manager.getGlanceIds(QuizWidget::class.java)

            // tìm GlanceId tương ứng với appWidgetId
            val targetGlanceId = glanceIds.firstOrNull { gid ->
                manager.getAppWidgetId(gid) == appWidgetId
            }

            if (targetGlanceId != null) {
                // lưu type vào store của thư viện Glance cho instance này
                updateAppWidgetState(this@QuizWidgetConfigureActivity, targetGlanceId) { prefs ->
                    prefs[WidgetKeys.WIDGET_TYPE] = type
                }
                // cập nhật UI của widget instance đó
                QuizWidget().update(this@QuizWidgetConfigureActivity, targetGlanceId)
            } else {
                // fallback (hiếm xảy ra): lưu tạm vào SharedPreferences app theo appWidgetId
                val sp = getSharedPreferences("legacy_widget_prefs", MODE_PRIVATE)
                sp.edit {
                    putInt("widget_type_${'$'}appWidgetId", type)
                }
                // cập nhật tất cả widget của lớp này thủ công
                val allIds = manager.getGlanceIds(QuizWidget::class.java)
                allIds.forEach { gid ->
                    QuizWidget().update(this@QuizWidgetConfigureActivity, gid)
                }
            }

            // trả về cho system biết cấu hình thành công
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onDestroy() {
        // nếu user thoát mà không chọn -> hủy
        if (isFinishing && resultCodeIsSetToOkNot()) {
            setResult(RESULT_CANCELED)
        }
        super.onDestroy()
    }

    private fun resultCodeIsSetToOkNot(): Boolean {
        // đơn giản: không implement tracking riêng ở đây; Android system sẽ xử lý nếu không setResult OK
        return false
    }
}
