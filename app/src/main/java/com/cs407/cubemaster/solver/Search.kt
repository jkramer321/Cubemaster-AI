package com.cs407.cubemaster.solver

/**
 * IDA* search algorithm for both phases of Kociemba's algorithm
 */
object Search {

    /**
     * Search for Phase 1 solution (get to G1 subgroup)
     * Returns a list of moves, or null if no solution found within max depth
     */
    fun searchPhase1(
        state: CubeState,
        maxDepth: Int = 12
    ): List<CubeMove>? {
        if (state.isInG1()) return emptyList()

        val coord = CoordinateSystem.Phase1Coordinate.from(state)
        val solution = mutableListOf<CubeMove>()

        // IDA*: iteratively increase depth limit
        for (depth in 1..maxDepth) {
            solution.clear()
            if (searchPhase1Recursive(coord, 0, depth, solution, null)) {
                return solution
            }
        }

        return null // No solution found
    }

    private fun searchPhase1Recursive(
        coord: CoordinateSystem.Phase1Coordinate,
        currentDepth: Int,
        maxDepth: Int,
        solution: MutableList<CubeMove>,
        lastMove: CubeMove?
    ): Boolean {
        // Check if we've reached G1 subgroup
        if (coord.twist == 0 && coord.flip == 0 && coord.slice == CoordinateSystem.SOLVED_SLICE_COORDINATE) {
            return true
        }

        // Prune if we can't reach goal in remaining moves
        val estimate = PruningTables.getPhase1Distance(coord)
        if (currentDepth + estimate > maxDepth) {
            return false
        }

        // Try all moves
        for (move in CubeMove.values()) {
            // Don't do same face twice in a row, or opposite faces out of order
            if (shouldSkipMove(move, lastMove)) continue

            val newCoord = MoveTables.applyMovePhase1(coord, move)
            solution.add(move)

            if (searchPhase1Recursive(newCoord, currentDepth + 1, maxDepth, solution, move)) {
                return true
            }

            solution.removeAt(solution.size - 1)
        }

        return false
    }

    /**
     * Search for Phase 2 solution (solve within G1 subgroup)
     * Returns a list of moves, or null if no solution found within max depth
     */
    fun searchPhase2(
        state: CubeState,
        maxDepth: Int = 18
    ): List<CubeMove>? {
        if (state.isSolved()) return emptyList()

        val coord = CoordinateSystem.Phase2Coordinate.from(state)
        val solution = mutableListOf<CubeMove>()

        // IDA*: iteratively increase depth limit
        for (depth in 1..maxDepth) {
            solution.clear()
            if (searchPhase2Recursive(coord, 0, depth, solution, null)) {
                return solution
            }
        }

        return null
    }

    private fun searchPhase2Recursive(
        coord: CoordinateSystem.Phase2Coordinate,
        currentDepth: Int,
        maxDepth: Int,
        solution: MutableList<CubeMove>,
        lastMove: CubeMove?
    ): Boolean {
        // Check if solved
        if (coord.cornerPerm == 0 && coord.udEdgePerm == 0 && coord.sliceSorted == 0) {
            return true
        }

        // Prune if we can't reach goal in remaining moves
        val estimate = PruningTables.getPhase2Distance(coord)
        if (currentDepth + estimate > maxDepth) {
            return false
        }

        // Try only Phase 2 moves
        for (move in CubeMove.PHASE2_MOVES) {
            if (shouldSkipMove(move, lastMove)) continue

            val newCoord = MoveTables.applyMovePhase2(coord, move)
            solution.add(move)

            if (searchPhase2Recursive(newCoord, currentDepth + 1, maxDepth, solution, move)) {
                return true
            }

            solution.removeAt(solution.size - 1)
        }

        return false
    }

    /**
     * Determine if a move should be skipped based on the last move
     * Skip redundant moves like R R, R R', or out-of-order opposite faces
     */
    private fun shouldSkipMove(move: CubeMove, lastMove: CubeMove?): Boolean {
        if (lastMove == null) return false

        val moveFace = getFace(move)
        val lastFace = getFace(lastMove)

        // Don't do same face twice in a row
        if (moveFace == lastFace) return true

        // Don't do opposite faces out of canonical order
        // Canonical order: (U before D), (R before L), (F before B)
        val opposites = mapOf(
            'D' to 'U',
            'L' to 'R',
            'B' to 'F'
        )

        if (opposites[moveFace] == lastFace) return true

        return false
    }

    private fun getFace(move: CubeMove): Char {
        return move.notation[0]
    }

    /**
     * Complete two-phase solution
     */
    fun solve(state: CubeState): List<CubeMove>? {
        // Phase 1: Get to G1 subgroup
        val phase1Solution = searchPhase1(state) ?: return null

        // Apply Phase 1 moves to get intermediate state
        var intermediateState = state
        for (move in phase1Solution) {
            intermediateState = intermediateState.applyMove(move)
        }

        // Phase 2: Solve within G1
        val phase2Solution = searchPhase2(intermediateState) ?: return null

        // Combine solutions
        return phase1Solution + phase2Solution
    }

    /**
     * Format solution as readable string
     */
    fun formatSolution(moves: List<CubeMove>): String {
        return moves.joinToString(" ") { it.notation }
    }

    /**
     * Simplify solution by combining consecutive moves
     * e.g., R R R -> R', R R -> R2
     */
    fun simplifySolution(moves: List<CubeMove>): List<CubeMove> {
        if (moves.isEmpty()) return moves

        val simplified = mutableListOf<CubeMove>()
        var i = 0

        while (i < moves.size) {
            val move = moves[i]
            val face = getFace(move)

            // Count consecutive moves on same face
            var count = getMoveTurns(move)
            var j = i + 1

            while (j < moves.size && getFace(moves[j]) == face) {
                count += getMoveTurns(moves[j])
                j++
            }

            // Normalize to 0-3 quarter turns
            count %= 4

            // Add simplified move
            when (count) {
                1 -> simplified.add(getSingleMove(face))
                2 -> simplified.add(getDoubleMove(face))
                3 -> simplified.add(getPrimeMove(face))
                // 0 -> skip (no move needed)
            }

            i = j
        }

        return simplified
    }

    private fun getMoveTurns(move: CubeMove): Int {
        return when {
            move.notation.endsWith("2") -> 2
            move.notation.endsWith("'") -> 3
            else -> 1
        }
    }

    private fun getSingleMove(face: Char): CubeMove {
        return CubeMove.values().first { it.notation == face.toString() }
    }

    private fun getDoubleMove(face: Char): CubeMove {
        return CubeMove.values().first { it.notation == "${face}2" }
    }

    private fun getPrimeMove(face: Char): CubeMove {
        return CubeMove.values().first { it.notation == "${face}'" }
    }
}
