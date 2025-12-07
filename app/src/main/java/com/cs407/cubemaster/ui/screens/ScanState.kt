package com.cs407.cubemaster.ui.screens

import androidx.annotation.StringRes
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.Cube

/**
 * Represents the current state of the scanning process
 */
enum class ScanState {
    IDLE,              // Initial state, ready to start scanning
    SCANNING_FACE,     // Camera is active, waiting for user to tap "Scan"
    PREVIEW_FACE,      // Showing preview of scanned colors, waiting for Confirm/Rescan
    COMPLETE           // All 6 faces scanned, ready to navigate to validation
}

/**
 * Defines the order and mapping of faces to scan
 * Order: FRONT → RIGHT → BACK → LEFT → TOP → BOTTOM
 */
enum class FaceOrder(@StringRes val displayNameRes: Int, val cubeSide: String, val index: Int) {
    FRONT(R.string.face_front, "s1", 0),
    RIGHT(R.string.face_right, "s5", 1),
    BACK(R.string.face_back, "s6", 2),
    LEFT(R.string.face_left, "s4", 3),
    TOP(R.string.face_top, "s2", 4),
    BOTTOM(R.string.face_bottom, "s3", 5);

    companion object {
        /**
         * Get the face at a specific index (0-5)
         */
        fun fromIndex(index: Int): FaceOrder? {
            return values().find { it.index == index }
        }

        /**
         * Total number of faces to scan
         */
        const val TOTAL_FACES = 6
    }
}

/**
 * Manages the state of the cube scanning process
 */
data class ScanSession(
    val currentState: ScanState = ScanState.IDLE,
    val currentFaceIndex: Int = 0,
    val scannedFaces: Map<String, Array<Array<Int>>> = emptyMap()
) {
    /**
     * Get the current face being scanned
     */
    fun getCurrentFace(): FaceOrder? {
        return FaceOrder.fromIndex(currentFaceIndex)
    }

    /**
     * Check if all faces have been scanned
     */
    fun isComplete(): Boolean {
        return scannedFaces.size >= FaceOrder.TOTAL_FACES
    }

    /**
     * Get progress as a fraction (0.0 to 1.0)
     */
    fun getProgress(): Float {
        return scannedFaces.size.toFloat() / FaceOrder.TOTAL_FACES
    }

    /**
     * Get progress text (e.g., "1/6")
     */
    fun getProgressText(): String {
        return "${currentFaceIndex + 1}/${FaceOrder.TOTAL_FACES}"
    }

    /**
     * Create a Cube object from all scanned faces
     * Unscanned faces are initialized with zeros
     */
    fun buildCube(): Cube {
        val s1 = scannedFaces["s1"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }
        val s2 = scannedFaces["s2"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }
        val s3 = scannedFaces["s3"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }
        val s4 = scannedFaces["s4"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }
        val s5 = scannedFaces["s5"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }
        val s6 = scannedFaces["s6"]?.map { it.toMutableList() }?.toMutableList()
            ?: MutableList(3) { MutableList(3) { 0 } }

        return Cube(s1, s2, s3, s4, s5, s6)
    }

    /**
     * Add a scanned face to the session
     */
    fun addScannedFace(faceSide: String, colors: Array<Array<Int>>): ScanSession {
        val updatedFaces = scannedFaces.toMutableMap()
        updatedFaces[faceSide] = colors
        return this.copy(scannedFaces = updatedFaces)
    }

    /**
     * Move to the next face
     */
    fun moveToNextFace(): ScanSession {
        if (currentFaceIndex < FaceOrder.TOTAL_FACES - 1) {
            return this.copy(
                currentState = ScanState.SCANNING_FACE,
                currentFaceIndex = currentFaceIndex + 1
            )
        } else {
            return this.copy(currentState = ScanState.COMPLETE)
        }
    }

    /**
     * Start scanning the first face
     */
    fun startScanning(): ScanSession {
        return this.copy(
            currentState = ScanState.SCANNING_FACE,
            currentFaceIndex = 0
        )
    }

    /**
     * Transition to preview state after scanning
     */
    fun showPreview(): ScanSession {
        return this.copy(currentState = ScanState.PREVIEW_FACE)
    }

    /**
     * Return to scanning state (for rescan)
     */
    fun rescanCurrentFace(): ScanSession {
        return this.copy(currentState = ScanState.SCANNING_FACE)
    }
}

