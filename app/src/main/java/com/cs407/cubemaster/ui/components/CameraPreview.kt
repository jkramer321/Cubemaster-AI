package com.cs407.cubemaster.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    onCubeDetected: ((Uri) -> Unit)? = null,
    enableDetection: Boolean = true
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val captureTriggered = remember { mutableStateOf(false) }
    val lastAnalysisTime = remember { mutableLongStateOf(0L) }

    DisposableEffect(lensFacing, enableDetection) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(android.util.Size(640, 480))
                    .build()
                    .also {
                        it.setAnalyzer(analysisExecutor) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            // Only analyze every 500ms to reduce CPU usage
                            if (enableDetection && !captureTriggered.value && onCubeDetected != null
                                && (currentTime - lastAnalysisTime.longValue) >= 500) {
                                lastAnalysisTime.longValue = currentTime
                                if (detectCubeInFrame(imageProxy)) {
                                    captureTriggered.value = true
                                    captureImage(context, imageCapture, executor) { uri ->
                                        onCubeDetected(uri)
                                    }
                                }
                            }
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    val useCases = if (enableDetection && onCubeDetected != null) {
                        listOf(preview, imageCapture, imageAnalyzer)
                    } else {
                        listOf(preview)
                    }
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        *useCases.toTypedArray()
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera initialization failed", e)
            }
        }, executor)

        onDispose {
            analysisExecutor.shutdown()
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun detectCubeInFrame(imageProxy: ImageProxy): Boolean {
    try {
        val bitmap = imageProxyToBitmap(imageProxy) ?: return false

        val width = bitmap.width
        val height = bitmap.height

        // Define center region matching the overlay area (about 50% of frame)
        val centerX = width / 2
        val centerY = height / 2
        val regionSize = minOf(width, height) / 2

        val startX = (centerX - regionSize / 2).coerceAtLeast(0)
        val startY = (centerY - regionSize / 2).coerceAtLeast(0)
        val endX = (centerX + regionSize / 2).coerceAtMost(width - 1)
        val endY = (centerY + regionSize / 2).coerceAtMost(height - 1)

        // Check for colorful regions (Rubik's cube colors)
        var colorfulPixels = 0
        var totalPixels = 0
        var edgePixels = 0

        for (y in startY until endY step 15) {
            for (x in startX until endX step 15) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff

                totalPixels++

                // Check if pixel is a saturated color (typical of Rubik's cube)
                val maxChannel = maxOf(r, g, b)
                val minChannel = minOf(r, g, b)
                val saturation = if (maxChannel > 0) (maxChannel - minChannel).toFloat() / maxChannel else 0f

                // High saturation indicates colorful squares on cube
                if (saturation > 0.3f && maxChannel > 80) {
                    colorfulPixels++
                }

                // Simple edge detection (check if neighboring pixels differ significantly)
                if (x + 15 < endX && y + 15 < endY) {
                    val nextPixel = bitmap.getPixel(x + 15, y)
                    val nextR = (nextPixel shr 16) and 0xff
                    val diff = kotlin.math.abs(r - nextR) + kotlin.math.abs(g - ((nextPixel shr 8) and 0xff)) + kotlin.math.abs(b - (nextPixel and 0xff))
                    if (diff > 100) {
                        edgePixels++
                    }
                }
            }
        }

        val colorfulRatio = if (totalPixels > 0) colorfulPixels.toFloat() / totalPixels else 0f
        val edgeRatio = if (totalPixels > 0) edgePixels.toFloat() / totalPixels else 0f

        // Cube should have both colorful areas AND edges (from the grid pattern)
        val colorThreshold = 0.2f  // At least 20% colorful pixels
        val edgeThreshold = 0.15f   // At least 15% edge pixels

        val detected = colorfulRatio > colorThreshold && edgeRatio > edgeThreshold

        if (detected) {
            Log.d("CubeDetection", "Cube detected! Colors: ${colorfulRatio * 100}%, Edges: ${edgeRatio * 100}%")
        }

        return detected
    } catch (e: Exception) {
        Log.e("CubeDetection", "Error detecting cube", e)
        return false
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, imageProxy.width, imageProxy.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, imageProxy.width, imageProxy.height), 100, out)
        val imageBytes = out.toByteArray()
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    } catch (e: Exception) {
        Log.e("ImageConversion", "Error converting image", e)
        null
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "cube_scan_${System.currentTimeMillis()}.jpg")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CubeMaster")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    Log.d("ImageCapture", "Image saved: $uri")
                    onImageCaptured(uri)
                }
            }

            override fun onError(exc: ImageCaptureException) {
                Log.e("ImageCapture", "Photo capture failed: ${exc.message}", exc)
            }
        }
    )
}
