package com.cs407.cubemaster.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.ml.ColorClassifier
import com.cs407.cubemaster.ml.FaceMapper
import com.cs407.cubemaster.ml.FrameCaptureService
import com.cs407.cubemaster.ui.components.CameraPreview
import com.cs407.cubemaster.ui.components.CubeOverlay
import com.cs407.cubemaster.ui.components.FrameCaptureCallback
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(modifier: Modifier = Modifier, navController: NavController) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var scanSession by remember { mutableStateOf(ScanSession()) }
    var frameCaptureCallback by remember { mutableStateOf<FrameCaptureCallback?>(null) }
    // DEBUG: RGB color values extracted from camera frame - uncomment assignments/usage to enable RGB debug display
    // var previewRgbColors by remember { mutableStateOf<Array<Array<com.cs407.cubemaster.ml.ColorGrouper.RGBColor>>?>(null) }
    var previewColors by remember { mutableStateOf<Array<IntArray>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Track camera preview container size for accurate grid mapping
    var previewContainerWidth by remember { mutableFloatStateOf(0f) }
    var previewContainerHeight by remember { mutableFloatStateOf(0f) }

    // Services
    val frameCaptureService = remember { FrameCaptureService() }
    val colorClassifier = remember { ColorClassifier() }
    val faceMapper = remember { FaceMapper() }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // Auto-start scanning when permission is granted
    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted && scanSession.currentState == ScanState.IDLE) {
            scanSession = scanSession.startScanning()
        }
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            LightOrange,
            DarkOrange
        )
    )

    // Handle scan button click
    val onScanClick: () -> Unit = {
        if (!isProcessing && previewContainerWidth > 0 && previewContainerHeight > 0) {
            isProcessing = true
            val capturedWidth = previewContainerWidth
            val capturedHeight = previewContainerHeight
            
            frameCaptureCallback?.onFrameRequested { imageProxy ->
                if (imageProxy != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            // DEBUG: Extract RGB color values from the frame for debugging purposes
                            // To print RGB values, uncomment: android.util.Log.d("ScanScreen", "RGB Grid: ${rgbGrid.contentDeepToString()}")
                            val rgbGrid = frameCaptureService.extractGridColors(
                                imageProxy, capturedWidth, capturedHeight
                            )
                            if (rgbGrid != null) {
                                val colorCodes = colorClassifier.classifyGrid(rgbGrid)
                                withContext(Dispatchers.Main) {
                                    // DEBUG: Store RGB colors for potential debugging (currently not displayed in UI)
                                    // previewRgbColors = rgbGrid
                                    previewColors = colorCodes
                                    scanSession = scanSession.showPreview()
                                    isProcessing = false
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ScanScreen", "Error processing frame", e)
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                            }
                        } finally {
                            imageProxy.close()
                        }
                    }
                } else {
                    isProcessing = false
                }
            }
        }
    }

    // Handle confirm button click
    val onConfirmClick: () -> Unit = {
        val currentFace = scanSession.getCurrentFace()
        if (currentFace != null && previewColors != null) {
            val mappedColors = faceMapper.mapToCubeFace(previewColors!!, currentFace.cubeSide)
            // Convert Array<IntArray> to Array<Array<Int>>
            val colorsArray = Array(3) { row ->
                Array(3) { col ->
                    mappedColors[row][col]
                }
            }
            val updatedSession = scanSession.addScannedFace(currentFace.cubeSide, colorsArray)
            scanSession = updatedSession.moveToNextFace()
            previewColors = null
            // DEBUG: previewRgbColors = null
            
            // Clear the frame capture callback - the new CameraPreview will provide a fresh one
            // This prevents using a stale callback from the previous CameraPreview instance
            frameCaptureCallback = null
            
            // If complete, navigate to validation
            if (scanSession.isComplete()) {
                CubeHolder.scannedCube = scanSession.buildCube()
                navController.navigate("validation")
            }
        }
    }

    // Handle rescan button click
    val onRescanClick: () -> Unit = {
        previewColors = null
        // DEBUG: previewRgbColors = null
        isProcessing = false
        scanSession = scanSession.rescanCurrentFace()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Camera preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(32.dp)
                    .border(4.dp, LightOrange, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    // Track container size for accurate grid coordinate mapping
                    .onGloballyPositioned { coordinates ->
                        previewContainerWidth = coordinates.size.width.toFloat()
                        previewContainerHeight = coordinates.size.height.toFloat()
                    }
            ) {
                if (cameraPermissionState.status.isGranted) {
                    // Show camera only when scanning (not in preview)
                    if (scanSession.currentState == ScanState.SCANNING_FACE) {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            lensFacing = lensFacing,
                            onFrameCaptureReady = { callback ->
                                frameCaptureCallback = callback
                            }
                        )

                        // Grid overlay - must match the grid calculation in FrameCaptureService
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CubeOverlay(
                                color = Color.White,
                                alpha = 0.8f,
                                strokeWidth = 3f
                            )
                        }
                    } else if (scanSession.currentState == ScanState.PREVIEW_FACE && previewColors != null) {
                        // Show preview grid with classified colors
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            ColorPreviewGrid(colors = previewColors!!)
                            // DEBUG: Uncomment below to show RGB debug grid instead of simple color preview
                            // if (previewRgbColors != null) {
                            //     DebugColorPreviewGrid(
                            //         rgbColors = previewRgbColors!!,
                            //         classifiedColors = previewColors!!
                            //     )
                            // }
                        }
                    }

                    // Flip camera button (only show when scanning)
                    if (scanSession.currentState == ScanState.SCANNING_FACE) {
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cameraswitch,
                                contentDescription = stringResource(R.string.cd_flip_camera),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cameraPermissionState.status.shouldShowRationale) {
                                stringResource(R.string.camera_permission_required)
                            } else {
                                stringResource(R.string.requesting_permission)
                            },
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Control panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkOrange),
                contentAlignment = Alignment.Center
            ) {
                when (scanSession.currentState) {
                    ScanState.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ready to scan",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { scanSession = scanSession.startScanning() }) {
                                Text("Start Scanning")
                            }
                        }
                    }
                    
                    ScanState.SCANNING_FACE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val currentFace = scanSession.getCurrentFace()
                            Text(
                                text = "Scanning ${currentFace?.displayName ?: "Face"} (${scanSession.getProgressText()})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Processing...",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            } else {
                                Button(
                                    onClick = onScanClick,
                                    enabled = !isProcessing && frameCaptureCallback != null
                                ) {
                                    Text("Scan")
                                }
                            }
                        }
                    }
                    
                    ScanState.PREVIEW_FACE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val currentFace = scanSession.getCurrentFace()
                            Text(
                                text = "Preview ${currentFace?.displayName ?: "Face"} (${scanSession.getProgressText()})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(onClick = onRescanClick) {
                                    Text("Rescan")
                                }
                                Button(onClick = onConfirmClick) {
                                    Text("Confirm")
                                }
                            }
                        }
                    }
                    
                    ScanState.COMPLETE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "All faces scanned!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate("validation") }) {
                                Text("Continue")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Preview grid showing detected colors
 */
@Composable
fun ColorPreviewGrid(colors: Array<IntArray>) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0..2) {
                    val colorCode = colors[row][col]
                    val color = when (colorCode) {
                        0 -> Color.White      // White
                        1 -> Color.Red        // Red
                        2 -> Color.Blue       // Blue
                        3 -> Color(0xFFFF9800) // Orange
                        4 -> Color.Green     // Green
                        5 -> Color.Yellow     // Yellow
                        else -> Color.Gray   // Unknown
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color, RoundedCornerShape(4.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

/**
 * Debug preview grid showing actual RGB colors and classification
 */
@Composable
fun DebugColorPreviewGrid(
    rgbColors: Array<Array<com.cs407.cubemaster.ml.ColorGrouper.RGBColor>>,
    classifiedColors: Array<IntArray>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (col in 0..2) {
                    val rgb = rgbColors[row][col]
                    val actualColor = Color(rgb.r, rgb.g, rgb.b)
                    val classifiedCode = classifiedColors[row][col]
                    
                    val classifiedColorName = when (classifiedCode) {
                        0 -> "W"
                        1 -> "R"
                        2 -> "B"
                        3 -> "O"
                        4 -> "G"
                        5 -> "Y"
                        else -> "?"
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(actualColor, RoundedCornerShape(4.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "R:${rgb.r}\nG:${rgb.g}\nB:${rgb.b}\n→$classifiedColorName",
                            color = Color.White,
                            fontSize = 7.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    CubemasterTheme {
        ScanScreen(navController = rememberNavController())
    }
}
