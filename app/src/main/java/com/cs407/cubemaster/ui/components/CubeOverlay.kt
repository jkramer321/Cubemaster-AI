package com.cs407.cubemaster.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Overlay that displays a centered 3×3 grid for single-face cube scanning
 */
@Composable
fun CubeOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.8f,
    strokeWidth: Float = 3f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height
        
        // Calculate grid size (80% of canvas, centered)
        val gridSize = minOf(w, h) * 0.8f
        val gridLeft = (w - gridSize) / 2f
        val gridTop = (h - gridSize) / 2f
        
        val gridColor = color.copy(alpha = alpha)
        
        // Draw outer border
        drawRect(
            color = gridColor,
            topLeft = Offset(gridLeft, gridTop),
            size = androidx.compose.ui.geometry.Size(gridSize, gridSize),
            style = Stroke(width = strokeWidth)
        )
        
        // Draw 3×3 grid lines
        val cellSize = gridSize / 3f
        val gridLineColor = gridColor.copy(alpha = alpha * 0.6f)
        
        // Vertical lines (2 lines dividing into 3 columns)
        for (i in 1..2) {
            val x = gridLeft + cellSize * i
            drawLine(
                color = gridLineColor,
                start = Offset(x, gridTop),
                end = Offset(x, gridTop + gridSize),
                strokeWidth = strokeWidth * 0.8f
            )
        }
        
        // Horizontal lines (2 lines dividing into 3 rows)
        for (i in 1..2) {
            val y = gridTop + cellSize * i
            drawLine(
                color = gridLineColor,
                start = Offset(gridLeft, y),
                end = Offset(gridLeft + gridSize, y),
                strokeWidth = strokeWidth * 0.8f
            )
        }
    }
}
