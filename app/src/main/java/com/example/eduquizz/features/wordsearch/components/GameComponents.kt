package com.example.wordsearch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wordsearch.model.Cell
import com.example.wordsearch.model.Word
import com.example.wordsearch.ui.theme.*
import kotlin.math.sqrt


/**
 * Hiển thị grid chứa các chữ cái
 */

@Composable
fun ModernWordGrid(
    grid: List<Cell>,
    selectedCells: List<Cell>,
    hintCell: Cell?,
    onCellSelected: (Cell) -> Unit
) {
    val gridSize = sqrt(grid.size.toFloat()).toInt()

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    ) {
        items(grid.size) { index ->
            val cell = grid[index]

            ModernGridCell(
                cell = cell,
                isSelected = selectedCells.any { it.row == cell.row && it.col == cell.col },
                isActiveSelection = selectedCells.isNotEmpty() && selectedCells.last().row == cell.row && selectedCells.last().col == cell.col,
                hintCell = hintCell,
                onCellSelected = { onCellSelected(cell) }

            )
        }
    }
}

/**
 * Hiển thị một ô trong grid
 */
@Composable
fun ModernGridCell(
    cell: Cell,
    isSelected: Boolean,
    isActiveSelection: Boolean,
    hintCell: Cell?,
    onCellSelected: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            cell == hintCell -> Color.Yellow
            cell.belongsToFoundWord -> FoundWordCell
            isActiveSelection -> ActiveWordCell
            isSelected -> SelectedCell
            else -> CellBackground
        },
        animationSpec = tween(durationMillis = 300),
        label = "background_color_animation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 4.dp else 1.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onCellSelected)
            .padding(2.dp)
    ) {
        Text(
            text = cell.char.toString(),
            fontSize = 18.sp,
            fontWeight = if (isSelected || cell.belongsToFoundWord) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Hiển thị từ đang được chọn
 */

@Composable
fun ModernSelectedWordDisplay(
    selectedWord: String,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = selectedWord,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onClearSelection,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = ButtonPrimary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear Selection",
                    tint = ButtonPrimary
                )
            }
        }
    }
}

/**
 * Hiển thị danh sách các từ cần tìm
 */
@Composable
fun ModernWordsToFindList(
    wordsToFind: List<Word>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Words to Find",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wordsToFind) { word ->
                ModernWordItem(word = word)
            }
        }
    }
}

/**
 * Hiển thị một từ trong danh sách cần tìm
 */

@Composable
fun ModernWordItem(word: Word) {
    val textColor by animateColorAsState(
        targetValue = if(word.isFound) TextSecondary else TextPrimary,
        animationSpec = tween(durationMillis = 500),
        label = "text_color_animation"
    )

    Card(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (word.isFound) FoundWordCell.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ){
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = word.word,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                textDecoration = if(word.isFound) TextDecoration.LineThrough else TextDecoration.None,
                fontWeight = if(word.isFound) FontWeight.Normal else FontWeight.Medium
            )
        }
    }
    if(word.isFound){
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Found",
            tint = Color(0xFF48C67F),
            modifier = Modifier.size(20.dp)
        )
    }
}