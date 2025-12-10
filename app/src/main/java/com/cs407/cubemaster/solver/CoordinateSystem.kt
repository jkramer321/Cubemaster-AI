package com.cs407.cubemaster.solver

/**
 * Coordinate system for Kociemba's two-phase algorithm.
 *
 * Phase 1 coordinates:
 * - Twist: Corner orientation coordinate (0..2186, which is 3^7)
 * - Flip: Edge orientation coordinate (0..2047, which is 2^11)
 * - Slice: Position of 4 middle-layer edges (0..494, which is C(12,4))
 *
 * Phase 2 coordinates:
 * - Corner permutation (0..40319, which is 8!)
 * - Edge permutation of 8 U/D edges (0..40319, which is 8!)
 * - Slice sorted: permutation of 4 middle-layer edges (0..23, which is 4!)
 */
object CoordinateSystem {

    // Phase 1: Corner twist coordinate (0..2186)
    fun getTwistCoordinate(state: CubeState): Int {
        var coord = 0
        for (i in 0..6) {
            coord = 3 * coord + state.cornerOrientation[i]
        }
        return coord
    }

    fun setTwistCoordinate(state: CubeState, coord: Int) {
        var remaining = coord
        var sum = 0
        for (i in 6 downTo 0) {
            state.cornerOrientation[i] = remaining % 3
            sum += state.cornerOrientation[i]
            remaining /= 3
        }
        state.cornerOrientation[7] = (3 - (sum % 3)) % 3
    }

    // Phase 1: Edge flip coordinate (0..2047)
    fun getFlipCoordinate(state: CubeState): Int {
        var coord = 0
        for (i in 0..10) {
            coord = 2 * coord + state.edgeOrientation[i]
        }
        return coord
    }

    fun setFlipCoordinate(state: CubeState, coord: Int) {
        var remaining = coord
        var sum = 0
        for (i in 10 downTo 0) {
            state.edgeOrientation[i] = remaining % 2
            sum += state.edgeOrientation[i]
            remaining /= 2
        }
        state.edgeOrientation[11] = (2 - (sum % 2)) % 2
    }

    // Phase 1: Slice coordinate - which 4 edges are in middle layer (0..494)
    fun getSliceCoordinate(state: CubeState): Int {
        var coord = 0
        var k = 3
        for (i in 11 downTo 0) {
            if (state.edgePermutation[i] in 8..11) {
                coord += binomial(i, k)
                k--
            }
        }
        return coord
    }

    // Phase 2: Corner permutation coordinate (0..40319)
    fun getCornerPermutationCoordinate(state: CubeState): Int {
        return permutationToIndex(state.cornerPermutation)
    }

    fun setCornerPermutationCoordinate(state: CubeState, coord: Int) {
        indexToPermutation(coord, state.cornerPermutation)
    }

    // Phase 2: UD edge permutation (0..40319)
    // Only considers the 8 edges in U and D layers
    fun getUDEdgePermutationCoordinate(state: CubeState): Int {
        val udEdges = IntArray(8)
        var idx = 0
        for (i in 0..11) {
            val edge = state.edgePermutation[i]
            if (edge < 8) {
                udEdges[idx++] = edge
            }
        }
        return permutationToIndex(udEdges)
    }

    // Phase 2: Slice sorted coordinate (0..23)
    // Permutation of the 4 middle-layer edges within themselves
    fun getSliceSortedCoordinate(state: CubeState): Int {
        val sliceEdges = IntArray(4)
        var idx = 0
        for (i in 0..11) {
            val edge = state.edgePermutation[i]
            if (edge in 8..11) {
                sliceEdges[idx++] = edge - 8
            }
        }
        return permutationToIndex(sliceEdges)
    }

    /**
     * Convert permutation to index using Lehmer code
     */
    private fun permutationToIndex(perm: IntArray): Int {
        val n = perm.size
        var index = 0
        for (i in 0 until n - 1) {
            var smaller = 0
            for (j in i + 1 until n) {
                if (perm[j] < perm[i]) smaller++
            }
            index = index * (n - i) + smaller
        }
        return index
    }

    /**
     * Convert index to permutation using Lehmer code
     */
    private fun indexToPermutation(index: Int, result: IntArray) {
        val n = result.size
        val available = (0 until n).toMutableList()
        var remaining = index

        for (i in 0 until n) {
            val factorial = factorial(n - 1 - i)
            val pos = remaining / factorial
            result[i] = available[pos]
            available.removeAt(pos)
            remaining %= factorial
        }
    }

    /**
     * Binomial coefficient C(n, k)
     */
    private fun binomial(n: Int, k: Int): Int {
        if (k < 0 || k > n) return 0
        if (k == 0 || k == n) return 1

        var result = 1
        for (i in 0 until k) {
            result = result * (n - i) / (i + 1)
        }
        return result
    }

    /**
     * Factorial
     */
    private fun factorial(n: Int): Int {
        if (n <= 1) return 1
        var result = 1
        for (i in 2..n) {
            result *= i
        }
        return result
    }

    /**
     * Coordinate for Phase 1 search
     */
    data class Phase1Coordinate(
        val twist: Int,
        val flip: Int,
        val slice: Int
    ) {
        companion object {
            fun from(state: CubeState): Phase1Coordinate {
                return Phase1Coordinate(
                    getTwistCoordinate(state),
                    getFlipCoordinate(state),
                    getSliceCoordinate(state)
                )
            }
        }
    }

    /**
     * Coordinate for Phase 2 search
     */
    data class Phase2Coordinate(
        val cornerPerm: Int,
        val udEdgePerm: Int,
        val sliceSorted: Int
    ) {
        companion object {
            fun from(state: CubeState): Phase2Coordinate {
                return Phase2Coordinate(
                    getCornerPermutationCoordinate(state),
                    getUDEdgePermutationCoordinate(state),
                    getSliceSortedCoordinate(state)
                )
            }
        }
    }
}
