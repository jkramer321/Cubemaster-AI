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

    // Pending frame request callback - when set, the next frame from analyzer will be delivered here
    val pendingFrameRequest = remember { AtomicReference<((ImageProxy?) -> Unit)?>(null) }
    
    // Fallback: store latest frame in case request comes before analyzer is running
    val latestFrameRef = remember { AtomicReference<ImageProxy?>(null) }
    
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    // Create frame capture callback that waits for a FRESH frame
    val frameCaptureCallback = remember {
        object : FrameCaptureCallback {
            override fun onFrameRequested(callback: (ImageProxy?) -> Unit) {
                // Clear any stale frame
                latestFrameRef.getAndSet(null)?.close()
                
                // Set the pending request - the analyzer will fulfill it with the NEXT frame
                val previousRequest = pendingFrameRequest.getAndSet(callback)
                if (previousRequest != null) {
                    // There was already a pending request (shouldn't happen, but handle it)
                    Log.w("CameraPreview", "Overwriting pending frame request")
                    previousRequest(null)
                }
                
                Log.d("CameraPreview", "Frame requested - waiting for fresh frame from analyzer")
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
                            // Check if there's a pending frame request
                            val pendingCallback = pendingFrameRequest.getAndSet(null)
                            if (pendingCallback != null) {
                                // Fulfill the pending request with this fresh frame
                                Log.d("CameraPreview", "Delivering fresh frame to pending request")
                                pendingCallback(imageProxy)
                                // Don't close - the caller will handle it
                            } else {
                                // No pending request - store as latest (and close previous)
                                val previousFrame = latestFrameRef.getAndSet(imageProxy)
                                previousFrame?.close()
                            }
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
            // Cancel any pending request
            pendingFrameRequest.getAndSet(null)?.invoke(null)
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
