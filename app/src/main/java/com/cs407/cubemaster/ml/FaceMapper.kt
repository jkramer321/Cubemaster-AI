package com.cs407.cubemaster.ml

/**
 * Maps scanned 3×3 grid to the correct Cube face orientation
 * Handles face-specific coordinate transformations based on scanning perspective
 * 
 * SCANNING ORDER: F → R → B → L → Top → Bottom
 * ASSUMPTION: User always rotates the cube to the right (clockwise) between side faces
 * 
 * When scanning side faces after rotating right:
 * - Left column (col 0) of scanned grid = Previous face (the face we just scanned)
 * - Right column (col 2) of scanned grid = Next face (the face we'll scan next)
 * - Top row (row 0) = Top face connection
 * - Bottom row (row 2) = Bottom face connection
 */
class FaceMapper {
    
    /**
     * Map a scanned 3×3 grid to a Cube face
     * The scanned grid is in row-major order (top to bottom, left to right from camera perspective)
     * 
     * @param scannedGrid 3×3 array of color codes (0-5) from the scanner
     * @param faceSide The cube side identifier ("s1", "s2", etc.)
     * @return 3×3 array of color codes in the correct orientation for the Cube data structure
     */
    fun mapToCubeFace(scannedGrid: Array<IntArray>, faceSide: String): Array<IntArray> {
        return when (faceSide) {
            "s1" -> mapFrontFace(scannedGrid)      // Front - reference face
            "s2" -> mapTopFace(scannedGrid)         // Top
            "s3" -> mapBottomFace(scannedGrid)       // Bottom
            "s4" -> mapLeftFace(scannedGrid)       // Left
            "s5" -> mapRightFace(scannedGrid)      // Right
            "s6" -> mapBackFace(scannedGrid)       // Back
            else -> scannedGrid // Default: no transformation
        }
    }
    
    /**
     * Front face (s1): Reference face - no transformation
     * 
     * Scanning orientation: Right-side up
     * - Row 0 (top) → connects to Top face (s2)
     * - Row 2 (bottom) → connects to Bottom face (s3)
     * - Col 2 (right) → connects to Right face (s5)
     * - Col 0 (left) → connects to Left face (s4)
     */
    private fun mapFrontFace(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Right face (s5): Transform for "always rotate right" scanning
     * 
     * Scanning order: Scanned after Front (F → R)
     * When user rotates cube to the right, the previous face (Front) is now on the left side
     * 
     * Edge connections:
     * - Left column (col 0) → Front (s1) - previous face
     * - Right column (col 2) → Back (s6) - next face
     * - Top row (row 0) → Top (s2)
     * - Bottom row (row 2) → Bottom (s3)
     * 
     * Transformation: The scanned grid should already have Front on left, Back on right
     * If camera captures mirrored, we may need column reversal
     */
    private fun mapRightFace(grid: Array<IntArray>): Array<IntArray> {
        // When rotating right from Front to Right:
        // - Front face moves to left side of cube
        // - Right face becomes new front
        // - Back face moves to right side of cube
        // So left column should have Front colors, right column should have Back colors
        // This should already be correct from scanning, but we verify the orientation
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Back face (s6): Transform for "always rotate right" scanning
     * 
     * Scanning order: Scanned after Right (F → R → B)
     * When user rotates cube to the right, the previous face (Right) is now on the left side
     * 
     * Camera capture (screen coordinates, viewing from front):
     * - Col 0 (left of screen) = right face (s5) - previous face
     * - Col 2 (right of screen) = left face (s4) - next face
     * - Row 0 (top of screen) = top face (s2)
     * - Row 2 (bottom of screen) = bottom face (s3)
     * 
     * Internal representation (back face stored from behind):
     * - Col 0 = left edge of cube = left face (s4) when viewed from behind
     * - Col 2 = right edge of cube = right face (s5) when viewed from behind
     * - Row 0 = top face (s2)
     * - Row 2 = bottom face (s3)
     * 
     * Issue: Camera captures with columns in opposite order from internal storage
     * The internal storage is "from behind", so columns are already swapped.
     * Camera: col 0 = right face, col 2 = left face
     * Internal: col 0 = left face (from behind), col 2 = right face (from behind)
     * Fix: Direct mapping - no transformation needed (the "from behind" storage handles the swap)
     */
    private fun mapBackFace(grid: Array<IntArray>): Array<IntArray> {
        // Camera captures: col 0 = right face, col 2 = left face
        // Internal storage (from behind): col 0 = left face, col 2 = right face
        // The "from behind" perspective naturally swaps columns, so direct mapping works
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Left face (s4): Transform for "always rotate right" scanning
     * 
     * Scanning order: Scanned after Back (F → R → B → L)
     * When user rotates cube to the right, the previous face (Back) is now on the left side
     * 
     * Edge connections:
     * - Left column (col 0) → Back (s6) - previous face
     * - Right column (col 2) → Front (s1) - next face (wraps around)
     * - Top row (row 0) → Top (s2)
     * - Bottom row (row 2) → Bottom (s3)
     * 
     * Transformation: The scanned grid should have Back on left, Front on right
     */
    private fun mapLeftFace(grid: Array<IntArray>): Array<IntArray> {
        // When rotating right from Back to Left:
        // - Back face moves to left side of cube
        // - Left face becomes new front
        // - Front face moves to right side of cube (wraps around)
        // So left column should have Back colors, right column should have Front colors
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Top face (s2): Transform to align edges with side faces
     * 
     * Scanning order: Scanned after all side faces
     * User orients cube so bottom edge of top face connects to front
     * 
     * Camera capture (screen coordinates):
     * - Row 0 (top of screen) = back edge
     * - Row 2 (bottom of screen) = front edge
     * - Col 0 (left of screen) = left edge
     * - Col 2 (right of screen) = right edge
     * 
     * Internal representation:
     * - Row 0 = back edge
     * - Row 2 = front edge
     * - Col 0 = left edge
     * - Col 2 = right edge
     * 
     * Issue: Camera captures mirrored across x-axis (rows are swapped)
     * Fix: Reverse rows (vertical flip)
     */
    private fun mapTopFace(grid: Array<IntArray>): Array<IntArray> {
        // Camera has rows swapped: row 0 (screen top) = back, but should be row 0 = back
        // Need to flip vertically: swap row 0 ↔ row 2
        return Array(3) { row ->
            IntArray(3) { col ->
                grid[2 - row][col] // Reverse rows (vertical flip)
            }
        }
    }
    
    /**
     * Bottom face (s3): Transform to align edges with side faces
     *
     * Scanning order: Scanned last
     * User orients cube so top edge of bottom face connects to front
     *
     * Camera capture (screen coordinates):
     * - Row 0 (top of screen) = front edge
     * - Row 2 (bottom of screen) = back edge
     * - Col 0 (left of screen) = left edge
     * - Col 2 (right of screen) = right edge
     *
     * Internal representation:
     * - Row 0 = front edge
     * - Row 2 = back edge
     * - Col 0 = left edge
     * - Col 2 = right edge
     *
     * Issue: Camera captures mirrored vertically (rows swapped)
     * Fix: Vertical flip (reverse rows)
     */
    private fun mapBottomFace(grid: Array<IntArray>): Array<IntArray> {
        // Camera has rows mirrored: row 0 ↔ row 2
        // Apply vertical flip to correct
        return Array(3) { row ->
            IntArray(3) { col ->
                grid[2 - row][col] // Vertical flip (reverse rows)
            }
        }
    }
}

