package com.cs407.cubemaster.solver

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Pre-computed move tables for Kociemba's algorithm.
 *
 * These tables map (coordinate, move) -> new coordinate
 * This allows us to perform moves in coordinate space without
 * maintaining the full cube state.
 */
object MoveTables {

    private const val MOVE_TABLE_MAGIC = 0x4D54424C // 'MTBL'
    private const val MOVE_TABLE_VERSION = 1

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

    fun exportBinary(out: OutputStream, compress: Boolean = true) {
        if (!initialized) {
            initialize()
        }
        val stream: OutputStream = if (compress) {
            GZIPOutputStream(BufferedOutputStream(out))
        } else {
            BufferedOutputStream(out)
        }
        DataOutputStream(stream).use { data ->
            data.writeInt(MOVE_TABLE_MAGIC)
            data.writeInt(MOVE_TABLE_VERSION)

            writeTable(data, twistMoveTable)
            writeTable(data, flipMoveTable)
            writeTable(data, sliceMoveTable)
            writeTable(data, cornerPermMoveTable)
            writeTable(data, udEdgePermMoveTable)
            writeTable(data, sliceSortedMoveTable)
        }
    }

    fun importBinary(input: InputStream, compressed: Boolean = true): Boolean {
        return try {
            val stream: InputStream = if (compressed) {
                GZIPInputStream(BufferedInputStream(input))
            } else {
                BufferedInputStream(input)
            }
            DataInputStream(stream).use { data ->
                if (data.readInt() != MOVE_TABLE_MAGIC) return false
                val version = data.readInt()
                if (version != MOVE_TABLE_VERSION) return false

                if (!readTable(data, twistMoveTable)) return false
                if (!readTable(data, flipMoveTable)) return false
                if (!readTable(data, sliceMoveTable)) return false
                if (!readTable(data, cornerPermMoveTable)) return false
                if (!readTable(data, udEdgePermMoveTable)) return false
                if (!readTable(data, sliceSortedMoveTable)) return false
            }
            initialized = true
            true
        } catch (_: Exception) {
            initialized = false
            false
        }
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

    private fun writeTable(data: DataOutputStream, table: Array<IntArray>) {
        data.writeInt(table.size)
        data.writeInt(table[0].size)
        for (row in table) {
            for (value in row) {
                data.writeInt(value)
            }
        }
    }

    private fun readTable(data: DataInputStream, table: Array<IntArray>): Boolean {
        val rows = data.readInt()
        val cols = data.readInt()
        if (rows != table.size || cols != table[0].size) return false
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                table[i][j] = data.readInt()
            }
        }
        return true
    }
}
