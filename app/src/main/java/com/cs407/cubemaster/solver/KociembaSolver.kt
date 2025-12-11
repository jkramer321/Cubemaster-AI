package com.cs407.cubemaster.solver

import android.util.Log
import com.cs407.cubemaster.data.Cube
import com.cs407.cubemaster.solver.TableAssetLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main interface for solving Rubik's cubes using Kociemba's two-phase algorithm
 *
 * Usage:
 * ```
 * val solver = KociembaSolver()
 * solver.initialize() // Call once at app startup
 * val solution = solver.solve(cube)
 * ```
 */
class KociembaSolver {

    private var isInitialized = false
    @Volatile
    private var initSource: String = "none"

    /**
     * Initialize the solver (generate move and pruning tables)
     * This should be called once when the app starts
     * Takes several seconds to complete when generating
     */
    suspend fun initialize(openAsset: ((String) -> java.io.InputStream?)? = null) = withContext(Dispatchers.Default) {
        if (isInitialized) return@withContext

        var loadedFromAssets = false
        if (openAsset != null) {
            loadedFromAssets = TableAssetLoader.loadAll(openAsset)
            if (!loadedFromAssets) {
                SolverLog.e("KociembaSolver", "Failed to load tables from assets; falling back to generation")
            }
        }

        if (!loadedFromAssets) {
            SolverLog.d("KociembaSolver", "Initializing move tables...")
            MoveTables.initialize()

            SolverLog.d("KociembaSolver", "Initializing pruning tables...")
            PruningTables.initialize()
            initSource = "generated"
        } else {
            SolverLog.d("KociembaSolver", "Loaded tables from assets")
            initSource = "assets"
        }

        isInitialized = true
        SolverLog.d("KociembaSolver", "Solver initialized successfully (source=$initSource)")
    }

    /**
     * Solve a Rubik's cube
     * Returns a list of moves in standard notation, or null if no solution found
     *
     * @param cube The cube to solve
     * @param maxPhase1Depth Maximum search depth for Phase 1 (default: 12)
     * @param maxPhase2Depth Maximum search depth for Phase 2 (default: 18)
     * @return Solution as list of move strings, or null if unsolvable
     */
    suspend fun solve(
        cube: Cube,
        maxPhase1Depth: Int = 12,
        maxPhase2Depth: Int = 18
    ): List<String>? = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            SolverLog.e("KociembaSolver", "Solver not initialized. Call initialize() first.")
            return@withContext null
        }

        if (cube.isSolved()) {
            SolverLog.d("KociembaSolver", "Cube is already solved")
            return@withContext emptyList()
        }

        // Convert app's Cube to CubeState
        val cubeState = CubeConverter.fromCube(cube)

        logStateDiagnostics(cubeState, "solve-entry")
        CoordinateSystem.logCoordinates(cubeState, "solve-entry", force = true)

        SolverLog.d("KociembaSolver", "Starting Phase 1 search...")
        val phase1Solution = Search.searchPhase1(cubeState, maxPhase1Depth)
        if (phase1Solution == null) {
            SolverLog.e("KociembaSolver", "Phase 1 search failed")
            return@withContext null
        }
        SolverLog.d("KociembaSolver", "Phase 1 complete: ${phase1Solution.size} moves")

        // Apply Phase 1 moves
        var intermediateState = cubeState
        for (move in phase1Solution) {
            intermediateState = intermediateState.applyMove(move)
        }

        logStateDiagnostics(intermediateState, "pre-phase2")
        CoordinateSystem.logCoordinates(intermediateState, "pre-phase2", force = true)

        if (!intermediateState.isInG1()) {
            SolverLog.e("KociembaSolver", "Phase 1 result is not in G1; aborting Phase 2")
            return@withContext null
        }

        SolverLog.d("KociembaSolver", "Starting Phase 2 search...")
        val phase2Solution = Search.searchPhase2(intermediateState, maxPhase2Depth)
        if (phase2Solution == null) {
            SolverLog.e("KociembaSolver", "Phase 2 search failed")
            return@withContext null
        }
        SolverLog.d("KociembaSolver", "Phase 2 complete: ${phase2Solution.size} moves")

        // Combine and simplify
        val fullSolution = phase1Solution + phase2Solution
        val simplified = Search.simplifySolution(fullSolution)

        SolverLog.d("KociembaSolver", "Solution found: ${simplified.size} moves")
        SolverLog.d("KociembaSolver", "Moves: ${Search.formatSolution(simplified)}")

        return@withContext simplified.map { it.notation }
    }

    /**
     * Check if solver is ready to use
     */
    fun isReady(): Boolean = isInitialized

    fun lastInitSource(): String = initSource

    /**
     * Format a solution into a readable string
     */
    fun formatSolution(solution: List<String>): String {
        return solution.joinToString(" ")
    }

    /**
     * Get estimated number of moves for a solution
     * Returns -1 if cube is not solvable or solver not initialized
     */
    suspend fun estimateMoves(cube: Cube): Int = withContext(Dispatchers.Default) {
        if (!isInitialized) return@withContext -1
        if (cube.isSolved()) return@withContext 0

        val cubeState = CubeConverter.fromCube(cube)
        val coord1 = CoordinateSystem.Phase1Coordinate.from(cubeState)
        val coord2 = CoordinateSystem.Phase2Coordinate.from(cubeState)

        val phase1Estimate = PruningTables.getPhase1Distance(coord1)
        val phase2Estimate = PruningTables.getPhase2Distance(coord2)

        return@withContext phase1Estimate + phase2Estimate
    }

    /**
     * Emit validity, parity, and orientation diagnostics for a given state.
     */
    private fun logStateDiagnostics(state: CubeState, label: String) {
        val cornerOriSum = state.cornerOrientation.sum() % 3
        val edgeOriSum = state.edgeOrientation.sum() % 2
        val cornerParity = permutationParity(state.cornerPermutation)
        val edgeParity = permutationParity(state.edgePermutation)

        SolverLog.d(
            "KociembaSolver",
            "[$label] valid=${state.isValid()} g1=${state.isInG1()} " +
                    "cornerOriSum=$cornerOriSum edgeOriSum=$edgeOriSum " +
                    "cornerParity=$cornerParity edgeParity=$edgeParity"
        )
    }

    private fun permutationParity(arr: IntArray): Int {
        var swaps = 0
        for (i in arr.indices) {
            for (j in i + 1 until arr.size) {
                if (arr[i] > arr[j]) swaps++
            }
        }
        return swaps % 2
    }
}
