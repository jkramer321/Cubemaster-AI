package com.cs407.cubemaster.ml

/**
 * Maps scanned 3×3 grid to the correct Cube face orientation
 * Handles face-specific coordinate transformations based on scanning perspective
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
            "s1" -> mapFrontFace(scannedGrid)      // Front
            "s2" -> mapTopFace(scannedGrid)         // Top
            "s3" -> mapBottomFace(scannedGrid)       // Bottom
            "s4" -> mapLeftFace(scannedGrid)       // Left
            "s5" -> mapRightFace(scannedGrid)      // Right
            "s6" -> mapBackFace(scannedGrid)       // Back
            else -> scannedGrid // Default: no transformation
        }
    }
    
    /**
     * Front face (s1): Direct mapping
     * Row 0 = top, Row 2 = bottom
     * Col 0 = left, Col 2 = right
     */
    private fun mapFrontFace(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Right face (s5): Standard mapping
     * When scanning from front, right face is viewed from the side
     * Row 0 = top, Row 2 = bottom
     * Col 0 = front edge, Col 2 = back edge
     */
    private fun mapRightFace(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Back face (s6): Direct mapping
     * The 3D renderer (getCubieColors) already handles column mirroring with `1 - x`
     * so we don't apply any transformation here to avoid double reversal
     */
    private fun mapBackFace(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Left face (s4): Standard mapping
     * Row 0 = top, Row 2 = bottom
     * Col 0 = back edge, Col 2 = front edge
     */
    private fun mapLeftFace(grid: Array<IntArray>): Array<IntArray> {
        return grid.map { it.clone() }.toTypedArray()
    }
    
    /**
     * Top face (s2): Row reversal to compensate for 3D renderer mapping
     * The 3D renderer (getCubieColors) maps rows opposite to Cube_Reference:
     * - Renderer: Row 0 → z=+1 (front), Row 2 → z=-1 (back)
     * - Reference: Row 0 = z=-1 (back), Row 2 = z=+1 (front)
     * So we reverse rows here to compensate
     */
    private fun mapTopFace(grid: Array<IntArray>): Array<IntArray> {
        return Array(3) { row ->
            IntArray(3) { col ->
                grid[2 - row][col] // Reverse rows only
            }
        }
    }
    
    /**
     * Bottom face (s3): Row reversal to compensate for 3D renderer mapping
     * The 3D renderer (getCubieColors) maps rows opposite to Cube_Reference:
     * - Renderer: Row 0 → z=-1 (back), Row 2 → z=+1 (front)
     * - Reference: Row 0 = z=+1 (front), Row 2 = z=-1 (back)
     * So we reverse rows here to compensate
     */
    private fun mapBottomFace(grid: Array<IntArray>): Array<IntArray> {
        return Array(3) { row ->
            IntArray(3) { col ->
                grid[2 - row][col] // Reverse rows only
            }
        }
    }
}

