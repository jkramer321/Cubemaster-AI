package com.cs407.cubemaster.ml

import android.graphics.Color
import android.util.Log

private const val DEBUG_TAG = "CubeDebug"

/**
 * Classifies RGB colors to Rubik's Cube color codes using HSV thresholds.
 *
 * Output is already in canonical URFDLB order:
 * 0 = White/Up, 1 = Red/Right, 2 = Green/Front, 3 = Yellow/Down,
 * 4 = Orange/Left, 5 = Blue/Back
 *
 * HSV Color Ranges (Hue: 0-360°, Saturation: 0-1, Value: 0-1):
 *
 * RED:     Hue 0-10° or 350-360° (wraps around)
 * ORANGE:  Hue 10-50°, Saturation > 0.3, Value > 0.2 (extended range to catch all variations)
 * YELLOW:  Hue 50-70°, Saturation > 0.4, Value > 0.3
 * GREEN:   Hue 80-160°
 * BLUE:    Hue 200-260°
 * WHITE:   Saturation < 0.3, Value > 0.3
 *
 * Transition zones:
 * - 35-45°: Orange/Yellow boundary - use saturation and value to distinguish
 */
class ColorClassifier {

    init {
        val mapping = "Classifier outputs canonical URFDLB: 0W,1R,2G,3Y,4O,5B"
        try { Log.d(DEBUG_TAG, "ColorClassifier: $mapping") } catch (_: Exception) {}
        println("ColorClassifier: $mapping")
    }
    
    // Explicit HSV range constants
    companion object {
        // Hue ranges (in degrees, 0-360)
        private const val RED_HUE_MAX = 10f
        private const val RED_HUE_MIN_WRAP = 350f
        
        private const val ORANGE_HUE_MIN = 10f
        private const val ORANGE_HUE_MAX = 50f  // revert to baseline
        
        private const val YELLOW_HUE_MIN = 50f
        private const val YELLOW_HUE_MAX = 75f  // slightly widened from original 70
        
        private const val GREEN_HUE_MIN = 80f
        private const val GREEN_HUE_MAX = 160f
        
        private const val BLUE_HUE_MIN = 200f
        private const val BLUE_HUE_MAX = 260f
        
        // Transition zone between orange and yellow (now handled inline)
        // Orange range extended to 50° to catch all variations
        
        // Saturation thresholds
        private const val MIN_SATURATION_FOR_COLOR = 0.4f  // Below this, likely white/gray
        private const val HIGH_SATURATION_THRESHOLD = 0.6f // High saturation suggests stronger color
        
        // Value (brightness) thresholds
        private const val MIN_VALUE_FOR_COLOR = 0.3f
        private const val BRIGHT_VALUE_THRESHOLD = 0.7f
    }
    
    /**
     * Convert RGB to HSV and classify to cube color code
     * Returns color code (0-5) or -1 if classification is ambiguous
     */
    fun classifyColor(rgbColor: ColorGrouper.RGBColor): Int {
        return classifyWithHsv(rgbColor).first
    }

    /**
     * Classify a 3×3 grid of RGB colors to cube color codes
     */
    fun classifyGrid(rgbGrid: Array<Array<ColorGrouper.RGBColor>>): Array<IntArray> {
        val classified = Array(3) { row ->
            IntArray(3) { col ->
                val (label, hsv) = classifyWithHsv(rgbGrid[row][col])
                logSticker(row, col, hsv, label)
                label
            }
        }

        // Log center sample to verify canonical labels reach downstream (once per grid)
        val center = classified[1][1]
        val msg = "ColorClassifier center (canonical) = $center"
        try { Log.d(DEBUG_TAG, msg) } catch (_: Exception) {}
        println(msg)

        return classified
    }

    private fun classifyWithHsv(rgbColor: ColorGrouper.RGBColor): Pair<Int, FloatArray> {
        val hsv = FloatArray(3)
        Color.RGBToHSV(rgbColor.r, rgbColor.g, rgbColor.b, hsv)
        
        val hue = hsv[0]        // 0-360 degrees
        val saturation = hsv[1] // 0-1 (0 = gray, 1 = full color)
        val value = hsv[2]      // 0-1 (0 = black, 1 = full brightness)
        
        // STEP 1: Check for white (low saturation, reasonable brightness)
        if (saturation < 0.15f) {
            return (if (value > 0.3f) 0 else -1) to hsv
        }
        if (saturation < 0.3f) {
            return (if (value > 0.6f) 0 else -1) to hsv
        }
        
        // STEP 2: Classify by hue with explicit ranges
        val result = when {
            // RED: Hue 0-10° or 350-360° (wraps around)
            (hue <= RED_HUE_MAX || hue >= RED_HUE_MIN_WRAP) -> 1
            
            // ORANGE: Extended range 10-50° to catch all orange variations
            // With flash, orange can appear brighter and shift towards yellow hues
            hue in ORANGE_HUE_MIN..ORANGE_HUE_MAX -> {
                // More permissive: accept orange if saturation is reasonable
                if (saturation >= 0.3f && value >= 0.2f) {
                    4  // Orange (Left)
                } else {
                    -1
                }
            }
            
            // YELLOW: Hue 50-75°
            hue in YELLOW_HUE_MIN..YELLOW_HUE_MAX -> {
                if (saturation >= MIN_SATURATION_FOR_COLOR && value >= MIN_VALUE_FOR_COLOR) {
                    3  // Yellow (Down)
                } else {
                    -1
                }
            }
            
            // GREEN: Hue 80-160°
            hue in GREEN_HUE_MIN..GREEN_HUE_MAX -> 2
            
            // GREEN/BLUE transition: 160-200 (favor green <180, blue otherwise)
            hue in 160f..200f -> if (hue < 180f) 2 else 5

            // BLUE: Hue 200-260°
            hue in BLUE_HUE_MIN..BLUE_HUE_MAX -> 5
            
            // Edge cases for other transitions
            hue in YELLOW_HUE_MAX..GREEN_HUE_MIN -> {
                // Yellow/Green transition - prefer yellow if high saturation
                if (saturation > 0.5f && value > 0.5f) 3 else 2
            }
            hue in 260f..350f -> {
                if (saturation > 0.6f) {
                    if (hue < 300f) 5 else 1  // Blue/Red transition
                } else -1
            }
            
            else -> -1  // Unclassified
        }
        
        return result to hsv
    }

    private fun logSticker(row: Int, col: Int, hsv: FloatArray, canonical: Int) {
        val msg = "Sticker r$row c$col -> H=${hsv[0].toInt()} S=${(hsv[1]*100).toInt()} V=${(hsv[2]*100).toInt()} label=$canonical"
        try { Log.d(DEBUG_TAG, msg) } catch (_: Exception) {}
        println(msg)
    }
}
