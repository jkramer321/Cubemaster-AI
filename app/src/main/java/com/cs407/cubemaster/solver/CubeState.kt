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

    // Corner indices:
    // 0: URF, 1: UFL, 2: ULB, 3: UBR
    // 4: DFR, 5: DLF, 6: DBL, 7: DRB

    // Edge indices:
    // 0: UR, 1: UF, 2: UL, 3: UB
    // 4: DR, 5: DF, 6: DL, 7: DB
    // 8: FR, 9: FL, 10: BL, 11: BR

    private fun applyU(state: CubeState) {
        // Cycle corners: URF -> UBR -> ULB -> UFL -> URF
        cycleFour(state.cornerPermutation, 0, 3, 2, 1)
        cycleFour(state.cornerOrientation, 0, 3, 2, 1)

        // Cycle edges: UR -> UB -> UL -> UF -> UR
        cycleFour(state.edgePermutation, 0, 3, 2, 1)
        cycleFour(state.edgeOrientation, 0, 3, 2, 1)
    }

    private fun applyD(state: CubeState) {
        // Cycle corners: DFR -> DLF -> DBL -> DRB -> DFR
        cycleFour(state.cornerPermutation, 4, 5, 6, 7)
        cycleFour(state.cornerOrientation, 4, 5, 6, 7)

        // Cycle edges: DR -> DF -> DL -> DB -> DR
        cycleFour(state.edgePermutation, 4, 5, 6, 7)
        cycleFour(state.edgeOrientation, 4, 5, 6, 7)
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
        // Cycle corners: UFL -> ULB -> DBL -> DLF -> UFL
        cycleFour(state.cornerPermutation, 1, 2, 6, 5)
        // L move changes corner orientation: +2 for UFL/DBL, +1 for ULB/DLF
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[1] = (temp[5] + 1) % 3
        state.cornerOrientation[2] = (temp[1] + 2) % 3
        state.cornerOrientation[6] = (temp[2] + 1) % 3
        state.cornerOrientation[5] = (temp[6] + 2) % 3

        // Cycle edges: UL -> FL -> DL -> BL -> UL
        cycleFour(state.edgePermutation, 2, 9, 6, 10)
        cycleFour(state.edgeOrientation, 2, 9, 6, 10)
    }

    private fun applyF(state: CubeState) {
        // Cycle corners: URF -> UFL -> DLF -> DFR -> URF
        cycleFour(state.cornerPermutation, 0, 1, 5, 4)
        // F move changes corner orientation: +1 for URF/DLF, +2 for UFL/DFR
        val temp = state.cornerOrientation.clone()
        state.cornerOrientation[0] = (temp[4] + 1) % 3
        state.cornerOrientation[1] = (temp[0] + 2) % 3
        state.cornerOrientation[5] = (temp[1] + 1) % 3
        state.cornerOrientation[4] = (temp[5] + 2) % 3

        // Cycle edges: UF -> FR -> DF -> FL -> UF (and flip them)
        cycleFour(state.edgePermutation, 1, 8, 5, 9)
        val tempEdge = state.edgeOrientation.clone()
        state.edgeOrientation[1] = (tempEdge[9] + 1) % 2
        state.edgeOrientation[8] = (tempEdge[1] + 1) % 2
        state.edgeOrientation[5] = (tempEdge[8] + 1) % 2
        state.edgeOrientation[9] = (tempEdge[5] + 1) % 2
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
        /**
         * Create a solved cube state
         */
        fun solved(): CubeState {
            return CubeState(
                cornerPermutation = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7),
                cornerOrientation = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
                edgePermutation = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11),
                edgeOrientation = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
        }

        /**
         * Convert from the app's Cube representation to Kociemba's coordinate system
         *
         * The app uses:
         * s1: Front, s2: Top, s3: Bottom, s4: Left, s5: Right, s6: Back
         *
         * We need to map the facelets to corner/edge pieces and their orientations
         */
        fun fromCube(cube: Cube): CubeState {
            // This is a complex mapping that needs to read the cube's facelets
            // and determine which piece is in each position and its orientation

            // For now, return a placeholder - will implement the full conversion
            // This requires identifying each piece by its color combination
            return solved()
        }
    }
}
