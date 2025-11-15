package com.cs407.cubemaster.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CubeOverlay(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.8f,
    strokeWidth: Float = 3f,
    showFaceGrids: Boolean = false
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f)
    ) {
        val w = size.width
        val h = size.height

        //  Isometric basis (all axes share the SAME edge length)
        val cos30 = cos(Math.toRadians(30.0)).toFloat()
        val sin30 = sin(Math.toRadians(30.0)).toFloat()

        val edge = minOf(w, h) * 0.28f           // 3D edge length in screen units
        val vx = Offset(cos30 * edge,  sin30 * edge)   // +X
        val vy = Offset(-cos30 * edge, sin30 * edge)   // +Y
        val vz = Offset(0f, -edge)                     // +Z (up)

        // We want the cube CENTERED. For this orientation (front vertical edge),
        // the cube center is: topFront - (vx + vy + vz) / 2.
        val cubeCenter = Offset(w / 2f, h / 2f + edge * 0.10f)
        val topFront    = cubeCenter + (vx + vy + vz) / 2f
        val bottomFront = topFront - vz                 // EXACTLY one edge length down

        // Recede one edge along X and Y from the front edge
        val topRightBack     = topFront - vx
        val bottomRightBack  = bottomFront - vx
        val topLeftBack      = topFront - vy
        val bottomLeftBack   = bottomFront - vy
        val topBackCorner    = topFront - vx - vy
        val bottomBackCorner = bottomFront - vx - vy

        // Faces touching the front vertical edge (top, left, right)
        val topFace = Path().apply {
            moveTo(topFront.x, topFront.y)
            lineTo(topRightBack.x, topRightBack.y)
            lineTo(topBackCorner.x, topBackCorner.y)
            lineTo(topLeftBack.x, topLeftBack.y)
            close()
        }
        val rightFace = Path().apply {
            moveTo(topFront.x, topFront.y)
            lineTo(bottomFront.x, bottomFront.y)
            lineTo(bottomRightBack.x, bottomRightBack.y)
            lineTo(topRightBack.x, topRightBack.y)
            close()
        }
        val leftFace = Path().apply {
            moveTo(topFront.x, topFront.y)
            lineTo(topLeftBack.x, topLeftBack.y)
            lineTo(bottomLeftBack.x, bottomLeftBack.y)
            lineTo(bottomFront.x, bottomFront.y)
            close()
        }

        // Light fills + strokes
        drawCubeFace(topFace,  color, alpha * 0.10f, strokeWidth)
        drawCubeFace(leftFace, color, alpha * 0.12f, strokeWidth)
        drawCubeFace(rightFace,color, alpha * 0.12f, strokeWidth)

        if (showFaceGrids) {
            drawFaceGrid(topFront, topRightBack, topBackCorner, topLeftBack, color.copy(alpha = alpha * 0.35f))
            drawFaceGrid(topFront, bottomFront, bottomLeftBack, topLeftBack, color.copy(alpha = alpha * 0.35f))
            drawFaceGrid(topFront, topRightBack, bottomRightBack, bottomFront, color.copy(alpha = alpha * 0.35f))
        }

        // Emphasize outer edges (and the front vertical edge)
        val edgeColor = color.copy(alpha = alpha)
        drawPath(topFace,  edgeColor, style = Stroke(strokeWidth))
        drawPath(leftFace, edgeColor, style = Stroke(strokeWidth))
        drawPath(rightFace,edgeColor, style = Stroke(strokeWidth))
        drawLine(edgeColor, start = topFront, end = bottomFront, strokeWidth = strokeWidth)
    }
}

private fun DrawScope.drawCubeFace(
    path: Path,
    color: Color,
    alpha: Float,
    strokeWidth: Float
) {
    drawPath(path = path, color = color.copy(alpha = alpha))
    drawPath(path = path, color = color.copy(alpha = alpha * 4f), style = Stroke(width = strokeWidth))
}

private fun DrawScope.drawFaceGrid(
    p0: Offset, p1: Offset, p2: Offset, p3: Offset, lineColor: Color
) {
    fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
    val stroke = 1.2f
    for (t in listOf(1f / 3f, 2f / 3f)) {
        drawLine(lineColor, lerp(p0, p1, t), lerp(p3, p2, t), stroke)
        drawLine(lineColor, lerp(p0, p3, t), lerp(p1, p2, t), stroke)
    }
}

// vector helpers
private operator fun Offset.plus(o: Offset) = Offset(x + o.x, y + o.y)
private operator fun Offset.minus(o: Offset) = Offset(x - o.x, y - o.y)
private operator fun Offset.div(k: Float) = Offset(x / k, y / k)
