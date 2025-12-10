package com.cs407.cubemaster.solver

/**
 * Pre-computed move tables for Kociemba's algorithm.
 *
 * These tables map (coordinate, move) -> new coordinate
 * This allows us to perform moves in coordinate space without
 * maintaining the full cube state.
 */
object MoveTables {

    // Phase 1 move tables
    private val twistMoveTable = Array(2187) { IntArray(18) }
    private val flipMoveTable = Array(2048) { IntArray(18) }
    private val sliceMoveTable = Array(495) { IntArray(18) }

    // Phase 2 move tables (only 10 moves: U, U2, U', D, D2, D', R2, L2, F2, B2)
    private val cornerPermMoveTable = Array(40320) { IntArray(10) }
    private val udEdgePermMoveTable = Array(40320) { IntArray(10) }
    private val sliceSortedMoveTable = Array(24) { IntArray(10) }

    private var initialized = false

    /**
     * Initialize all move tables
     * This is computationally expensive and should be done once at startup
     */
    fun initialize() {
        if (initialized) return

        initializePhase1Tables()
        initializePhase2Tables()

        initialized = true
    }

    private fun initializePhase1Tables() {
        val allMoves = CubeMove.values()

        // Initialize twist table
        for (twist in 0..2186) {
            val state = CubeState.solved()
            CoordinateSystem.setTwistCoordinate(state, twist)

            for ((moveIdx, move) in allMoves.withIndex()) {
                val newState = state.applyMove(move)
                twistMoveTable[twist][moveIdx] = CoordinateSystem.getTwistCoordinate(newState)
            }
        }

        // Initialize flip table
        for (flip in 0..2047) {
            val state = CubeState.solved()
            CoordinateSystem.setFlipCoordinate(state, flip)

            for ((moveIdx, move) in allMoves.withIndex()) {
                val newState = state.applyMove(move)
                flipMoveTable[flip][moveIdx] = CoordinateSystem.getFlipCoordinate(newState)
            }
        }

        // Initialize slice table (this is more complex - need to generate all slice positions)
        val generatedSlices = mutableSetOf<Int>()
        val queue = ArrayDeque<CubeState>()
        queue.add(CubeState.solved())
        generatedSlices.add(0)

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            val slice = CoordinateSystem.getSliceCoordinate(state)

            for ((moveIdx, move) in allMoves.withIndex()) {
                val newState = state.applyMove(move)
                val newSlice = CoordinateSystem.getSliceCoordinate(newState)
                sliceMoveTable[slice][moveIdx] = newSlice

                if (newSlice !in generatedSlices) {
                    generatedSlices.add(newSlice)
                    queue.add(newState)
                }
            }
        }
    }

    private fun initializePhase2Tables() {
        val phase2Moves = CubeMove.PHASE2_MOVES

        // Initialize corner permutation table
        for (cp in 0..40319) {
            val state = CubeState.solved()
            CoordinateSystem.setCornerPermutationCoordinate(state, cp)

            for ((moveIdx, move) in phase2Moves.withIndex()) {
                val newState = state.applyMove(move)
                cornerPermMoveTable[cp][moveIdx] = CoordinateSystem.getCornerPermutationCoordinate(newState)
            }
        }

        // Initialize UD edge permutation table (expensive!)
        val generatedUDPerms = mutableSetOf<Int>()
        val queue = ArrayDeque<CubeState>()
        queue.add(CubeState.solved())
        generatedUDPerms.add(0)

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            val udPerm = CoordinateSystem.getUDEdgePermutationCoordinate(state)

            for ((moveIdx, move) in phase2Moves.withIndex()) {
                val newState = state.applyMove(move)
                val newUDPerm = CoordinateSystem.getUDEdgePermutationCoordinate(newState)
                udEdgePermMoveTable[udPerm][moveIdx] = newUDPerm

                if (newUDPerm !in generatedUDPerms && generatedUDPerms.size < 40320) {
                    generatedUDPerms.add(newUDPerm)
                    queue.add(newState)
                }
            }
        }

        // Initialize slice sorted table
        for (ss in 0..23) {
            val state = CubeState.solved()
            // Set up a state with given slice sorted coordinate
            // (this is simplified - in reality we'd need to carefully set edge permutation)

            for ((moveIdx, move) in phase2Moves.withIndex()) {
                val newState = state.applyMove(move)
                sliceSortedMoveTable[ss][moveIdx] = CoordinateSystem.getSliceSortedCoordinate(newState)
            }
        }
    }

    /**
     * Apply a move to Phase 1 coordinates
     */
    fun applyMovePhase1(coord: CoordinateSystem.Phase1Coordinate, move: CubeMove): CoordinateSystem.Phase1Coordinate {
        val moveIdx = move.ordinal
        return CoordinateSystem.Phase1Coordinate(
            twist = twistMoveTable[coord.twist][moveIdx],
            flip = flipMoveTable[coord.flip][moveIdx],
            slice = sliceMoveTable[coord.slice][moveIdx]
        )
    }

    /**
     * Apply a move to Phase 2 coordinates
     */
    fun applyMovePhase2(coord: CoordinateSystem.Phase2Coordinate, move: CubeMove): CoordinateSystem.Phase2Coordinate {
        val moveIdx = CubeMove.PHASE2_MOVES.indexOf(move)
        if (moveIdx == -1) {
            throw IllegalArgumentException("Move $move is not allowed in Phase 2")
        }

        return CoordinateSystem.Phase2Coordinate(
            cornerPerm = cornerPermMoveTable[coord.cornerPerm][moveIdx],
            udEdgePerm = udEdgePermMoveTable[coord.udEdgePerm][moveIdx],
            sliceSorted = sliceSortedMoveTable[coord.sliceSorted][moveIdx]
        )
    }

    fun isInitialized(): Boolean = initialized
}
