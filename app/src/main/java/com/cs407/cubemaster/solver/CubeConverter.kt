package com.cs407.cubemaster.solver

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

    /**
     * Convert from app's Cube to solver's CubeState
     */
    fun fromCube(cube: Cube): CubeState {
        // Read all facelets
        val facelets = IntArray(54)

        // Map faces: U(top), R(right), F(front), D(bottom), L(left), B(back)
        // Order in our facelet array: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)

        // s2 is top (U)
        for (i in 0..8) {
            facelets[i] = cube.getCell("s2", i / 3, i % 3)
        }

        // s5 is right (R)
        for (i in 0..8) {
            facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)
        }

        // s1 is front (F)
        for (i in 0..8) {
            facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)
        }

        // s3 is bottom (D)
        for (i in 0..8) {
            facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)
        }

        // s4 is left (L)
        for (i in 0..8) {
            facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)
        }

        // s6 is back (B)
        for (i in 0..8) {
            facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)
        }

        val state = fromFacelets(facelets)
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

    /**
     * Convert from facelet representation to CubeState
     * Facelet array format: U(0-8), R(9-17), F(18-26), D(27-35), L(36-44), B(45-53)
     */
    private fun fromFacelets(facelets: IntArray): CubeState {
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
        // Facelet index = row * 3 + col
        // 
        // Transformations applied during scanning:
        // - Top face (s2): Vertical flip (row 0 ↔ row 2) - row 0 = Back, row 2 = Front
        // - Bottom face (s3): 90° clockwise rotation - row 0 = Front, row 2 = Back
        // - Back face (s6): Horizontal flip (col 0 ↔ col 2) - col 0 = Left, col 2 = Right
        // - Side faces: No transformation needed
        //
        // Corner indices (after transformations):
        val cornerFacelets = arrayOf(
            intArrayOf(8, 9, 20),    // 0: URF = U[8] (s2[2][2] bottom-right, connects to Front), R[9] (s5[0][0] top-left), F[20] (s1[2][2] bottom-right)
            intArrayOf(6, 18, 38),   // 1: UFL = U[6] (s2[2][0] bottom-left, connects to Front), F[18] (s1[0][0] top-left), L[38] (s4[2][2] bottom-right)
            intArrayOf(0, 36, 47),   // 2: ULB = U[0] (s2[0][0] top-left, connects to Back), L[36] (s4[0][0] top-left), B[47] (s6[2][2] bottom-right, after horizontal flip)
            intArrayOf(2, 45, 11),   // 3: UBR = U[2] (s2[0][2] top-right, connects to Back), B[45] (s6[0][0] top-left, after horizontal flip), R[11] (s5[2][2] bottom-right)
            intArrayOf(29, 26, 15),  // 4: DFR = D[29] (s3[1][2] middle-right, after 90° CW), F[26] (s1[2][2] bottom-right), R[15] (s5[1][2] middle-right)
            intArrayOf(27, 44, 24),  // 5: DLF = D[27] (s3[0][0] top-left, after 90° CW, connects to Front), L[44] (s4[2][2] bottom-right), F[24] (s1[2][0] bottom-left)
            intArrayOf(33, 53, 42),  // 6: DBL = D[33] (s3[1][0] middle-left, after 90° CW), B[53] (s6[2][2] bottom-right, after horizontal flip), L[42] (s4[1][0] middle-left)
            intArrayOf(35, 17, 51)   // 7: DRB = D[35] (s3[2][2] bottom-right, after 90° CW, connects to Back), R[17] (s5[2][0] bottom-left), B[51] (s6[2][0] bottom-left, after horizontal flip)
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
        val foundCorners = mutableSetOf<Int>()
        for (i in 0..7) {
            val faceletIndices = cornerFacelets[i]
            val rawColors = intArrayOf(
                facelets[faceletIndices[0]],
                facelets[faceletIndices[1]],
                facelets[faceletIndices[2]]
            )
            val colors = charArrayOf(
                colorToFace(rawColors[0]),
                colorToFace(rawColors[1]),
                colorToFace(rawColors[2])
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
                        if (j in foundCorners) {
                            System.err.println("WARNING: Corner $j found at multiple positions! Position $i also maps to corner $j")
                        }
                        cornerPermutation[i] = j
                        cornerOrientation[i] = orientation
                        foundCorners.add(j)
                        found = true
                        break
                    }
                }
                if (found) break
            }
            
            if (!found) {
                val errorMsg = "Corner $i not found! " +
                        "Raw colors: [${rawColors.joinToString()}], " +
                        "Mapped colors: [${colors.joinToString()}], " +
                        "Facelet indices: [${faceletIndices.joinToString()}], " +
                        "Facelet values: [${faceletIndices.map { facelets[it] }.joinToString()}]"
                System.err.println(errorMsg)
                throw IllegalStateException("Cannot identify corner at position $i. This usually means:\n" +
                        "1. The cube scan has invalid/unrecognized colors\n" +
                        "2. The color mapping is incorrect\n" +
                        "3. The facelet indices are wrong\n" +
                        "Details: $errorMsg")
            }
        }
        
        // Verify all corners were found
        if (foundCorners.size != 8) {
            val missing = (0..7).filter { it !in foundCorners }
            throw IllegalStateException("Not all corners were identified! Missing corners: $missing. " +
                    "This indicates the cube state is invalid or the scan is incorrect.")
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
