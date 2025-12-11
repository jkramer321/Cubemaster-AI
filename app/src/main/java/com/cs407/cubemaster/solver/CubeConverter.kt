package com.cs407.cubemaster.solver

import android.util.Log
import com.cs407.cubemaster.data.Cube

/**
 * Converts between the app's Cube representation and the solver's CubeState.
 *
 * The app's Cube uses a facelet representation (6 faces × 9 facelets each)
 * The solver's CubeState uses a cubie representation (8 corners + 12 edges with orientations)
 *
 * Cube orientation in the app:
 * s1: Front, s2: Top, s3: Bottom, s4: Left, s5: Right, s6: Back
 *
 * Facelet indices on each face:
 * 0 1 2
 * 3 4 5
 * 6 7 8
 */
object CubeConverter {

    data class RecoveryResult(val cubeState: CubeState, val facelets: IntArray)

    /**
     * Convert from app's Cube to solver's CubeState
     */
    fun fromCube(cube: Cube): CubeState {
        val entryMsg = "CubeConverter.fromCube invoked"
        try {
            Log.w("CubeConverter", entryMsg) // higher priority to bypass debug filters
        } catch (_: Exception) {
            // ignore
        }
        println(entryMsg)
        SolverLog.d("CubeConverter", entryMsg)

        // Build facelets in requested Kociemba-friendly order:
        // s1(front)=0-8, s2(top)=9-17, s3(bottom)=18-26,
        // s4(left)=27-35, s5(right)=36-44, s6(back)=45-53
        val kociFacelets = IntArray(54)

        fun copyFace(side: String, start: Int) {
            for (i in 0..8) {
                kociFacelets[start + i] = cube.getCell(side, i / 3, i % 3)
            }
        }

        copyFace("s1", 0)   // front
        copyFace("s2", 9)   // top
        copyFace("s3", 18)  // bottom
        copyFace("s4", 27)  // left
        copyFace("s5", 36)  // right
        copyFace("s6", 45)  // back

        logKociFacelets(kociFacelets)

        // Reorder into solver facelet layout: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)
        val solverFacelets = IntArray(54)
        System.arraycopy(kociFacelets, 9, solverFacelets, 0, 9)   // U from top
        System.arraycopy(kociFacelets, 36, solverFacelets, 9, 9)  // R from right
        System.arraycopy(kociFacelets, 0, solverFacelets, 18, 9)  // F from front
        System.arraycopy(kociFacelets, 18, solverFacelets, 27, 9) // D from bottom
        System.arraycopy(kociFacelets, 27, solverFacelets, 36, 9) // L from left
        System.arraycopy(kociFacelets, 45, solverFacelets, 45, 9) // B from back

        val state = fromFacelets(solverFacelets)
        if (!state.isValid()) {
            val errorMsg = "Invalid cube state: " +
                    "CO sum=${state.cornerOrientation.sum()}, " +
                    "EO sum=${state.edgeOrientation.sum()}, " +
                    "CP=${state.cornerPermutation.contentToString()}, " +
                    "EP=${state.edgePermutation.contentToString()}"
            System.err.println(errorMsg)
            throw IllegalStateException(errorMsg)
        }
        return state
    }

    private fun logKociFacelets(facelets: IntArray) {
        if (facelets.size != 54) return
        fun slice(start: Int) = facelets.slice(start until start + 9).joinToString(",")
        val msg =
            "Koci facelets -> s1(front 0-8): [${slice(0)}]; " +
                    "s2(top 9-17): [${slice(9)}]; " +
                    "s3(bottom 18-26): [${slice(18)}]; " +
                    "s4(left 27-35): [${slice(27)}]; " +
                    "s5(right 36-44): [${slice(36)}]; " +
                    "s6(back 45-53): [${slice(45)}]"
        SolverLog.d("CubeConverter", msg)
        // Direct log to Logcat for easier filtering
        try {
            Log.d("CubeConverter", msg)
        } catch (_: Exception) {
            // no-op fallback
        }
    }

    /**
     * Convert from facelet representation to CubeState
     * Facelet array format: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)
     */
    @JvmStatic
    fun fromFacelets(facelets: IntArray): CubeState {
        // Identify center colors
        val uColor = facelets[4]   // U center
        val rColor = facelets[13]  // R center
        val fColor = facelets[22]  // F center
        val dColor = facelets[31]  // D center
        val lColor = facelets[40]  // L center
        val bColor = facelets[49]  // B center

        // Helper to convert color to face
        fun colorToFace(color: Int): Char {
            return when (color) {
                uColor -> 'U'
                rColor -> 'R'
                fColor -> 'F'
                dColor -> 'D'
                lColor -> 'L'
                bColor -> 'B'
                else -> '?'
            }
        }

        val cornerPermutation = IntArray(8)
        val cornerOrientation = IntArray(8)
        val edgePermutation = IntArray(12)
        val edgeOrientation = IntArray(12)

        // Define corner positions and their facelet indices
        // Using Kociemba standard: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)
        // Face layout: 0 1 2 / 3 4 5 / 6 7 8
        val cornerFacelets = arrayOf(
            intArrayOf(8, 9, 20),    // 0: URF = U9 (my s2[2][2]), R1, F3
            intArrayOf(6, 18, 38),   // 1: UFL = U7 (my s2[2][0]), F1, L3
            intArrayOf(0, 36, 47),   // 2: ULB = U1 (my s2[0][0]), L1, B3
            intArrayOf(2, 45, 11),   // 3: UBR = U3 (my s2[0][2]), B1, R3
            intArrayOf(29, 26, 15),  // 4: DFR
            intArrayOf(27, 44, 24),  // 5: DLF
            intArrayOf(33, 53, 42),  // 6: DBL
            intArrayOf(35, 17, 51)   // 7: DRB
        )

        // Expected colors for each corner in solved state (URF orientation)
        val cornerColors = arrayOf(
            charArrayOf('U', 'R', 'F'), // URF
            charArrayOf('U', 'F', 'L'), // UFL
            charArrayOf('U', 'L', 'B'), // ULB
            charArrayOf('U', 'B', 'R'), // UBR
            charArrayOf('D', 'F', 'R'), // DFR
            charArrayOf('D', 'L', 'F'), // DLF
            charArrayOf('D', 'B', 'L'), // DBL
            charArrayOf('D', 'R', 'B')  // DRB
        )

        // Identify corners
        for (i in 0..7) {
            val faceletIndices = cornerFacelets[i]
            val colors = charArrayOf(
                colorToFace(facelets[faceletIndices[0]]),
                colorToFace(facelets[faceletIndices[1]]),
                colorToFace(facelets[faceletIndices[2]])
            )

            // Find which corner this is and its orientation
            // Orientation is determined by where the U/D colored facelet is:
            // - Position 0 (primary facelet on U/D face) → orientation 0
            // - Position 1 (first adjacent face) → orientation 1  
            // - Position 2 (second adjacent face) → orientation 2
            
            var found = false
            for (j in 0..7) {
                val expectedColors = cornerColors[j]
                
                // Check all 3 rotations to find which corner this is
                for (orientation in 0..2) {
                    val rotatedColors = when (orientation) {
                        0 -> charArrayOf(colors[0], colors[1], colors[2])
                        1 -> charArrayOf(colors[1], colors[2], colors[0])
                        2 -> charArrayOf(colors[2], colors[0], colors[1])
                        else -> colors
                    }
                    
                    if (rotatedColors.contentEquals(expectedColors)) {
                        cornerPermutation[i] = j
                        cornerOrientation[i] = orientation
                        found = true
                        break
                    }
                }
                if (found) break
            }
            
            if (!found) {
                System.err.println("Corner $i not found! Colors: ${colors.contentToString()}, Indices: ${faceletIndices.contentToString()}")
            }
        }

        // Indices of facelets for each edge
        // 0: UR, 1: UF, 2: UL, 3: UB
        // 4: DR, 5: DF, 6: DL, 7: DB
        // 8: FR, 9: FL, 10: BL, 11: BR
        val edgeFacelets = arrayOf(
            intArrayOf(5, 10),   // 0: UR
            intArrayOf(7, 19),   // 1: UF
            intArrayOf(3, 37),   // 2: UL
            intArrayOf(1, 46),   // 3: UB
            intArrayOf(32, 16),  // 4: DR
            intArrayOf(28, 25),  // 5: DF
            intArrayOf(30, 43),  // 6: DL
            intArrayOf(34, 52),  // 7: DB
            intArrayOf(23, 12),  // 8: FR
            intArrayOf(21, 41),  // 9: FL (Fixed index: 39->41)
            intArrayOf(50, 39),  // 10: BL (Fixed B index: 48->50)
            intArrayOf(48, 14)   // 11: BR (Fixed B index: 50->48)
        )

        // Expected colors for each edge in solved state
        val edgeColors = arrayOf(
            charArrayOf('U', 'R'), // UR
            charArrayOf('U', 'F'), // UF
            charArrayOf('U', 'L'), // UL
            charArrayOf('U', 'B'), // UB
            charArrayOf('D', 'R'), // DR
            charArrayOf('D', 'F'), // DF
            charArrayOf('D', 'L'), // DL
            charArrayOf('D', 'B'), // DB
            charArrayOf('F', 'R'), // FR
            charArrayOf('F', 'L'), // FL
            charArrayOf('B', 'L'), // BL
            charArrayOf('B', 'R')  // BR
        )

        // Identify edges
        for (i in 0..11) {
            val faceletIndices = edgeFacelets[i]
            val colors = charArrayOf(
                colorToFace(facelets[faceletIndices[0]]),
                colorToFace(facelets[faceletIndices[1]])
            )

            // Find which edge this is
            for (j in 0..11) {
                if (colors.contentEquals(edgeColors[j])) {
                    edgePermutation[i] = j
                    edgeOrientation[i] = 0
                    break
                } else if (charArrayOf(colors[1], colors[0]).contentEquals(edgeColors[j])) {
                    edgePermutation[i] = j
                    edgeOrientation[i] = 1
                    break
                }
            }
        }

        return CubeState(
            cornerPermutation,
            cornerOrientation,
            edgePermutation,
            edgeOrientation
        )
    }

    /**
     * Attempt to recover a CubeState from scanned faces where side labels may be permuted.
     * Assumes colors 0-5 correspond to U,R,F,D,L,B respectively (center colors).
     * Returns null if any face is missing.
     */
    fun recoverMappingFromScannedFaces(scanned: Map<String, Array<Array<Int>>>): RecoveryResult? {
        // Require all six faces
        if (scanned.size < 6) return null

        // Helper to find the face whose center matches a given color
        fun findFaceByCenter(color: Int): Array<Array<Int>>? {
            return scanned.values.firstOrNull { face ->
                face[1][1] == color
            }
        }

        val uFace = findFaceByCenter(0) ?: return null
        val rFace = findFaceByCenter(1) ?: return null
        val fFace = findFaceByCenter(2) ?: return null
        val dFace = findFaceByCenter(3) ?: return null
        val lFace = findFaceByCenter(4) ?: return null
        val bFace = findFaceByCenter(5) ?: return null

        val facelets = IntArray(54)

        fun copy(face: Array<Array<Int>>, destStart: Int) {
            var idx = 0
            for (r in 0..2) {
                for (c in 0..2) {
                    facelets[destStart + idx] = face[r][c]
                    idx++
                }
            }
        }

        // Solver order: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)
        copy(uFace, 0)
        copy(rFace, 9)
        copy(fFace, 18)
        copy(dFace, 27)
        copy(lFace, 36)
        copy(bFace, 45)

        val state = fromFacelets(facelets)
        return RecoveryResult(state, facelets)
    }

    /**
     * Apply a move string to a Cube
     * Move string format: "R", "U'", "F2", etc.
     */
    fun applyMoveString(cube: Cube, moveStr: String) {
        when (moveStr) {
            // U moves (rotate top)
            "U" -> cube.rotateRow(0, false)
            "U'" -> cube.rotateRow(0, true)
            "U2" -> { cube.rotateRow(0, false); cube.rotateRow(0, false) }

            // D moves (rotate bottom)
            "D" -> cube.rotateRow(2, true)
            "D'" -> cube.rotateRow(2, false)
            "D2" -> { cube.rotateRow(2, true); cube.rotateRow(2, true) }

            // R moves (rotate right)
            "R" -> cube.rotateCol(2, true)
            "R'" -> cube.rotateCol(2, false)
            "R2" -> { cube.rotateCol(2, true); cube.rotateCol(2, true) }

            // L moves (rotate left)
            "L" -> cube.rotateCol(0, false)
            "L'" -> cube.rotateCol(0, true)
            "L2" -> { cube.rotateCol(0, false); cube.rotateCol(0, false) }

            // F moves (rotate front)
            "F" -> cube.rotateDepth(0, true)
            "F'" -> cube.rotateDepth(0, false)
            "F2" -> { cube.rotateDepth(0, true); cube.rotateDepth(0, true) }

            // B moves (rotate back)
            "B" -> cube.rotateDepth(2, true)
            "B'" -> cube.rotateDepth(2, false)
            "B2" -> { cube.rotateDepth(2, true); cube.rotateDepth(2, true) }
        }
    }

    /**
     * Rotate front face (s1) clockwise
     *
     * Cube layout:
     *      s2
     *  s4  s1  s5  s6
     *      s3
     *
     * F move affects:
     * - s1 face rotates clockwise
     * - Bottom row of s2 -> Right column of s5 -> Top row of s3 -> Left column of s4 -> Bottom row of s2
     */
    private fun rotateFrontClockwise(cube: Cube) {
        // Rotate s1 face clockwise
        rotateFaceClockwise(cube, "s1")

        // Save the affected edges
        val temp2 = cube.getRow("s2", 2).toList()  // Bottom row of top (s2)
        val temp5 = listOf(
            cube.getCell("s5", 0, 0),
            cube.getCell("s5", 1, 0),
            cube.getCell("s5", 2, 0)
        )  // Left column of right (s5)
        val temp3 = cube.getRow("s3", 0).toList()  // Top row of bottom (s3)
        val temp4 = listOf(
            cube.getCell("s4", 0, 2),
            cube.getCell("s4", 1, 2),
            cube.getCell("s4", 2, 2)
        )  // Right column of left (s4)

        // Cycle: s2 bottom -> s5 left, s5 left -> s3 top, s3 top -> s4 right, s4 right -> s2 bottom
        
        // s2 bottom <- s4 right (reversed)
        // L(0,2)->U(2,2), L(1,2)->U(2,1), L(2,2)->U(2,0)
        cube.setCell("s2", 2, 0, temp4[2])
        cube.setCell("s2", 2, 1, temp4[1])
        cube.setCell("s2", 2, 2, temp4[0])

        // s5 left <- s2 bottom
        // U(2,0)->R(0,0), U(2,1)->R(1,0), U(2,2)->R(2,0)
        cube.setCell("s5", 0, 0, temp2[0])
        cube.setCell("s5", 1, 0, temp2[1])
        cube.setCell("s5", 2, 0, temp2[2])

        // s3 top <- s5 left (reversed)
        // R(2,0)->D(0,0), R(1,0)->D(0,1), R(0,0)->D(0,2)
        cube.setCell("s3", 0, 0, temp5[2])
        cube.setCell("s3", 0, 1, temp5[1])
        cube.setCell("s3", 0, 2, temp5[0])

        // s4 right <- s3 top
        // D(0,0)->L(0,2), D(0,1)->L(1,2), D(0,2)->L(2,2)
        cube.setCell("s4", 0, 2, temp3[0])
        cube.setCell("s4", 1, 2, temp3[1])
        cube.setCell("s4", 2, 2, temp3[2])
    }

    private fun rotateFrontCounterClockwise(cube: Cube) {
        // F' = F F F
        rotateFrontClockwise(cube)
        rotateFrontClockwise(cube)
        rotateFrontClockwise(cube)
    }

    /**
     * Rotate back face (s6) clockwise
     */
    private fun rotateBackClockwise(cube: Cube) {
        // Rotate s6 face clockwise
        rotateFaceClockwise(cube, "s6")

        // Save the affected edges
        val temp2 = cube.getRow("s2", 0).toList()  // Top row of top (s2)
        val temp4 = listOf(
            cube.getCell("s4", 0, 0),
            cube.getCell("s4", 1, 0),
            cube.getCell("s4", 2, 0)
        )  // Left column of left (s4)
        val temp3 = cube.getRow("s3", 2).toList()  // Bottom row of bottom (s3)
        val temp5 = listOf(
            cube.getCell("s5", 0, 2),
            cube.getCell("s5", 1, 2),
            cube.getCell("s5", 2, 2)
        )  // Right column of right (s5)

        // Cycle: s2 top -> s4 left, s4 left -> s3 bottom, s3 bottom -> s5 right, s5 right -> s2 top
        
        // s4 left <- s2 top (Reversed)
        // U(0,2)->L(0,0), U(0,1)->L(1,0), U(0,0)->L(2,0)
        cube.setCell("s4", 0, 0, temp2[2])
        cube.setCell("s4", 1, 0, temp2[1])
        cube.setCell("s4", 2, 0, temp2[0])

        // s3 bottom <- s4 left (Straight)
        // L(0,0)->D(2,0), L(1,0)->D(2,1), L(2,0)->D(2,2)
        cube.setCell("s3", 2, 0, temp4[0])
        cube.setCell("s3", 2, 1, temp4[1])
        cube.setCell("s3", 2, 2, temp4[2])

        // s5 right <- s3 bottom (Reversed)
        // D(2,2)->R(0,2), D(2,1)->R(1,2), D(2,0)->R(2,2)
        cube.setCell("s5", 0, 2, temp3[2])
        cube.setCell("s5", 1, 2, temp3[1])
        cube.setCell("s5", 2, 2, temp3[0])

        // s2 top <- s5 right (Straight)
        // R(0,2)->U(0,0), R(1,2)->U(0,1), R(2,2)->U(0,2)
        cube.setCell("s2", 0, 0, temp5[0])
        cube.setCell("s2", 0, 1, temp5[1])
        cube.setCell("s2", 0, 2, temp5[2])
    }

    private fun rotateBackCounterClockwise(cube: Cube) {
        // B' = B B B
        rotateBackClockwise(cube)
        rotateBackClockwise(cube)
        rotateBackClockwise(cube)
    }

    /**
     * Helper to rotate a face clockwise in place
     */
    private fun rotateFaceClockwise(cube: Cube, side: String) {
        val original = mutableListOf<MutableList<Int>>()
        for (row in 0..2) {
            val rowList = mutableListOf<Int>()
            for (col in 0..2) {
                rowList.add(cube.getCell(side, row, col))
            }
            original.add(rowList)
        }

        val rotated = MutableList(3) { MutableList(3) { 0 } }
        for (i in 0..2) {
            for (j in 0..2) {
                rotated[j][2 - i] = original[i][j]
            }
        }

        for (i in 0..2) {
            for (j in 0..2) {
                cube.setCell(side, i, j, rotated[i][j])
            }
        }
    }

    /**
     * Helper to access setCell method via reflection since it's private
     */
    private fun Cube.setCell(side: String, row: Int, col: Int, value: Int) {
        // Use reflection to access private setCell method
        val method = this::class.java.getDeclaredMethod("setCell", String::class.java, Int::class.java, Int::class.java, Int::class.java)
        method.isAccessible = true
        method.invoke(this, side, row, col, value)
    }
}
