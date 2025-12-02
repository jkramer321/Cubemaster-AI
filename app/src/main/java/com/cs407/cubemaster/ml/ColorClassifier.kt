package com.cs407.cubemaster.ml

import android.graphics.Color

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
 */
class ColorClassifier {
    
    /**
     * Convert RGB to HSV and classify to cube color code
     * Returns color code (0-5) or -1 if classification is ambiguous
     */
    fun classifyColor(rgbColor: ColorGrouper.RGBColor): Int {
        val hsv = FloatArray(3)
        Color.RGBToHSV(rgbColor.r, rgbColor.g, rgbColor.b, hsv)
        
        val hue = hsv[0]        // 0-360
        val saturation = hsv[1] // 0-1
        val value = hsv[2]      // 0-1 (brightness)
        
        // White: Low saturation, reasonable brightness
        if (saturation < 0.15f) {
            return if (value > 0.3f) 0 else -1
        }
        
        // Low saturation - might be off-white
        if (saturation < 0.3f) {
            return if (value > 0.6f) 0 else -1
        }
        
        // Classify by hue
        return when {
            // Red: Hue around 0-10 or 350-360
            (hue <= 10f || hue >= 350f) -> 1
            
            // Orange: Hue around 10-30
            hue in 10f..30f -> 3
            
            // Yellow: Hue around 40-80
            hue in 40f..80f -> 5
            
            // Green: Hue around 80-160
            hue in 80f..160f -> 4
            
            // Blue: Hue around 200-260
            hue in 200f..260f -> 2
            
            // Edge cases
            hue in 30f..40f -> if (value > 0.6f) 5 else 3
            hue in 160f..200f -> if (hue < 180f) 4 else 2
            hue in 260f..350f -> {
                if (saturation > 0.6f) {
                    if (hue < 300f) 2 else 1
                } else -1
            }
            
            else -> -1
        }
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
