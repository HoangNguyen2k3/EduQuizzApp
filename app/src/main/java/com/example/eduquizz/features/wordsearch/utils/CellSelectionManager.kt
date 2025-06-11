package com.example.wordsearch.ui.utils

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.wordsearch.model.Cell
import kotlin.collections.iterator
import kotlin.math.sqrt

class CellSelectionManager{
    private val cellPositions = mutableMapOf<Cell, Pair<Float, Float>>()

    fun registerCellPosition(cell: Cell, centerX: Float, centerY: Float){
        cellPositions[cell] = Pair(centerX, centerY)
    }

    fun createDragModifier(onCellSelected: (Cell) -> Unit): Modifier{
        return Modifier.pointerInput(Unit){
            detectDragGestures { change, dragAmount -> change.consume()
            val position = change.position

                val nearestCell = findNearestCell(position.x, position.y)
                nearestCell?.let {
                    onCellSelected(it)
                }
            }
        }
    }

    private fun findNearestCell(x: Float, y: Float): Cell? {
        var minDistance = Float.MAX_VALUE
        var nearestCell: Cell? = null

        for((cell, position) in cellPositions){
            val distance = calculateDistance(x, y, position.first, position.second)
            if(distance < minDistance){
                minDistance = distance
                nearestCell = cell
            }
        }
        return nearestCell
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    fun clear() {
        cellPositions.clear()
    }
}