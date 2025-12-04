package com.cs407.cubemaster.ml

import android.graphics.Color
import android.util.Log

/**
 * Classifies RGB colors to Rubik's Cube color codes using HSV thresholds
 * 
 * Color codes:
 * 0 = White
 * 1 = Red
 * 2 = Blue
 * 3 = Orange
 * 4 = Green
 * 5 = Yellow
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
    
    // Explicit HSV range constants
    companion object {
        // Hue ranges (in degrees, 0-360)
        private const val RED_HUE_MAX = 10f
        private const val RED_HUE_MIN_WRAP = 350f
        
        private const val ORANGE_HUE_MIN = 10f
        private const val ORANGE_HUE_MAX = 50f  // Extended to catch all orange variations
        
        private const val YELLOW_HUE_MIN = 50f   // Moved up to avoid overlap with orange
        private const val YELLOW_HUE_MAX = 70f   // Extended slightly
        
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
        val hsv = FloatArray(3)
        Color.RGBToHSV(rgbColor.r, rgbColor.g, rgbColor.b, hsv)
        
        val hue = hsv[0]        // 0-360 degrees
        val saturation = hsv[1] // 0-1 (0 = gray, 1 = full color)
        val value = hsv[2]      // 0-1 (0 = black, 1 = full brightness)
        
        // STEP 1: Check for white (low saturation, reasonable brightness)
        if (saturation < 0.15f) {
            return if (value > 0.3f) 0 else -1
        }
        if (saturation < 0.3f) {
            return if (value > 0.6f) 0 else -1
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
                    3  // Orange
                } else {
                    -1
                }
            }
            
            // YELLOW: Hue 50-70° (moved up to avoid overlap with orange)
            hue in YELLOW_HUE_MIN..YELLOW_HUE_MAX -> {
                if (saturation >= MIN_SATURATION_FOR_COLOR && value >= MIN_VALUE_FOR_COLOR) {
                    5  // Yellow
                } else {
                    -1
                }
            }
            
            // GREEN: Hue 80-160°
            hue in GREEN_HUE_MIN..GREEN_HUE_MAX -> 4
            
            // BLUE: Hue 200-260°
            hue in BLUE_HUE_MIN..BLUE_HUE_MAX -> 2
            
            // Edge cases for other transitions
            hue in YELLOW_HUE_MAX..GREEN_HUE_MIN -> {
                // Yellow/Green transition - prefer yellow if high saturation
                if (saturation > 0.5f && value > 0.5f) 5 else 4
            }
            hue in 160f..200f -> if (hue < 180f) 4 else 2  // Green/Blue transition
            hue in 260f..350f -> {
                if (saturation > 0.6f) {
                    if (hue < 300f) 2 else 1  // Blue/Red transition
                } else -1
            }
            
            else -> -1  // Unclassified
        }
        
        // Debug logging for orange/yellow classification
        if (hue in 10f..70f) {
            val colorName = when (result) {
                3 -> "ORANGE"
                5 -> "YELLOW"
                else -> "UNKNOWN"
            }
            Log.d("ColorClassifier", 
                "RGB(${rgbColor.r},${rgbColor.g},${rgbColor.b}) -> " +
                "HSV(H=${hue.toInt()}°, S=${(saturation*100).toInt()}%, V=${(value*100).toInt()}%) -> $colorName"
            )
        }
        
        return result
    }
    
    
    /**
     * Classify a 3×3 grid of RGB colors to cube color codes
     */
    fun classifyGrid(rgbGrid: Array<Array<ColorGrouper.RGBColor>>): Array<IntArray> {
        return Array(3) { row ->
            IntArray(3) { col ->
                classifyColor(rgbGrid[row][col])
            }
        }
    }
}
