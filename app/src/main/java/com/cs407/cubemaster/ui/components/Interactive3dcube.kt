package com.cs407.cubemaster.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.cubemaster.data.Cube
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Interactive3DCube(
    modifier: Modifier = Modifier,
    cube: Cube = createSolvedCube()
) {
    var rotationX by remember { mutableStateOf(-25f) }
    var rotationY by remember { mutableStateOf(35f) }
    var scale by remember { mutableStateOf(1f) }

    // Create a mutable cube that we can rotate
    var currentCube by remember { mutableStateOf(cube.freeze()) }

    Column(modifier = modifier.fillMaxSize()) {
        // Cube display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationY += dragAmount.x * 0.3f
                            rotationX -= dragAmount.y * 0.3f
                        }
                    }
            ) {
                val cubeSize = size.minDimension * 0.4f * scale
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                draw3DRubiksCube(
                    centerX = centerX,
                    centerY = centerY,
                    cubeSize = cubeSize,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    cube = currentCube
                )
            }

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { scale = (scale + 0.2f).coerceAtMost(3f) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.Black
                    )
                }
                IconButton(
                    onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.Black
                    )
                }
            }
        }

        // Cube move controls
        CubeMoveControls(
            onMove = { move ->
                currentCube = performMove(currentCube, move)
            },
            onReset = {
                currentCube = createSolvedCube()
            }
        )
    }
}

@Composable
fun CubeMoveControls(
    onMove: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cube Moves",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // First row: F, R, U
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("F", Color(0xFF4CAF50)) { onMove("F") }
            MoveButton("R", Color(0xFF2196F3)) { onMove("R") }
            MoveButton("U", Color(0xFFFFC107)) { onMove("U") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second row: F', R', U'
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("F'", Color(0xFF388E3C)) { onMove("F'") }
            MoveButton("R'", Color(0xFF1976D2)) { onMove("R'") }
            MoveButton("U'", Color(0xFFFFA000)) { onMove("U'") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Third row: L, B, D
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("L", Color(0xFFFF9800)) { onMove("L") }
            MoveButton("B", Color(0xFF009688)) { onMove("B") }
            MoveButton("D", Color(0xFFF44336)) { onMove("D") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fourth row: L', B', D'
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("L'", Color(0xFFE65100)) { onMove("L'") }
            MoveButton("B'", Color(0xFF00695C)) { onMove("B'") }
            MoveButton("D'", Color(0xFFC62828)) { onMove("D'") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reset button
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "RESET",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MoveButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(100.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Perform Rubik's Cube moves
fun performMove(cube: Cube, move: String): Cube {
    val newCube = cube.freeze()

    when (move) {
        "F" -> newCube.rotateCol(2, rotateUp = true)
        "F'" -> newCube.rotateCol(2, rotateUp = false)
        "R" -> newCube.rotateRow(2, rotateRight = false)
        "R'" -> newCube.rotateRow(2, rotateRight = true)
        "U" -> newCube.rotateRow(0, rotateRight = true)
        "U'" -> newCube.rotateRow(0, rotateRight = false)
        "L" -> newCube.rotateCol(0, rotateUp = false)
        "L'" -> newCube.rotateCol(0, rotateUp = true)
        "B" -> newCube.rotateCol(0, rotateUp = false)
        "B'" -> newCube.rotateCol(0, rotateUp = true)
        "D" -> newCube.rotateRow(2, rotateRight = false)
        "D'" -> newCube.rotateRow(2, rotateRight = true)
    }

    return newCube
}

private fun DrawScope.draw3DRubiksCube(
    centerX: Float,
    centerY: Float,
    cubeSize: Float,
    rotationX: Float,
    rotationY: Float,
    cube: Cube
) {
    val cubieSize = cubeSize / 3f
    val gap = cubieSize * 0.04f
    val allFaces = mutableListOf<DrawableFace>()

    for (x in -1..1) {
        for (y in -1..1) {
            for (z in -1..1) {
                val worldPos = Vector3(
                    x * (cubieSize + gap),
                    y * (cubieSize + gap),
                    z * (cubieSize + gap)
                )

                val colors = getCubieColors(cube, x, y, z)
                val cubieFaces = generateCubieFaces(
                    position = worldPos,
                    size = cubieSize,
                    colors = colors,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    centerX = centerX,
                    centerY = centerY
                )
                allFaces.addAll(cubieFaces)
            }
        }
    }

    val sortedFaces = allFaces.sortedBy { it.depth }
    sortedFaces.forEach { face ->
        drawQuad(face.corners, face.color)
    }
}

private fun generateCubieFaces(
    position: Vector3,
    size: Float,
    colors: CubieColors,
    rotationX: Float,
    rotationY: Float,
    centerX: Float,
    centerY: Float
): List<DrawableFace> {
    val half = size / 2f
    val p = position

    val corners3D = listOf(
        Vector3(p.x - half, p.y - half, p.z + half),
        Vector3(p.x + half, p.y - half, p.z + half),
        Vector3(p.x + half, p.y + half, p.z + half),
        Vector3(p.x - half, p.y + half, p.z + half),
        Vector3(p.x - half, p.y - half, p.z - half),
        Vector3(p.x + half, p.y - half, p.z - half),
        Vector3(p.x + half, p.y + half, p.z - half),
        Vector3(p.x - half, p.y + half, p.z - half)
    )

    val rotated = corners3D.map { it.rotateX(rotationX).rotateY(rotationY) }
    val projected = rotated.map { Offset(centerX + it.x, centerY - it.y) }

    val faceData = listOf(
        FaceData(listOf(0, 1, 2, 3), colors.front),
        FaceData(listOf(5, 4, 7, 6), colors.back),
        FaceData(listOf(3, 2, 6, 7), colors.top),
        FaceData(listOf(1, 0, 4, 5), colors.bottom),
        FaceData(listOf(4, 0, 3, 7), colors.left),
        FaceData(listOf(1, 5, 6, 2), colors.right)
    )

    val result = mutableListOf<DrawableFace>()

    faceData.forEach { face ->
        if (face.color != null) {
            val face3D = face.indices.map { rotated[it] }
            val v0 = face3D[0]
            val v1 = face3D[1]
            val v2 = face3D[2]

            val edge1X = v1.x - v0.x
            val edge1Y = v1.y - v0.y
            val edge1Z = v1.z - v0.z

            val edge2X = v2.x - v0.x
            val edge2Y = v2.y - v0.y
            val edge2Z = v2.z - v0.z

            val normalZ = edge1X * edge2Y - edge1Y * edge2X

            if (normalZ > -50f) {
                val avgDepth = (face3D[0].z + face3D[1].z + face3D[2].z + face3D[3].z) / 4f
                val projectedCorners = face.indices.map { projected[it] }

                result.add(DrawableFace(projectedCorners, face.color, avgDepth))
            }
        }
    }

    return result
}

private fun DrawScope.drawQuad(corners: List<Offset>, color: Color) {
    val path = Path().apply {
        moveTo(corners[0].x, corners[0].y)
        lineTo(corners[1].x, corners[1].y)
        lineTo(corners[2].x, corners[2].y)
        lineTo(corners[3].x, corners[3].y)
        close()
    }
    drawPath(path = path, color = color, style = Fill)
    drawPath(path = path, color = Color.Black, style = Stroke(width = 3f))
}

private fun getCubieColors(cube: Cube, x: Int, y: Int, z: Int): CubieColors {
    return CubieColors(
        front = if (z == 1) getColorFromInt(cube.getCell("s1", 1 - y, x + 1)) else null,
        back = if (z == -1) getColorFromInt(cube.getCell("s6", 1 - y, 1 - x)) else null,
        top = if (y == 1) getColorFromInt(cube.getCell("s2", 1 - z, x + 1)) else null,
        bottom = if (y == -1) getColorFromInt(cube.getCell("s3", z + 1, x + 1)) else null,
        left = if (x == -1) getColorFromInt(cube.getCell("s4", 1 - y, z + 1)) else null,
        right = if (x == 1) getColorFromInt(cube.getCell("s5", 1 - y, 1 - z)) else null
    )
}

private fun getColorFromInt(colorInt: Int): Color {
    return when (colorInt) {
        0 -> Color.White
        1 -> Color.Red
        2 -> Color.Blue
        3 -> Color(0xFFFFA500)
        4 -> Color.Green
        5 -> Color.Yellow
        else -> Color.Gray
    }
}

private fun createSolvedCube(): Cube {
    return Cube(
        s1 = mutableListOf(
            mutableListOf(0, 0, 0),
            mutableListOf(0, 0, 0),
            mutableListOf(0, 0, 0)
        ),
        s2 = mutableListOf(
            mutableListOf(5, 5, 5),
            mutableListOf(5, 5, 5),
            mutableListOf(5, 5, 5)
        ),
        s3 = mutableListOf(
            mutableListOf(1, 1, 1),
            mutableListOf(1, 1, 1),
            mutableListOf(1, 1, 1)
        ),
        s4 = mutableListOf(
            mutableListOf(3, 3, 3),
            mutableListOf(3, 3, 3),
            mutableListOf(3, 3, 3)
        ),
        s5 = mutableListOf(
            mutableListOf(2, 2, 2),
            mutableListOf(2, 2, 2),
            mutableListOf(2, 2, 2)
        ),
        s6 = mutableListOf(
            mutableListOf(4, 4, 4),
            mutableListOf(4, 4, 4),
            mutableListOf(4, 4, 4)
        )
    )
}

private data class Vector3(val x: Float, val y: Float, val z: Float) {
    fun rotateX(angleDegrees: Float): Vector3 {
        val rad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        return Vector3(x, y * cos - z * sin, y * sin + z * cos)
    }

    fun rotateY(angleDegrees: Float): Vector3 {
        val rad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        return Vector3(x * cos + z * sin, y, -x * sin + z * cos)
    }
}

private data class CubieColors(
    val front: Color?, val back: Color?, val top: Color?,
    val bottom: Color?, val left: Color?, val right: Color?
)

private data class FaceData(val indices: List<Int>, val color: Color?)

private data class DrawableFace(val corners: List<Offset>, val color: Color, val depth: Float)