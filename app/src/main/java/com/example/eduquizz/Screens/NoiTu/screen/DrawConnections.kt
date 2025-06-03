package com.example.quizapp.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import com.example.quizapp.model.Connection
import com.example.quizapp.model.WordPair

@Composable
fun DrawConnections(
    connections: List<Connection>,
    wordPositions: List<Offset>,
    defPositions: List<Offset>,
    showResult: Boolean,
    wordPairs: List<WordPair>,
    definitions: List<String>,
    boxLeftWidth: Dp,
    boxHeight: Dp,
) {
    val density = LocalDensity.current
    val boxHeightPx = with(density) { boxHeight.toPx() }
    val boxLeftWidthPx = with(density) { boxLeftWidth.toPx() }
    
    // Kích thước của điểm kết nối
    val connectionPointRadius = 5f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (wordPositions.size == 5 && defPositions.size == 5) {
            // Vẽ các điểm kết nối cho tất cả các box
            wordPositions.forEach { position ->
                // Điểm kết nối bên phải của box từ
                val centerX = position.x + boxLeftWidthPx
                val centerY = position.y + (boxHeightPx / 2)
                
                drawCircle(
                    color = Color(0xFF9CA3AF),
                    radius = connectionPointRadius,
                    center = Offset(centerX, centerY)
                )
            }
            
            defPositions.forEach { position ->
                // Điểm kết nối bên trái của box định nghĩa
                val centerX = position.x
                val centerY = position.y + (boxHeightPx / 2)
                
                drawCircle(
                    color = Color(0xFF9CA3AF),
                    radius = connectionPointRadius,
                    center = Offset(centerX, centerY)
                )
            }

            // Vẽ các đường nối
            connections.forEach { connection ->
                if (connection.wordIndex in wordPositions.indices && 
                    connection.definitionIndex in defPositions.indices) {
                    
                    // Tính toán điểm bắt đầu và kết thúc
                    val startX = wordPositions[connection.wordIndex].x + boxLeftWidthPx
                    val startY = wordPositions[connection.wordIndex].y + (boxHeightPx / 2)
                    val endX = defPositions[connection.definitionIndex].x
                    val endY = defPositions[connection.definitionIndex].y + (boxHeightPx / 2)

                    val isCorrect = showResult && 
                        wordPairs[connection.wordIndex].definition == 
                        definitions[connection.definitionIndex]

                    val lineColor = when {
                        showResult -> if (isCorrect) Color(0xFF16A34A) else Color(0xFFEB5353)
                        else -> Color(0xFF9CA3AF)
                    }

                    // Vẽ đường nối
                    drawLine(
                        color = lineColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f
                    )
                    
                    // Vẽ lại các điểm kết nối phía trên đường nối
                    drawCircle(
                        color = lineColor,
                        radius = connectionPointRadius,
                        center = Offset(startX, startY)
                    )
                    
                    drawCircle(
                        color = lineColor,
                        radius = connectionPointRadius,
                        center = Offset(endX, endY)
                    )
                }
            }
        }
    }
}


