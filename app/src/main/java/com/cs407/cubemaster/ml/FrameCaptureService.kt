package com.cs407.cubemaster.ml

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import kotlin.math.min

/**
 * Service for capturing and processing camera frames for cube scanning.
 * Extracts color samples from a 3×3 grid aligned with the CubeOverlay.
 */
class FrameCaptureService {
    
    companion object {
        private const val TAG = "FrameCapture"
        
        // These values must match CubeOverlay.kt
        const val OVERLAY_WIDTH_FRACTION = 0.7f  // Canvas is 70% of container width
        const val GRID_SIZE_FRACTION = 0.8f      // Grid is 80% of canvas
        
        // Sampling parameters
        const val SAMPLE_REGION_SIZE = 30
        const val BLACK_THRESHOLD = 50
    }
    
    data class SampleRegion(
        val centerX: Float,
        val centerY: Float,
        val sampleSize: Float
    )
    
    data class CoordinateMapping(
        val scaleX: Float,
        val scaleY: Float,
        val offsetX: Float,
        val offsetY: Float,
        val previewWidth: Float,
        val previewHeight: Float
    )

    /**
     * Convert ImageProxy (RGBA_8888) to Bitmap
     */
    @OptIn(ExperimentalGetImage::class)
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val image = imageProxy.image ?: return null
            val plane = image.planes[0]
            val buffer = plane.buffer
            val rowStride = plane.rowStride
            
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            
            buffer.rewind()
            val pixels = IntArray(image.width * image.height)
            var offset = 0
            for (y in 0 until image.height) {
                buffer.position(y * rowStride)
                for (x in 0 until image.width) {
                    val r = buffer.get().toInt() and 0xFF
                    val g = buffer.get().toInt() and 0xFF
                    val b = buffer.get().toInt() and 0xFF
                    val a = buffer.get().toInt() and 0xFF
                    pixels[offset++] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            bitmap.setPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap", e)
            null
        }
    }
    
    /**
     * Calculate the FIT_CENTER mapping between preview container and camera image
     */
    private fun calculateFitCenterMapping(
        previewWidth: Float,
        previewHeight: Float,
        imageWidth: Float,
        imageHeight: Float
    ): CoordinateMapping {
        val scaleToFit = min(previewWidth / imageWidth, previewHeight / imageHeight)
        val scaledImageWidth = imageWidth * scaleToFit
        val scaledImageHeight = imageHeight * scaleToFit
        
        return CoordinateMapping(
            scaleX = 1f / scaleToFit,
            scaleY = 1f / scaleToFit,
            offsetX = (previewWidth - scaledImageWidth) / 2f,
            offsetY = (previewHeight - scaledImageHeight) / 2f,
            previewWidth = previewWidth,
            previewHeight = previewHeight
        )
    }
    
    /**
     * Calculate the 3×3 grid cell centers in camera image coordinates
     */
    private fun calculateGridRegions(mapping: CoordinateMapping): List<SampleRegion> {
        val overlayCanvasWidth = mapping.previewWidth * OVERLAY_WIDTH_FRACTION
        val canvasLeft = (mapping.previewWidth - overlayCanvasWidth) / 2f
        val canvasTop = (mapping.previewHeight - overlayCanvasWidth) / 2f
        
        val gridSize = overlayCanvasWidth * GRID_SIZE_FRACTION
        val gridLeft = canvasLeft + (overlayCanvasWidth - gridSize) / 2f
        val gridTop = canvasTop + (overlayCanvasWidth - gridSize) / 2f
        val cellSize = gridSize / 3f
        
        return (0..2).flatMap { row ->
            (0..2).map { col ->
                val previewCenterX = gridLeft + (col + 0.5f) * cellSize
                val previewCenterY = gridTop + (row + 0.5f) * cellSize
                
                SampleRegion(
                    centerX = (previewCenterX - mapping.offsetX) * mapping.scaleX,
                    centerY = (previewCenterY - mapping.offsetY) * mapping.scaleY,
                    sampleSize = (cellSize * 0.5f * mapping.scaleX).coerceAtLeast(SAMPLE_REGION_SIZE.toFloat())
                )
            }
        }
    }
    
    /**
     * Sample average RGB color from a region, excluding dark pixels
     */
    private fun sampleRegion(bitmap: Bitmap, region: SampleRegion): ColorGrouper.RGBColor {
        val centerX = region.centerX.toInt()
        val centerY = region.centerY.toInt()
        val halfSize = (region.sampleSize / 2).toInt().coerceAtLeast(10)
        
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var validPixelCount = 0
        
        for (dy in -halfSize until halfSize) {
            for (dx in -halfSize until halfSize) {
                val x = (centerX + dx).coerceIn(0, bitmap.width - 1)
                val y = (centerY + dy).coerceIn(0, bitmap.height - 1)
                
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                
                val brightness = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                if (brightness > BLACK_THRESHOLD) {
                    totalR += r
                    totalG += g
                    totalB += b
                    validPixelCount++
                }
            }
        }
        
        if (validPixelCount > 0) {
            return ColorGrouper.RGBColor(
                (totalR / validPixelCount).toInt(),
                (totalG / validPixelCount).toInt(),
                (totalB / validPixelCount).toInt()
            )
        }
        
        // Fallback: sample center pixel
        val pixel = bitmap.getPixel(
            centerX.coerceIn(0, bitmap.width - 1),
            centerY.coerceIn(0, bitmap.height - 1)
        )
        return ColorGrouper.RGBColor(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
    }
    
    /**
     * Extract 3×3 grid of colors from a camera frame
     */
    fun extractGridColors(
        imageProxy: ImageProxy,
        previewWidth: Float,
        previewHeight: Float
    ): Array<Array<ColorGrouper.RGBColor>>? {
        val originalBitmap = imageProxyToBitmap(imageProxy) ?: return null
        
        try {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            
            val bitmap = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
            } else {
                originalBitmap
            }
            
            val mapping = calculateFitCenterMapping(
                previewWidth, previewHeight,
                bitmap.width.toFloat(), bitmap.height.toFloat()
            )
            
            val regions = calculateGridRegions(mapping)
            val colors = Array(3) { Array(3) { ColorGrouper.RGBColor(128, 128, 128) } }
            
            regions.forEachIndexed { i, region ->
                val row = i / 3
                val col = i % 3
                if (region.centerX in 0f..<bitmap.width.toFloat() &&
                    region.centerY in 0f..<bitmap.height.toFloat()) {
                    colors[row][col] = sampleRegion(bitmap, region)
                }
            }
            
            if (rotationDegrees != 0 && bitmap != originalBitmap) {
                bitmap.recycle()
            }
            
            return colors
        } finally {
            originalBitmap.recycle()
        }
    }
}
