package com.cs407.cubemaster.ui.components

import androidx.compose.animation.core.*
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
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Data class to represent an animation state
data class MoveAnimation(
    val axis: RotationAxis,
    val layer: Int,
    val direction: Int, // 1 for clockwise, -1 for counter-clockwise
    val targetRotation: Float = 90f
)

enum class RotationAxis { X, Y, Z }

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

    // Animation states
    var currentAnimation by remember { mutableStateOf<MoveAnimation?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    val animationScope = rememberCoroutineScope()

    // Animated rotation value
    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) animationProgress else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        finishedListener = {
            if (isAnimating) {
                // Animation finished, apply the move to the cube
                isAnimating = false
                animationProgress = 0f
                currentAnimation = null
            }
        },
        label = "cube_rotation"
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Cube display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.5f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (!isAnimating) {
                                rotationY += dragAmount.x * 0.3f
                                rotationX -= dragAmount.y * 0.3f
                            }
                        }
                    }
            ) {
                val cubeSize = size.minDimension * 0.55f * scale
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                draw3DRubiksCube(
                    centerX = centerX,
                    centerY = centerY,
                    cubeSize = cubeSize,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    cube = currentCube,
                    animation = currentAnimation,
                    animationProgress = animatedRotation
                )
            }

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = { scale = (scale + 0.2f).coerceAtMost(3f) },
                    modifier = Modifier
                        .size(40.dp)
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
                        .size(40.dp)
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
                if (!isAnimating) {
                    // Set up the animation
                    val animation = getMoveAnimation(move)
                    currentAnimation = animation
                    isAnimating = true
                    animationProgress = animation.direction * animation.targetRotation

                    // Schedule the actual cube update after animation starts
                    animationScope.launch {
                        kotlinx.coroutines.delay(400) // Match animation duration
                        currentCube = performMove(currentCube, move)
                    }
                }
            },
            onReset = {
                if (!isAnimating) {
                    currentCube = createSolvedCube()
                }
            },
            isAnimating = isAnimating
        )
    }
}

// Helper function to determine animation parameters for each move
// Based on standard Rubik's cube notation with proper face mappings
fun getMoveAnimation(move: String): MoveAnimation {
    return when (move) {
        // U/U' - Top face (row 0) rotates horizontally
        "U" -> MoveAnimation(RotationAxis.X, 0, 1)       // Top row right
        "U'" -> MoveAnimation(RotationAxis.X, 0, -1)     // Top row left

        // D/D' - Bottom face (row 2) rotates horizontally - SWAPPED for consistency
        "D" -> MoveAnimation(RotationAxis.X, 2, 1)       // Bottom row right (was -1)
        "D'" -> MoveAnimation(RotationAxis.X, 2, -1)     // Bottom row left (was 1)

        // L/L' - Left face (column 0) rotates vertically - SWAPPED for consistency
        "L" -> MoveAnimation(RotationAxis.Z, 0, -1)      // Left column up, animate DOWN (reversed for visual match)
        "L'" -> MoveAnimation(RotationAxis.Z, 0, 1)      // Left column down, animate UP (reversed for visual match)

        // R/R' - Right face (column 2) rotates vertically - FIXED!
        "R" -> MoveAnimation(RotationAxis.Z, 2, -1)      // Right column up, animate DOWN (reversed for visual match)
        "R'" -> MoveAnimation(RotationAxis.Z, 2, 1)      // Right column down, animate UP (reversed for visual match)

        // F/F' - Front face (middle column as placeholder)
        "F" -> MoveAnimation(RotationAxis.Z, 1, -1)      // Middle column up, animate DOWN (reversed for visual match)
        "F'" -> MoveAnimation(RotationAxis.Z, 1, 1)      // Middle column down, animate UP (reversed for visual match)

        // B/B' - Back face (middle row as placeholder)
        "B" -> MoveAnimation(RotationAxis.X, 1, 1)       // Middle row right
        "B'" -> MoveAnimation(RotationAxis.X, 1, -1)     // Middle row left

        else -> MoveAnimation(RotationAxis.X, 0, 1)
    }
}

@Composable
fun CubeMoveControls(
    onMove: (String) -> Unit,
    onReset: () -> Unit,
    isAnimating: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2C2C2C))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Cube Moves",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // First row: L, F, R
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("L", Color(0xFFFF9800), isAnimating) { onMove("L") }
            MoveButton("F", Color(0xFF4CAF50), isAnimating) { onMove("F") }
            MoveButton("R", Color(0xFF2196F3), isAnimating) { onMove("R") }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Second row: L', F', R'
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("L'", Color(0xFFE65100), isAnimating) { onMove("L'") }
            MoveButton("F'", Color(0xFF388E3C), isAnimating) { onMove("F'") }
            MoveButton("R'", Color(0xFF1976D2), isAnimating) { onMove("R'") }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Third row: U, B, D
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("U", Color(0xFFFFC107), isAnimating) { onMove("U") }
            MoveButton("B", Color(0xFF009688), isAnimating) { onMove("B") }
            MoveButton("D", Color(0xFFF44336), isAnimating) { onMove("D") }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Fourth row: U', B', D'
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoveButton("U'", Color(0xFFFFA000), isAnimating) { onMove("U'") }
            MoveButton("B'", Color(0xFF00695C), isAnimating) { onMove("B'") }
            MoveButton("D'", Color(0xFFC62828), isAnimating) { onMove("D'") }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset button
        Button(
            onClick = onReset,
            enabled = !isAnimating,
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "RESET",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MoveButton(
    label: String,
    color: Color,
    isAnimating: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isAnimating,
        modifier = Modifier
            .width(85.dp)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAnimating) Color.White.copy(alpha = 0.5f) else Color.White
        )
    }
}

// Perform Rubik's Cube moves
// Mapped according to standard notation with Front=White, Top=Yellow, Bottom=Red,
// Left=Orange, Right=Blue, Back=Green
fun performMove(cube: Cube, move: String): Cube {
    val newCube = cube.freeze()

    when (move) {
        // U/U' - Top face (Yellow) - Works correctly ✓
        "U" -> newCube.rotateRow(0, rotateRight = true)   // Top row rotates right
        "U'" -> newCube.rotateRow(0, rotateRight = false) // Top row rotates left

        // D/D' - Bottom face (Red) - SWAPPED to match horizontal consistency
        "D" -> newCube.rotateRow(2, rotateRight = true)   // Bottom row rotates right (was false)
        "D'" -> newCube.rotateRow(2, rotateRight = false) // Bottom row rotates left (was true)

        // L/L' - Left face (Orange) - SWAPPED for consistency with vertical commands
        "L" -> newCube.rotateCol(0, rotateUp = true)      // Left column rotates up (was false)
        "L'" -> newCube.rotateCol(0, rotateUp = false)    // Left column rotates down (was true)

        // R/R' - Right face (Blue) - FIXED! Was incorrectly using rotateRow
        "R" -> newCube.rotateCol(2, rotateUp = true)      // Right column rotates up (CW from right)
        "R'" -> newCube.rotateCol(2, rotateUp = false)    // Right column rotates down (CCW from right)

        // F/F' - Front face (White) - CANNOT BE IMPLEMENTED with current API
        // The API only supports column/row slices, not arbitrary face rotations
        // For now, map to middle column rotation as placeholder
        "F" -> newCube.rotateCol(1, rotateUp = true)      // Middle column (not true front face)
        "F'" -> newCube.rotateCol(1, rotateUp = false)    // Middle column

        // B/B' - Back face (Green) - CANNOT BE IMPLEMENTED with current API
        // Map to middle row rotation as placeholder
        "B" -> newCube.rotateRow(1, rotateRight = true)   // Middle row (not true back face)
        "B'" -> newCube.rotateRow(1, rotateRight = false) // Middle row
    }

    return newCube
}

private fun DrawScope.draw3DRubiksCube(
    centerX: Float,
    centerY: Float,
    cubeSize: Float,
    rotationX: Float,
    rotationY: Float,
    cube: Cube,
    animation: MoveAnimation?,
    animationProgress: Float
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

                // Determine if this cubie should be animated
                val shouldAnimate = animation != null && when (animation.axis) {
                    RotationAxis.X -> {
                        // For row rotations: check y coordinate
                        // Row 0 (top) = y = +1, Row 2 (bottom) = y = -1
                        when (animation.layer) {
                            0 -> y == 1   // Top row
                            1 -> y == 0   // Middle row
                            2 -> y == -1  // Bottom row
                            else -> false
                        }
                    }
                    RotationAxis.Y -> false // Not used in current implementation
                    RotationAxis.Z -> {
                        // For column rotations: check x coordinate
                        // Col 0 (left) = x = -1, Col 2 (right) = x = +1
                        when (animation.layer) {
                            0 -> x == -1  // Left column
                            1 -> x == 0   // Middle column
                            2 -> x == 1   // Right column
                            else -> false
                        }
                    }
                }

                val extraRotation = if (shouldAnimate && animation != null) {
                    when (animation.axis) {
                        RotationAxis.X -> Triple(0f, animationProgress, 0f)  // Row rotations = Y-axis rotation (horizontal)
                        RotationAxis.Y -> Triple(0f, 0f, 0f)                 // Not used
                        RotationAxis.Z -> Triple(animationProgress, 0f, 0f)  // Column rotations = X-axis rotation (vertical)
                    }
                } else {
                    Triple(0f, 0f, 0f)
                }

                val cubieFaces = generateCubieFaces(
                    position = worldPos,
                    size = cubieSize,
                    colors = colors,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    centerX = centerX,
                    centerY = centerY,
                    extraRotation = extraRotation
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
    centerY: Float,
    extraRotation: Triple<Float, Float, Float> = Triple(0f, 0f, 0f)
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

    // Apply extra rotation for animation (around the cube center, not cubie center)
    val rotatedAroundCenter = corners3D.map { corner ->
        val relativePos = Vector3(corner.x - p.x, corner.y - p.y, corner.z - p.z)
        val rotated = relativePos
            .rotateX(extraRotation.first)
            .rotateY(extraRotation.second)
            .rotateZ(extraRotation.third)
        Vector3(rotated.x + p.x, rotated.y + p.y, rotated.z + p.z)
    }

    // Apply position rotation for animation
    val animatedPosition = p
        .rotateX(extraRotation.first)
        .rotateY(extraRotation.second)
        .rotateZ(extraRotation.third)

    val finalCorners = rotatedAroundCenter.map { corner ->
        val offset = Vector3(corner.x - p.x, corner.y - p.y, corner.z - p.z)
        Vector3(animatedPosition.x + offset.x, animatedPosition.y + offset.y, animatedPosition.z + offset.z)
    }

    val rotated = finalCorners.map { it.rotateX(rotationX).rotateY(rotationY) }
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

    fun rotateZ(angleDegrees: Float): Vector3 {
        val rad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        return Vector3(x * cos - y * sin, x * sin + y * cos, z)
    }
}

private data class CubieColors(
    val front: Color?, val back: Color?, val top: Color?,
    val bottom: Color?, val left: Color?, val right: Color?
)

private data class FaceData(val indices: List<Int>, val color: Color?)

private data class DrawableFace(val corners: List<Offset>, val color: Color, val depth: Float)