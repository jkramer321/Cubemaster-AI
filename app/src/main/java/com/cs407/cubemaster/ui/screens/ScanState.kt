package com.cs407.cubemaster.ui.screens

import androidx.annotation.StringRes
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.Cube
import com.cs407.cubemaster.solver.CubeConverter
import android.util.Log

private const val DEBUG_TAG = "CubeDebug"

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
     * Normalize scanned faces to canonical sides using center colors.
     * Canonical mapping (URFDLB colors): 0=U(s2), 1=R(s5), 2=F(s1), 3=D(s3), 4=L(s4), 5=B(s6).
     */
    private fun remapByCenter(): Map<String, Array<Array<Int>>> {
        val byCenter = mutableMapOf<Int, Array<Array<Int>>>()
        scannedFaces.values.forEach { face ->
            val center = face[1][1]
            byCenter[center] = face
        }

        logCenterSummary(byCenter)

        fun faceOrEmpty(color: Int): Array<Array<Int>> =
            byCenter[color] ?: Array(3) { Array(3) { 0 } }

        return mapOf(
            "s2" to faceOrEmpty(0), // Up
            "s5" to faceOrEmpty(1), // Right
            "s1" to faceOrEmpty(2), // Front
            "s3" to faceOrEmpty(3), // Down
            "s4" to faceOrEmpty(4), // Left
            "s6" to faceOrEmpty(5)  // Back
        )
    }
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
        val canonical = remapByCenter()
        debugRemap("ScanSession.buildCube", canonical)

        val s1 = canonical["s1"]!!.map { it.toMutableList() }.toMutableList()
        val s2 = canonical["s2"]!!.map { it.toMutableList() }.toMutableList()
        val s3 = canonical["s3"]!!.map { it.toMutableList() }.toMutableList()
        val s4 = canonical["s4"]!!.map { it.toMutableList() }.toMutableList()
        val s5 = canonical["s5"]!!.map { it.toMutableList() }.toMutableList()
        val s6 = canonical["s6"]!!.map { it.toMutableList() }.toMutableList()

        val cube = Cube(s1, s2, s3, s4, s5, s6)
        selfCheckSolvedParity(canonical)
        return cube
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
     * Snapshot facelets in solver order U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53).
     * Missing faces are filled with zeros.
     */
    fun faceletsSnapshot(): IntArray {
        val facelets = IntArray(54)

        val remapped = remapByCenter()
        debugRemap("ScanSession.snapshot", remapped)

        fun writeFace(side: String, destStart: Int) {
            val face = remapped[side] ?: return
            var idx = 0
            for (r in 0..2) {
                for (c in 0..2) {
                    facelets[destStart + idx] = face[r][c]
                    idx++
                }
            }
        }

        // Map from our sides to solver order
        writeFace("s2", 0)   // U
        writeFace("s5", 9)   // R
        writeFace("s1", 18)  // F
        writeFace("s3", 27)  // D
        writeFace("s4", 36)  // L
        writeFace("s6", 45)  // B

        return facelets
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

    private fun debugRemap(tag: String, faces: Map<String, Array<Array<Int>>>) {
        fun faceStr(side: String) = faces[side]?.joinToString(" | ") { it.joinToString(",") } ?: "missing"
        val msg = buildString {
            appendLine("$tag remap ->")
            appendLine("U(s2): ${faceStr("s2")}")
            appendLine("R(s5): ${faceStr("s5")}")
            appendLine("F(s1): ${faceStr("s1")}")
            appendLine("D(s3): ${faceStr("s3")}")
            appendLine("L(s4): ${faceStr("s4")}")
            appendLine("B(s6): ${faceStr("s6")}")
        }
        try { Log.d(DEBUG_TAG, msg) } catch (_: Exception) {}
        println(msg)
    }

    private fun logCenterSummary(byCenter: Map<Int, Array<Array<Int>>>) {
        val seen = byCenter.keys.sorted()
        val msg = "Center summary: seen=${seen.joinToString()} expected=[0,1,2,3,4,5]"
        try { Log.d(DEBUG_TAG, msg) } catch (_: Exception) {}
        println(msg)
    }

    /**
     * Quick parity/self-check: verify solver facelets build a valid CubeState.
     * Logs hints when CO/EO sums are off, which usually indicates a mirrored face.
     */
    private fun selfCheckSolvedParity(remapped: Map<String, Array<Array<Int>>>) {
        val facelets = faceletsSnapshot()
        val slice = { start: Int -> facelets.slice(start until start + 9).joinToString(",") }
        val summary = """
            Facelets snapshot U,R,F,D,L,B:
            U: [${slice(0)}]
            R: [${slice(9)}]
            F: [${slice(18)}]
            D: [${slice(27)}]
            L: [${slice(36)}]
            B: [${slice(45)}]
        """.trimIndent()
        try { Log.d(DEBUG_TAG, summary) } catch (_: Exception) {}
        println(summary)

        try {
            val state = CubeConverter.fromFacelets(facelets)
            val okMsg = "Self-check: CubeState valid (CO sum=${state.cornerOrientation.sum()}, EO sum=${state.edgeOrientation.sum()})"
            try { Log.d(DEBUG_TAG, okMsg) } catch (_: Exception) {}
            println(okMsg)
        } catch (e: Exception) {
            val errMsg = "Self-check: invalid cube state. Likely mirrored/rotated face. Details=${e.message}"
            try { Log.e(DEBUG_TAG, errMsg, e) } catch (_: Exception) {}
            println(errMsg)
        }
    }

}

