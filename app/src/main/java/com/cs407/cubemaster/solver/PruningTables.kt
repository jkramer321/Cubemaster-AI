package com.cs407.cubemaster.solver

import java.io.DataInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Pruning tables for Kociemba's algorithm.
 *
 * These tables store the minimum number of moves needed to solve
 * from any given coordinate. They are used to prune the search tree
 * in the IDA* algorithm.
 */
object PruningTables {

    // Phase 1 pruning tables
    // Store minimum distance to goal for each coordinate combination
    private val phase1TwistFlipPruning = ByteArray(2187 * 2048)
    private val phase1SliceTwistPruning = ByteArray(495 * 2187)

    // Phase 2 pruning tables
    private val phase2CornerEdgePruning = ByteArray(40320 * 8) // Simplified - full table would be huge
    private val phase2CornerSlicePruning = ByteArray(40320 * 24)

    private var initialized = false

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

            if (dist >= 7) continue // Limit depth to keep initialization fast

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
            if (dist >= 12) continue

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

        // For Phase 2, we use simplified pruning tables
        // A full implementation would require more sophisticated indexing

        // BFS for corner-slice pruning
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(0, 0)) // solved state
        setPhase2CornerSliceDistance(0, 0, 0)

        while (queue.isNotEmpty()) {
            val (cornerPerm, sliceSorted) = queue.removeFirst()
            val dist = getPhase2CornerSliceDistance(cornerPerm, sliceSorted)

            if (dist >= 10) continue // Limit depth

            for (move in CubeMove.PHASE2_MOVES) {
                val state = CubeState.solved()
                CoordinateSystem.setCornerPermutationCoordinate(state, cornerPerm)
                val newState = state.applyMove(move)
                val newCornerPerm = CoordinateSystem.getCornerPermutationCoordinate(newState)
                val newSliceSorted = CoordinateSystem.getSliceSortedCoordinate(newState)

                if (getPhase2CornerSliceDistance(newCornerPerm, newSliceSorted) == -1) {
                    setPhase2CornerSliceDistance(newCornerPerm, newSliceSorted, dist + 1)
                    queue.add(Pair(newCornerPerm, newSliceSorted))
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
        return maxOf(dist1, 0)
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
        val idx = (cornerPerm % 5040) * 24 + sliceSorted // Use modulo to reduce table size
        return if (idx < phase2CornerSlicePruning.size) phase2CornerSlicePruning[idx].toInt() else 0
    }

    private fun setPhase2CornerSliceDistance(cornerPerm: Int, sliceSorted: Int, dist: Int) {
        if (cornerPerm >= 40320 || sliceSorted >= 24) return
        val idx = (cornerPerm % 5040) * 24 + sliceSorted
        if (idx < phase2CornerSlicePruning.size) {
            phase2CornerSlicePruning[idx] = dist.toByte()
        }
    }

    fun isInitialized(): Boolean = initialized


    /**
     * Load pruning tables from a compressed or uncompressed binary file
     * File format matches TableGenerator.saveTables()
     * This loads both move and pruning tables from the same stream
     * 
     * @param inputStream The input stream to read from
     * @param isCompressed Whether the stream is GZIP compressed (true) or raw binary (false)
     */
    fun loadFromStream(inputStream: InputStream, isCompressed: Boolean = true): Boolean {
        try {
            val dataStream = if (isCompressed) {
                DataInputStream(GZIPInputStream(inputStream))
            } else {
                DataInputStream(inputStream)
            }
            
            dataStream.use { input ->
                // Read and verify magic number
                val magic = input.readInt()
                if (magic != 0x4B4F4349) { // "KOCI"
                    SolverLog.e("PruningTables", "Invalid magic number: 0x${magic.toString(16)}")
                    return false
                }
                
                // Read version
                val version = input.readInt()
                if (version != 1) {
                    SolverLog.e("PruningTables", "Unsupported version: $version")
                    return false
                }
                
                SolverLog.d("PruningTables", "Loading move tables...")
                // Load move tables first
                if (!MoveTables.loadFromStreamInternal(input)) {
                    SolverLog.e("PruningTables", "Failed to load move tables")
                    return false
                }
                SolverLog.d("PruningTables", "Move tables loaded successfully")
                
                // Load pruning tables
                SolverLog.d("PruningTables", "Loading pruning tables...")
                // Phase 1 twist-flip pruning [2187 * 2048]
                val phase1TwistFlipSize = input.readInt()
                if (phase1TwistFlipSize != 2187 * 2048) {
                    SolverLog.e("PruningTables", "Invalid phase1TwistFlip size: $phase1TwistFlipSize, expected ${2187 * 2048}")
                    return false
                }
                input.readFully(phase1TwistFlipPruning)
                SolverLog.d("PruningTables", "Phase 1 twist-flip pruning loaded")
                
                // Phase 1 slice-twist pruning [495 * 2187]
                val phase1SliceTwistSize = input.readInt()
                if (phase1SliceTwistSize != 495 * 2187) {
                    SolverLog.e("PruningTables", "Invalid phase1SliceTwist size: $phase1SliceTwistSize, expected ${495 * 2187}")
                    return false
                }
                input.readFully(phase1SliceTwistPruning)
                SolverLog.d("PruningTables", "Phase 1 slice-twist pruning loaded")
                
                // Phase 2 corner-edge pruning [40320 * 8]
                val phase2CornerEdgeSize = input.readInt()
                if (phase2CornerEdgeSize != 40320 * 8) {
                    SolverLog.e("PruningTables", "Invalid phase2CornerEdge size: $phase2CornerEdgeSize, expected ${40320 * 8}")
                    return false
                }
                input.readFully(phase2CornerEdgePruning)
                SolverLog.d("PruningTables", "Phase 2 corner-edge pruning loaded")
                
                // Phase 2 corner-slice pruning [40320 * 24]
                val phase2CornerSliceSize = input.readInt()
                if (phase2CornerSliceSize != 40320 * 24) {
                    SolverLog.e("PruningTables", "Invalid phase2CornerSlice size: $phase2CornerSliceSize, expected ${40320 * 24}")
                    return false
                }
                input.readFully(phase2CornerSlicePruning)
                SolverLog.d("PruningTables", "Phase 2 corner-slice pruning loaded")
                
                initialized = true
                SolverLog.d("PruningTables", "All tables loaded successfully from file")
                return true
            }
        } catch (e: Exception) {
            SolverLog.e("PruningTables", "Exception loading from stream: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Getter methods for TableGenerator compatibility
     */
    fun getPhase1TwistFlipPruning(): ByteArray = phase1TwistFlipPruning
    fun getPhase1SliceTwistPruning(): ByteArray = phase1SliceTwistPruning
    fun getPhase2CornerEdgePruning(): ByteArray = phase2CornerEdgePruning
    fun getPhase2CornerSlicePruning(): ByteArray = phase2CornerSlicePruning
}
