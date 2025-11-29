package com.cs407.cubemaster.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference

/**
 * Callback interface for frame capture requests
 */
interface FrameCaptureCallback {
    fun onFrameRequested(callback: (ImageProxy?) -> Unit)
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    onFrameCaptureReady: ((FrameCaptureCallback) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onFrameCaptureReadyState = rememberUpdatedState(onFrameCaptureReady)

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }

    // Store latest frame for capture
    val latestFrameRef = remember { AtomicReference<ImageProxy?>(null) }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    // Create frame capture callback
    val frameCaptureCallback = remember {
        object : FrameCaptureCallback {
            override fun onFrameRequested(callback: (ImageProxy?) -> Unit) {
                val frame = latestFrameRef.getAndSet(null)
                callback(frame)
            }
        }
    }

    // Notify parent that frame capture is ready
    LaunchedEffect(Unit) {
        onFrameCaptureReadyState.value?.invoke(frameCaptureCallback)
    }

    DisposableEffect(lensFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // ImageAnalysis for frame capture - request RGB format directly
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(executor) { imageProxy ->
                            // Store latest frame (don't close, we'll handle it)
                            val previousFrame = latestFrameRef.getAndSet(imageProxy)
                            previousFrame?.close()
                        }
                    }
                
                imageAnalysis = analysis

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        analysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera initialization failed", e)
            }
        }, executor)

        onDispose {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            // Close any remaining frame
            latestFrameRef.get()?.close()
            imageAnalysis?.clearAnalyzer()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
