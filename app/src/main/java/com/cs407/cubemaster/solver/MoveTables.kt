package com.cs407.cubemaster.solver

import java.io.DataInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

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

    fun forceInitialize() {
        initialized = false
        initialize()
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
        val solvedState = CubeState.solved()
        queue.add(solvedState)
        generatedSlices.add(CoordinateSystem.getSliceCoordinate(solvedState))

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

        // Initialize slice sorted table using BFS to generate all reachable states
        val generatedSliceCoords = mutableSetOf<Int>()
        val sliceQueue = ArrayDeque<CubeState>()
        val solvedState = CubeState.solved()
        sliceQueue.add(solvedState)
        generatedSliceCoords.add(0)

        // Fill table for solved state first
        for ((moveIdx, move) in phase2Moves.withIndex()) {
            val newState = solvedState.applyMove(move)
            sliceSortedMoveTable[0][moveIdx] = CoordinateSystem.getSliceSortedCoordinate(newState)
        }

        while (sliceQueue.isNotEmpty() && generatedSliceCoords.size < 24) {
            val state = sliceQueue.removeFirst()
            val ss = CoordinateSystem.getSliceSortedCoordinate(state)

            for ((moveIdx, move) in phase2Moves.withIndex()) {
                val newState = state.applyMove(move)
                val newSS = CoordinateSystem.getSliceSortedCoordinate(newState)

                sliceSortedMoveTable[ss][moveIdx] = newSS

                if (newSS !in generatedSliceCoords && generatedSliceCoords.size < 24) {
                    generatedSliceCoords.add(newSS)
                    sliceQueue.add(newState)
                }
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

    /**
     * Load tables from a compressed binary file
     * File format matches TableGenerator.saveTables()
     */
    fun loadFromStream(inputStream: InputStream): Boolean {
        try {
            DataInputStream(GZIPInputStream(inputStream)).use { input ->
                // Read and verify magic number
                val magic = input.readInt()
                if (magic != 0x4B4F4349) { // "KOCI"
                    return false
                }
                
                // Read version
                val version = input.readInt()
                if (version != 1) {
                    return false
                }
                
                return loadFromStreamInternal(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Internal method to load move tables from an already-opened DataInputStream
     * Assumes magic number and version have already been read
     */
    internal fun loadFromStreamInternal(input: DataInputStream): Boolean {
        try {
            // === Move Tables ===
            
            SolverLog.d("MoveTables", "Loading twist move table...")
            // Twist move table [2187][18]
            val twistRows = input.readInt()
            val twistCols = input.readInt()
            if (twistRows != 2187 || twistCols != 18) {
                SolverLog.e("MoveTables", "Invalid twist table dimensions: $twistRows x $twistCols, expected 2187 x 18")
                return false
            }
            for (i in 0 until twistRows) {
                for (j in 0 until twistCols) {
                    twistMoveTable[i][j] = input.readShort().toInt()
                }
            }
            SolverLog.d("MoveTables", "Twist move table loaded")
            
            SolverLog.d("MoveTables", "Loading flip move table...")
            // Flip move table [2048][18]
            val flipRows = input.readInt()
            val flipCols = input.readInt()
            if (flipRows != 2048 || flipCols != 18) {
                SolverLog.e("MoveTables", "Invalid flip table dimensions: $flipRows x $flipCols, expected 2048 x 18")
                return false
            }
            for (i in 0 until flipRows) {
                for (j in 0 until flipCols) {
                    flipMoveTable[i][j] = input.readShort().toInt()
                }
            }
            SolverLog.d("MoveTables", "Flip move table loaded")
            
            SolverLog.d("MoveTables", "Loading slice move table...")
            // Slice move table [495][18]
            val sliceRows = input.readInt()
            val sliceCols = input.readInt()
            if (sliceRows != 495 || sliceCols != 18) {
                SolverLog.e("MoveTables", "Invalid slice table dimensions: $sliceRows x $sliceCols, expected 495 x 18")
                return false
            }
            for (i in 0 until sliceRows) {
                for (j in 0 until sliceCols) {
                    sliceMoveTable[i][j] = input.readShort().toInt()
                }
            }
            SolverLog.d("MoveTables", "Slice move table loaded")
            
            SolverLog.d("MoveTables", "Loading corner perm move table...")
            // Corner perm move table [40320][10]
            val cornerPermRows = input.readInt()
            val cornerPermCols = input.readInt()
            if (cornerPermRows != 40320 || cornerPermCols != 10) {
                SolverLog.e("MoveTables", "Invalid corner perm table dimensions: $cornerPermRows x $cornerPermCols, expected 40320 x 10")
                return false
            }
            for (i in 0 until cornerPermRows) {
                for (j in 0 until cornerPermCols) {
                    cornerPermMoveTable[i][j] = input.readInt()
                }
            }
            SolverLog.d("MoveTables", "Corner perm move table loaded")
            
            SolverLog.d("MoveTables", "Loading UD edge perm move table...")
            // UD edge perm move table [40320][10]
            val udEdgePermRows = input.readInt()
            val udEdgePermCols = input.readInt()
            if (udEdgePermRows != 40320 || udEdgePermCols != 10) {
                SolverLog.e("MoveTables", "Invalid UD edge perm table dimensions: $udEdgePermRows x $udEdgePermCols, expected 40320 x 10")
                return false
            }
            for (i in 0 until udEdgePermRows) {
                for (j in 0 until udEdgePermCols) {
                    udEdgePermMoveTable[i][j] = input.readInt()
                }
            }
            SolverLog.d("MoveTables", "UD edge perm move table loaded")
            
            SolverLog.d("MoveTables", "Loading slice sorted move table...")
            // Slice sorted move table [24][10]
            val sliceSortedRows = input.readInt()
            val sliceSortedCols = input.readInt()
            if (sliceSortedRows != 24 || sliceSortedCols != 10) {
                SolverLog.e("MoveTables", "Invalid slice sorted table dimensions: $sliceSortedRows x $sliceSortedCols, expected 24 x 10")
                return false
            }
            for (i in 0 until sliceSortedRows) {
                for (j in 0 until sliceSortedCols) {
                    sliceSortedMoveTable[i][j] = input.readShort().toInt()
                }
            }
            SolverLog.d("MoveTables", "Slice sorted move table loaded")
            
            initialized = true
            SolverLog.d("MoveTables", "All move tables loaded successfully")
            return true
        } catch (e: Exception) {
            SolverLog.e("MoveTables", "Exception loading move tables: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Getter methods for TableGenerator compatibility
     */
    fun getTwistMoveTable(): Array<IntArray> = twistMoveTable
    fun getFlipMoveTable(): Array<IntArray> = flipMoveTable
    fun getSliceMoveTable(): Array<IntArray> = sliceMoveTable
    fun getCornerPermMoveTable(): Array<IntArray> = cornerPermMoveTable
    fun getUDEdgePermMoveTable(): Array<IntArray> = udEdgePermMoveTable
    fun getSliceSortedMoveTable(): Array<IntArray> = sliceSortedMoveTable
}
