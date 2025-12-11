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
 * Pruning tables for Kociemba's algorithm.
 *
 * These tables store the minimum number of moves needed to solve
 * from any given coordinate. They are used to prune the search tree
 * in the IDA* algorithm.
 */
object PruningTables {

    private const val PRUNE_TABLE_MAGIC = 0x5052554E // 'PRUN'
    // Bump version because we are adding a new Phase 2 pruning table.
    private const val PRUNE_TABLE_VERSION = 2

    // Phase 1 pruning tables
    // Store minimum distance to goal for each coordinate combination
    private val phase1TwistFlipPruning = ByteArray(2187 * 2048)
    private val phase1SliceTwistPruning = ByteArray(495 * 2187)

    // Phase 2 pruning tables
    private val phase2CornerEdgePruning = ByteArray(40320 * 8) // Simplified - full table would be huge
    private val phase2CornerSlicePruning = ByteArray(40320 * 24)
    private val phase2UDEdgeSlicePruning = ByteArray(40320 * 24)

    private var initialized = false

    // Depth limits for BFS initialization (kept moderate for mobile; admissible heuristics)
    private const val PHASE1_TWIST_FLIP_LIMIT = 7
    private const val PHASE1_SLICE_TWIST_LIMIT = 12
    private const val PHASE2_LIMIT = 12

    /**
     * Initialize all pruning tables using BFS
     * This is very expensive and should be done once
     */
    fun initialize() {
        if (initialized) return

        initializePhase1Pruning()
        initializePhase2Pruning()

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
            data.writeInt(PRUNE_TABLE_MAGIC)
            data.writeInt(PRUNE_TABLE_VERSION)

            writeByteArray(data, phase1TwistFlipPruning)
            writeByteArray(data, phase1SliceTwistPruning)
            writeByteArray(data, phase2CornerEdgePruning)
            writeByteArray(data, phase2CornerSlicePruning)
            writeByteArray(data, phase2UDEdgeSlicePruning)
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
                if (data.readInt() != PRUNE_TABLE_MAGIC) return false
                val version = data.readInt()
                if (version != PRUNE_TABLE_VERSION) return false

                if (!readByteArray(data, phase1TwistFlipPruning)) return false
                if (!readByteArray(data, phase1SliceTwistPruning)) return false
                if (!readByteArray(data, phase2CornerEdgePruning)) return false
                if (!readByteArray(data, phase2CornerSlicePruning)) return false
                if (!readByteArray(data, phase2UDEdgeSlicePruning)) return false
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

    private fun initializePhase1Pruning() {
        // Initialize all distances to -1 (unknown)
        phase1TwistFlipPruning.fill(-1)
        phase1SliceTwistPruning.fill(-1)

        // BFS for twist-flip pruning
        val queue1 = ArrayDeque<Pair<Int, Int>>()
        queue1.add(Pair(0, 0)) // solved state
        setPhase1TwistFlipDistance(0, 0, 0)

        while (queue1.isNotEmpty()) {
            val (twist, flip) = queue1.removeFirst()
            val dist = getPhase1TwistFlipDistance(twist, flip)

            if (dist >= PHASE1_TWIST_FLIP_LIMIT) continue // Limit depth to keep initialization fast

            for (move in CubeMove.values()) {
                val state = CubeState.solved()
                CoordinateSystem.setTwistCoordinate(state, twist)
                CoordinateSystem.setFlipCoordinate(state, flip)
                val newState = state.applyMove(move)
                val newTwist = CoordinateSystem.getTwistCoordinate(newState)
                val newFlip = CoordinateSystem.getFlipCoordinate(newState)

                if (getPhase1TwistFlipDistance(newTwist, newFlip) == -1) {
                    setPhase1TwistFlipDistance(newTwist, newFlip, dist + 1)
                    queue1.add(Pair(newTwist, newFlip))
                }
            }
        }

        // BFS for slice-twist pruning
        val queue2 = ArrayDeque<Pair<Int, Int>>()
        val solvedSlice = CoordinateSystem.getSliceCoordinate(CubeState.solved())
        val solvedTwist = CoordinateSystem.getTwistCoordinate(CubeState.solved())
        queue2.add(Pair(solvedSlice, solvedTwist))
        setPhase1SliceTwistDistance(solvedSlice, solvedTwist, 0)

        while (queue2.isNotEmpty()) {
            val (slice, twist) = queue2.removeFirst()
            val dist = getPhase1SliceTwistDistance(slice, twist)

            // Increase depth limit or remove it for full initialization
            // For mobile, we might want to keep it reasonable, e.g., 8-9
            // But for correctness, we need deeper tables.
            // Let's try 12 for now to avoid timeout during initialization
            if (dist >= PHASE1_SLICE_TWIST_LIMIT) continue

            for (move in CubeMove.values()) {
                val state = CubeState.solved()
                CoordinateSystem.setTwistCoordinate(state, twist)
                CoordinateSystem.setSliceCoordinate(state, slice)
                
                val newState = state.applyMove(move)
                val newSlice = CoordinateSystem.getSliceCoordinate(newState)
                val newTwist = CoordinateSystem.getTwistCoordinate(newState)

                if (getPhase1SliceTwistDistance(newSlice, newTwist) == -1) {
                    setPhase1SliceTwistDistance(newSlice, newTwist, dist + 1)
                    queue2.add(Pair(newSlice, newTwist))
                }
            }
        }
    }

    private fun initializePhase2Pruning() {
        // Initialize all distances to -1
        phase2CornerEdgePruning.fill(-1)
        phase2CornerSlicePruning.fill(-1)
        phase2UDEdgeSlicePruning.fill(-1)

        // BFS on Phase 2 coordinates using move tables to stay in canonical coordinate space
        val queue = ArrayDeque<CoordinateSystem.Phase2Coordinate>()
        val start = CoordinateSystem.Phase2Coordinate(0, 0, 0)
        queue.add(start)
        setPhase2CornerSliceDistance(0, 0, 0)
        setPhase2UDEdgeSliceDistance(0, 0, 0)

        while (queue.isNotEmpty()) {
            val coord = queue.removeFirst()
            val dist = getPhase2CornerSliceDistance(coord.cornerPerm, coord.sliceSorted)
            if (dist >= PHASE2_LIMIT) continue // keep generation bounded for startup

            for (move in CubeMove.PHASE2_MOVES) {
                val next = MoveTables.applyMovePhase2(coord, move)

                var enqueued = false
                if (getPhase2CornerSliceDistance(next.cornerPerm, next.sliceSorted) == -1) {
                    setPhase2CornerSliceDistance(next.cornerPerm, next.sliceSorted, dist + 1)
                    enqueued = true
                }
                if (getPhase2UDEdgeSliceDistance(next.udEdgePerm, next.sliceSorted) == -1) {
                    setPhase2UDEdgeSliceDistance(next.udEdgePerm, next.sliceSorted, dist + 1)
                    enqueued = true
                }
                if (enqueued) {
                    queue.add(next)
                }
            }
        }
    }

    /**
     * Get minimum distance estimate for Phase 1
     */
    fun getPhase1Distance(coord: CoordinateSystem.Phase1Coordinate): Int {
        val dist1 = getPhase1TwistFlipDistance(coord.twist, coord.flip)
        val dist2 = getPhase1SliceTwistDistance(coord.slice, coord.twist)
        return maxOf(dist1, dist2, 0)
    }

    /**
     * Get minimum distance estimate for Phase 2
     */
    fun getPhase2Distance(coord: CoordinateSystem.Phase2Coordinate): Int {
        val dist1 = getPhase2CornerSliceDistance(coord.cornerPerm, coord.sliceSorted)
        val dist2 = getPhase2UDEdgeSliceDistance(coord.udEdgePerm, coord.sliceSorted)
        return maxOf(dist1, dist2, 0)
    }

    private fun getPhase1TwistFlipDistance(twist: Int, flip: Int): Int {
        if (twist >= 2187 || flip >= 2048) return 0
        val idx = twist * 2048 + flip
        return phase1TwistFlipPruning[idx].toInt()
    }

    private fun setPhase1TwistFlipDistance(twist: Int, flip: Int, dist: Int) {
        if (twist >= 2187 || flip >= 2048) return
        val idx = twist * 2048 + flip
        phase1TwistFlipPruning[idx] = dist.toByte()
    }

    private fun getPhase1SliceTwistDistance(slice: Int, twist: Int): Int {
        if (slice >= 495 || twist >= 2187) return 0
        val idx = slice * 2187 + twist
        return phase1SliceTwistPruning[idx].toInt()
    }

    private fun setPhase1SliceTwistDistance(slice: Int, twist: Int, dist: Int) {
        if (slice >= 495 || twist >= 2187) return
        val idx = slice * 2187 + twist
        phase1SliceTwistPruning[idx] = dist.toByte()
    }

    private fun getPhase2CornerSliceDistance(cornerPerm: Int, sliceSorted: Int): Int {
        if (cornerPerm >= 40320 || sliceSorted >= 24) return 0
        val idx = cornerPerm * 24 + sliceSorted
        return phase2CornerSlicePruning[idx].toInt()
    }

    private fun setPhase2CornerSliceDistance(cornerPerm: Int, sliceSorted: Int, dist: Int) {
        if (cornerPerm >= 40320 || sliceSorted >= 24) return
        val idx = cornerPerm * 24 + sliceSorted
        phase2CornerSlicePruning[idx] = dist.toByte()
    }

    private fun getPhase2UDEdgeSliceDistance(udEdgePerm: Int, sliceSorted: Int): Int {
        if (udEdgePerm >= 40320 || sliceSorted >= 24) return 0
        val idx = udEdgePerm * 24 + sliceSorted
        return phase2UDEdgeSlicePruning[idx].toInt()
    }

    private fun setPhase2UDEdgeSliceDistance(udEdgePerm: Int, sliceSorted: Int, dist: Int) {
        if (udEdgePerm >= 40320 || sliceSorted >= 24) return
        val idx = udEdgePerm * 24 + sliceSorted
        phase2UDEdgeSlicePruning[idx] = dist.toByte()
    }

    fun isInitialized(): Boolean = initialized

    private fun writeByteArray(data: DataOutputStream, arr: ByteArray) {
        data.writeInt(arr.size)
        data.write(arr)
    }

    private fun readByteArray(data: DataInputStream, target: ByteArray): Boolean {
        val size = data.readInt()
        if (size != target.size) return false
        val bytes = data.readNBytes(size)
        if (bytes.size != size) return false
        System.arraycopy(bytes, 0, target, 0, size)
        return true
    }
}
