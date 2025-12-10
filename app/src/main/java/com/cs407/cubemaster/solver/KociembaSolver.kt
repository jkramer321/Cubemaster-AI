package com.cs407.cubemaster.solver

import android.util.Log
import com.cs407.cubemaster.data.Cube
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

    /**
     * Initialize the solver (generate move and pruning tables)
     * This should be called once when the app starts
     * Takes several seconds to complete
     */
    suspend fun initialize() = withContext(Dispatchers.Default) {
        if (isInitialized) return@withContext

        SolverLog.d("KociembaSolver", "Initializing move tables...")
        MoveTables.initialize()

        SolverLog.d("KociembaSolver", "Initializing pruning tables...")
        PruningTables.initialize()

        isInitialized = true
        SolverLog.d("KociembaSolver", "Solver initialized successfully")
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
}
