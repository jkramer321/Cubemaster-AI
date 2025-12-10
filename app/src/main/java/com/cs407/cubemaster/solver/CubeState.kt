package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube

/**
 * Represents a Rubik's cube in Kociemba's coordinate system.
 *
 * Kociemba's algorithm represents the cube using:
 * - Corner positions (8 corners, 8! permutations)
 * - Corner orientations (8 corners, 3^8 orientations, constraint: sum mod 3 = 0)
 * - Edge positions (12 edges, 12! permutations)
 * - Edge orientations (12 edges, 2^12 orientations, constraint: sum mod 2 = 0)
 *
 * Phase 1: Reduce cube to G1 subgroup (oriented edges, positioned slice edges)
 * Phase 2: Solve within G1 subgroup (all pieces to correct positions)
 */
data class CubeState(
    // Corner permutation: which corner is in each position (0-7)
    val cornerPermutation: IntArray,
    // Corner orientation: orientation of each corner (0-2)
    val cornerOrientation: IntArray,
    // Edge permutation: which edge is in each position (0-11)
    val edgePermutation: IntArray,
    // Edge orientation: orientation of each edge (0-1)
    val edgeOrientation: IntArray
) {

    init {
        require(cornerPermutation.size == 8) { "Corner permutation must have 8 elements" }
        require(cornerOrientation.size == 8) { "Corner orientation must have 8 elements" }
        require(edgePermutation.size == 12) { "Edge permutation must have 12 elements" }
        require(edgeOrientation.size == 12) { "Edge orientation must have 12 elements" }
    }

    fun copy(): CubeState {
        return CubeState(
            cornerPermutation.clone(),
            cornerOrientation.clone(),
            edgePermutation.clone(),
            edgeOrientation.clone()
        )
    }

    /**
     * Apply a move to this cube state and return a new state
     */
    fun applyMove(move: CubeMove): CubeState {
        val newState = copy()

        when (move) {
            CubeMove.U -> applyU(newState)
            CubeMove.U2 -> { applyU(newState); applyU(newState) }
            CubeMove.UP -> { applyU(newState); applyU(newState); applyU(newState) }
            CubeMove.D -> applyD(newState)
            CubeMove.D2 -> { applyD(newState); applyD(newState) }
            CubeMove.DP -> { applyD(newState); applyD(newState); applyD(newState) }
            CubeMove.R -> applyR(newState)
            CubeMove.R2 -> { applyR(newState); applyR(newState) }
            CubeMove.RP -> { applyR(newState); applyR(newState); applyR(newState) }
            CubeMove.L -> applyL(newState)
            CubeMove.L2 -> { applyL(newState); applyL(newState) }
            CubeMove.LP -> { applyL(newState); applyL(newState); applyL(newState) }
            CubeMove.F -> applyF(newState)
            CubeMove.F2 -> { applyF(newState); applyF(newState) }
            CubeMove.FP -> { applyF(newState); applyF(newState); applyF(newState) }
            CubeMove.B -> applyB(newState)
            CubeMove.B2 -> { applyB(newState); applyB(newState) }
            CubeMove.BP -> { applyB(newState); applyB(newState); applyB(newState) }
        }

        return newState
    }

    /**
     * Check if cube is solved
     */
    fun isSolved(): Boolean {
        for (i in 0..7) {
            if (cornerPermutation[i] != i || cornerOrientation[i] != 0) return false
        }
        for (i in 0..11) {
            if (edgePermutation[i] != i || edgeOrientation[i] != 0) return false
        }
        return true
    }

    /**
     * Check if cube is in G1 subgroup (Phase 1 complete)
     * - All edges oriented
     * - 4 middle layer edges in middle layer
     */
    fun isInG1(): Boolean {
        // All edges must be oriented
        for (i in 0..11) {
            if (edgeOrientation[i] != 0) return false
        }

        // Middle layer edges (8, 9, 10, 11) must be in middle layer positions
        val middleEdges = setOf(8, 9, 10, 11)
        for (i in 8..11) {
            if (edgePermutation[i] !in middleEdges) return false
        }

        return true
    }

    /**
     * Verify if the cube state is valid.
     * Checks:
     * 1. Corner orientation sum % 3 == 0
     * 2. Edge orientation sum % 2 == 0
     * 3. Corner permutation is valid (contains 0-7)
     * 4. Edge permutation is valid (contains 0-11)
     * 5. Parity matches (corner parity == edge parity)
     */
    fun isValid(): Boolean {
        // 1. Corner orientation sum
        if (cornerOrientation.sum() % 3 != 0) return false

        // 2. Edge orientation sum
        if (edgeOrientation.sum() % 2 != 0) return false

        // 3. Corner permutation
        val corners = BooleanArray(8)
        for (p in cornerPermutation) {
            if (p < 0 || p > 7) return false
            corners[p] = true
        }
        if (corners.any { !it }) return false

        // 4. Edge permutation
        val edges = BooleanArray(12)
        for (p in edgePermutation) {
            if (p < 0 || p > 11) return false
            edges[p] = true
        }
        if (edges.any { !it }) return false

        // 5. Parity check
        // Calculate corner parity
        var cornerParity = 0
        for (i in 0 until 8) {
            for (j in i + 1 until 8) {
                if (cornerPermutation[i] > cornerPermutation[j]) cornerParity++
            }
        }

        // Calculate edge parity
        var edgeParity = 0
        for (i in 0 until 12) {
            for (j in i + 1 until 12) {
                if (edgePermutation[i] > edgePermutation[j]) edgeParity++
            }
        }

        return (cornerParity % 2) == (edgeParity % 2)
    }

    // Corner indices:
    // 0: URF, 1: UFL, 2: ULB, 3: UBR
    // 4: DFR, 5: DLF, 6: DBL, 7: DRB

    // Edge indices:
    // 0: UR, 1: UF, 2: UL, 3: UB
    // 4: DR, 5: DF, 6: DL, 7: DB
    // 8: FR, 9: FL, 10: BL, 11: BR

    private fun applyU(state: CubeState) {
        // Cycle corners: URF -> UFL -> ULB -> UBR -> URF
        cycleFour(state.cornerPermutation, 0, 1, 2, 3)
        cycleFour(state.cornerOrientation, 0, 1, 2, 3)

        // Cycle edges: UR -> UF -> UL -> UB -> UR
        cycleFour(state.edgePermutation, 0, 1, 2, 3)
        cycleFour(state.edgeOrientation, 0, 1, 2, 3)
    }

    private fun applyD(state: CubeState) {
        // Cycle corners: DFR -> DRB -> DBL -> DLF -> DFR
        cycleFour(state.cornerPermutation, 4, 7, 6, 5)
        cycleFour(state.cornerOrientation, 4, 7, 6, 5)

        // Cycle edges: DR -> DB -> DL -> DF -> DR
        cycleFour(state.edgePermutation, 4, 7, 6, 5)
        cycleFour(state.edgeOrientation, 4, 7, 6, 5)
    }

    private fun applyR(state: CubeState) {
        // Cycle corners: URF -> UBR -> DRB -> DFR -> URF
        cycleFour(state.cornerPermutation, 0, 3, 7, 4)
        // R move changes corner orientation: +1 for URF/DRB, +2 for UBR/DFR
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[0] = (temp[4] + 2) % 3
        state.cornerOrientation[3] = (temp[0] + 1) % 3
        state.cornerOrientation[7] = (temp[3] + 2) % 3
        state.cornerOrientation[4] = (temp[7] + 1) % 3

        // Cycle edges: UR -> BR -> DR -> FR -> UR
        cycleFour(state.edgePermutation, 0, 11, 4, 8)
        cycleFour(state.edgeOrientation, 0, 11, 4, 8)
    }

    private fun applyL(state: CubeState) {
        // Cycle corners: UFL -> DLF -> DBL -> ULB -> UFL
        cycleFour(state.cornerPermutation, 1, 5, 6, 2)
        // L move changes corner orientation: +2 for UFL/DBL, +1 for ULB/DLF
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[1] = (temp[5] + 1) % 3
        state.cornerOrientation[2] = (temp[1] + 2) % 3
        state.cornerOrientation[6] = (temp[2] + 1) % 3
        state.cornerOrientation[5] = (temp[6] + 2) % 3

        // Cycle edges: UL -> BL -> DL -> FL -> UL
        cycleFour(state.edgePermutation, 2, 10, 6, 9)
        cycleFour(state.edgeOrientation, 2, 10, 6, 9)
    }

    private fun applyF(state: CubeState) {
        // Cycle corners: URF -> DFR -> DLF -> UFL -> URF
        cycleFour(state.cornerPermutation, 0, 4, 5, 1)
        // F move changes corner orientation: +1 for URF/DLF, +2 for UFL/DFR
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[0] = (temp[1] + 1) % 3
        state.cornerOrientation[1] = (temp[5] + 2) % 3
        state.cornerOrientation[5] = (temp[4] + 1) % 3
        state.cornerOrientation[4] = (temp[0] + 2) % 3

        // Cycle edges: UF -> FL -> DF -> FR -> UF (and flip them)
        cycleFour(state.edgePermutation, 1, 9, 5, 8)
        val tempEdge = state.edgeOrientation.clone()
        state.edgeOrientation[1] = (tempEdge[8] + 1) % 2
        state.edgeOrientation[8] = (tempEdge[5] + 1) % 2
        state.edgeOrientation[5] = (tempEdge[9] + 1) % 2
        state.edgeOrientation[9] = (tempEdge[1] + 1) % 2
    }

    private fun applyB(state: CubeState) {
        // Cycle corners: UBR -> ULB -> DBL -> DRB -> UBR
        cycleFour(state.cornerPermutation, 3, 2, 6, 7)
        // B move changes corner orientation: +1 for UBR/DBL, +2 for ULB/DRB
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[3] = (temp[7] + 2) % 3
        state.cornerOrientation[2] = (temp[3] + 1) % 3
        state.cornerOrientation[6] = (temp[2] + 2) % 3
        state.cornerOrientation[7] = (temp[6] + 1) % 3

        // Cycle edges: UB -> BL -> DB -> BR -> UB (and flip them)
        cycleFour(state.edgePermutation, 3, 10, 7, 11)
        val tempEdge = state.edgeOrientation.clone()
        state.edgeOrientation[3] = (tempEdge[11] + 1) % 2
        state.edgeOrientation[10] = (tempEdge[3] + 1) % 2
        state.edgeOrientation[7] = (tempEdge[10] + 1) % 2
        state.edgeOrientation[11] = (tempEdge[7] + 1) % 2
    }

    private fun cycleFour(arr: IntArray, a: Int, b: Int, c: Int, d: Int) {
        val temp = arr[a]
        arr[a] = arr[d]
        arr[d] = arr[c]
        arr[c] = arr[b]
        arr[b] = temp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CubeState) return false
        return cornerPermutation.contentEquals(other.cornerPermutation) &&
                cornerOrientation.contentEquals(other.cornerOrientation) &&
                edgePermutation.contentEquals(other.edgePermutation) &&
                edgeOrientation.contentEquals(other.edgeOrientation)
    }

    override fun hashCode(): Int {
        var result = cornerPermutation.contentHashCode()
        result = 31 * result + cornerOrientation.contentHashCode()
        result = 31 * result + edgePermutation.contentHashCode()
        result = 31 * result + edgeOrientation.contentHashCode()
        return result
    }

    companion object {
        private val cornerColorTable = arrayOf(
            intArrayOf(5, 2, 0), // URF
            intArrayOf(5, 0, 3), // UFL
            intArrayOf(5, 3, 4), // ULB
            intArrayOf(5, 4, 2), // UBR
            intArrayOf(1, 0, 2), // DFR
            intArrayOf(1, 3, 0), // DLF
            intArrayOf(1, 4, 3), // DBL
            intArrayOf(1, 2, 4)  // DRB
        )

        // Edge definitions (color pairs)
        private val edgeColorTable = arrayOf(
            intArrayOf(5, 2), // UR
            intArrayOf(5, 0), // UF
            intArrayOf(5, 3), // UL
            intArrayOf(5, 4), // UB
            intArrayOf(1, 2), // DR
            intArrayOf(1, 0), // DF
            intArrayOf(1, 3), // DL
            intArrayOf(1, 4), // DB
            intArrayOf(0, 2), // FR
            intArrayOf(0, 3), // FL
            intArrayOf(4, 3), // BL
            intArrayOf(4, 2)  // BR
        )

        /**
         * Extract a corner’s 3 colors from a given position (x,y,z)
         */
        private fun getCornerColors(c: Cube, x: Int, y: Int, z: Int): IntArray {
            val colors = mutableListOf<Int>()

            if (z == +1) colors += c.s1[1 - y][x + 1] // front
            if (z == -1) colors += c.s6[1 - y][1 - (x + 1)] // back (mirrored X)

            if (y == +1) colors += c.s2[1 - z][x + 1] // top
            if (y == -1) colors += c.s3[z + 1][x + 1] // bottom

            if (x == -1) colors += c.s4[1 - y][1 - z] // left
            if (x == +1) colors += c.s5[1 - y][z + 1] // right

            return colors.toIntArray()
        }

        /**
         * Extract an edge’s 2 colors
         */
        private fun getEdgeColors(c: Cube, x: Int, y: Int, z: Int): IntArray {
            val colors = mutableListOf<Int>()

            if (z == +1) colors += c.s1[1 - y][x + 1]
            if (z == -1) colors += c.s6[1 - y][1 - (x + 1)]

            if (y == +1) colors += c.s2[1 - z][x + 1]
            if (y == -1) colors += c.s3[z + 1][x + 1]

            if (x == -1) colors += c.s4[1 - y][1 - z]
            if (x == +1) colors += c.s5[1 - y][z + 1]

            return colors.toIntArray()
        }

        /**
         * Convert from the app’s Cube representation to Kociemba’s coordinate system.
         */
        fun fromCube(c: Cube): CubeState {
            val cp = IntArray(8)
            val co = IntArray(8)
            val ep = IntArray(12)
            val eo = IntArray(12)

            // Corner coordinates (Kociemba order)
            val cornerPositions = arrayOf(
                intArrayOf(+1, +1, +1), // URF
                intArrayOf(-1, +1, +1), // UFL
                intArrayOf(-1, +1, -1), // ULB
                intArrayOf(+1, +1, -1), // UBR
                intArrayOf(+1, -1, +1), // DFR
                intArrayOf(-1, -1, +1), // DLF
                intArrayOf(-1, -1, -1), // DBL
                intArrayOf(+1, -1, -1)  // DRB
            )

            // IDENTIFY CORNER PERMUTATION + ORIENTATION
            for (i in 0 until 8) {
                val (x, y, z) = cornerPositions[i]
                val colors = getCornerColors(c, x, y, z) // always gives 3 colors

                val sorted = colors.sorted()
                val id = cornerColorTable.indexOfFirst { it.sorted() == sorted }
                cp[i] = id

                // Correct corner orientation
                val corner = cornerColorTable[id]

                val uColor = 5  // Yellow = U
                val dColor = 1  // Red    = D

                val idxUorD = colors.indexOfFirst { it == uColor || it == dColor }

                // Orientation depends on which axis the U/D color belongs on
                co[i] = when {
                    colors[idxUorD] == corner[0] -> 0
                    colors[idxUorD] == corner[1] -> 1
                    else -> 2
                }
            }

            // Edge coordinates (Kociemba order)
            val edgePositions = arrayOf(
                intArrayOf(+1, +1, 0), // UR
                intArrayOf(0, +1, +1), // UF
                intArrayOf(-1, +1, 0), // UL
                intArrayOf(0, +1, -1), // UB
                intArrayOf(+1, -1, 0), // DR
                intArrayOf(0, -1, +1), // DF
                intArrayOf(-1, -1, 0), // DL
                intArrayOf(0, -1, -1), // DB
                intArrayOf(+1, 0, +1), // FR
                intArrayOf(-1, 0, +1), // FL
                intArrayOf(-1, 0, -1), // BL
                intArrayOf(+1, 0, -1)  // BR
            )

            // IDENTIFY EDGE PERMUTATION + ORIENTATION
            for (i in 0 until 12) {
                val (x, y, z) = edgePositions[i]
                val colors = getEdgeColors(c, x, y, z)

                val sorted = colors.sorted()
                val id = edgeColorTable.indexOfFirst { it.sorted() == sorted }
                ep[i] = id

                val edge = edgeColorTable[id]

                val uColor = 5
                val dColor = 1
                val fColor = 0
                val bColor = 4

                eo[i] = when {
                    // U/D edges
                    (i <= 3 || i in 4..7) -> {
                        if (colors.indexOf(uColor) == 0 || colors.indexOf(dColor) == 0) 0 else 1
                    }
                    // F/B edges
                    else -> {
                        if (colors.indexOf(fColor) == 0 || colors.indexOf(bColor) == 0) 0 else 1
                    }
                }
            }

            return CubeState(cp, co, ep, eo)
        }
    }
}
