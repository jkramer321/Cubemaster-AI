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

        return fromFacelets(facelets)
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
        // Each corner has 3 facelets in order: [primary, secondary, tertiary]
        val cornerFacelets = arrayOf(
            intArrayOf(8, 9, 20),    // 0: URF
            intArrayOf(6, 18, 38),   // 1: UFL
            intArrayOf(0, 36, 47),   // 2: ULB
            intArrayOf(2, 45, 11),   // 3: UBR
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

            // Find which corner this is
            for (j in 0..7) {
                if (colors.contentEquals(cornerColors[j])) {
                    cornerPermutation[i] = j
                    cornerOrientation[i] = 0
                    break
                } else if (charArrayOf(colors[1], colors[2], colors[0]).contentEquals(cornerColors[j])) {
                    cornerPermutation[i] = j
                    cornerOrientation[i] = 1
                    break
                } else if (charArrayOf(colors[2], colors[0], colors[1]).contentEquals(cornerColors[j])) {
                    cornerPermutation[i] = j
                    cornerOrientation[i] = 2
                    break
                }
            }
        }

        // Define edge positions and their facelet indices
        val edgeFacelets = arrayOf(
            intArrayOf(5, 10),   // 0: UR
            intArrayOf(7, 19),   // 1: UF
            intArrayOf(3, 37),   // 2: UL
            intArrayOf(1, 46),   // 3: UB
            intArrayOf(32, 14),  // 4: DR
            intArrayOf(28, 23),  // 5: DF
            intArrayOf(30, 41),  // 6: DL
            intArrayOf(34, 48),  // 7: DB
            intArrayOf(23, 12),  // 8: FR
            intArrayOf(21, 39),  // 9: FL
            intArrayOf(43, 50),  // 10: BL
            intArrayOf(52, 16)   // 11: BR
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
            "U" -> cube.rotateRow(0, true)
            "U'" -> cube.rotateRow(0, false)
            "U2" -> { cube.rotateRow(0, true); cube.rotateRow(0, true) }

            // D moves (rotate bottom)
            "D" -> cube.rotateRow(2, false)
            "D'" -> cube.rotateRow(2, true)
            "D2" -> { cube.rotateRow(2, false); cube.rotateRow(2, false) }

            // R moves (rotate right)
            "R" -> cube.rotateCol(2, true)
            "R'" -> cube.rotateCol(2, false)
            "R2" -> { cube.rotateCol(2, true); cube.rotateCol(2, true) }

            // L moves (rotate left)
            "L" -> cube.rotateCol(0, false)
            "L'" -> cube.rotateCol(0, true)
            "L2" -> { cube.rotateCol(0, false); cube.rotateCol(0, false) }

            // F moves (rotate front)
            "F" -> rotateFrontClockwise(cube)
            "F'" -> rotateFrontCounterClockwise(cube)
            "F2" -> { rotateFrontClockwise(cube); rotateFrontClockwise(cube) }

            // B moves (rotate back)
            "B" -> rotateBackClockwise(cube)
            "B'" -> rotateBackCounterClockwise(cube)
            "B2" -> { rotateBackClockwise(cube); rotateBackClockwise(cube) }
        }
    }

    // Helper functions for F and B moves (more complex due to cube structure)
    private fun rotateFrontClockwise(cube: Cube) {
        // Rotate s1 face clockwise
        val temp = cube.getRow("s1", 0)
        for (i in 0..2) {
            cube.getCell("s1", i, 2)
        }
        // TODO: Implement full F move logic
        // This requires rotating s1 face and cycling adjacent edges
    }

    private fun rotateFrontCounterClockwise(cube: Cube) {
        // TODO: Implement F' move
    }

    private fun rotateBackClockwise(cube: Cube) {
        // TODO: Implement B move
    }

    private fun rotateBackCounterClockwise(cube: Cube) {
        // TODO: Implement B' move
    }
}
