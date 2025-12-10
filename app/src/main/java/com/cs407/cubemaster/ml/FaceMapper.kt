package com.cs407.cubemaster.ml

import android.util.Log

private const val DEBUG_TAG = "CubeDebug"

/**
 * Maps scanned 3×3 grid to the correct Cube face orientation
 * Handles face-specific coordinate transformations based on scanning perspective
 */
class FaceMapper {
    init {
        val expected = "Expected label order: 0=Up/white, 1=Right/red, 2=Front/green, 3=Down/yellow, 4=Left/orange, 5=Back/blue"
        try { Log.d(DEBUG_TAG, "FaceMapper: $expected") } catch (_: Exception) {}
        println("FaceMapper: $expected")
    }
    
    /**
     * Map a scanned 3×3 grid to a Cube face
     * The scanned grid is in row-major order (top to bottom, left to right from camera perspective)
     * 
     * @param scannedGrid 3×3 array of color codes (0-5) from the scanner
     * @param faceSide The cube side identifier ("s1", "s2", etc.)
     * @return 3×3 array of color codes in the correct orientation for the Cube data structure
     */
    fun mapToCubeFace(scannedGrid: Array<IntArray>, faceSide: String): Array<IntArray> {
        val mapped = when (faceSide) {
            "s1" -> mapFrontFace(scannedGrid)      // Front
            "s2" -> mapTopFace(scannedGrid)         // Top
            "s3" -> mapBottomFace(scannedGrid)       // Bottom
            "s4" -> mapLeftFace(scannedGrid)       // Left
            "s5" -> mapRightFace(scannedGrid)      // Right
            "s6" -> mapBackFace(scannedGrid)       // Back
            else -> scannedGrid // Default: no transformation
        }
        debugFace("FaceMapper", faceSide, mapped)
        return mapped
    }
    
    private fun mapFrontFace(grid: Array<IntArray>): Array<IntArray> = grid.map { it.clone() }.toTypedArray()
    private fun mapRightFace(grid: Array<IntArray>): Array<IntArray> =
        grid.map { it.clone() }.toTypedArray()
    // Back: no mirroring; renderer handles back-face orientation
    private fun mapBackFace(grid: Array<IntArray>): Array<IntArray> =
        grid.map { it.clone() }.toTypedArray()
    private fun mapLeftFace(grid: Array<IntArray>): Array<IntArray> =
        grid.map { it.clone() }.toTypedArray()
    // Top: no row flip (align with renderer canonical)
    private fun mapTopFace(grid: Array<IntArray>): Array<IntArray> =
        grid.map { it.clone() }.toTypedArray()
    // Bottom: no row flip
    private fun mapBottomFace(grid: Array<IntArray>): Array<IntArray> =
        grid.map { it.clone() }.toTypedArray()

    private fun debugFace(tag: String, side: String, face: Array<IntArray>) {
        val msg = buildString {
            append("Mapped face ").append(side).append(": ")
            for (r in 0..2) {
                append(face[r].joinToString(",", prefix = "[", postfix = "]"))
                if (r < 2) append(" | ")
            }
        }
        try { Log.d(DEBUG_TAG, "$tag: $msg") } catch (_: Exception) {}
        println(msg)
    }
}

