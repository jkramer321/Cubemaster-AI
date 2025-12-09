package com.cs407.cubemaster.solver

import android.content.Context
import android.util.Log
import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

/**
 * Main interface for solving Rubik's cubes using Kociemba's two-phase algorithm
 *
 * Usage:
 * ```
 * val solver = KociembaSolver()
 * solver.initialize(context) // Call once at app startup
 * val solution = solver.solve(cube)
 * ```
 */
class KociembaSolver {

    private var isInitialized = false

    /**
     * Initialize the solver (load or generate move and pruning tables)
     * This should be called once when the app starts
     * 
     * First tries to load pre-computed tables from assets/kociemba_tables.bin.gz
     * If that fails, generates the tables (takes several seconds)
     * 
     * @param context Android context for accessing assets (can be null for testing)
     */
    suspend fun initialize(context: Context? = null) = withContext(Dispatchers.Default) {
        if (isInitialized) {
            SolverLog.d("KociembaSolver", "Solver already initialized")
            return@withContext
        }

        // Try loading from assets first if context is provided and tables aren't initialized
        if (context != null && !MoveTables.isInitialized() && !PruningTables.isInitialized()) {
            // Try both .bin.gz (compressed) and .bin (uncompressed) filenames
            val possibleFilenames = listOf("kociemba_tables.bin.gz", "kociemba_tables.bin")
            var loaded = false
            
            for (filename in possibleFilenames) {
                try {
                    SolverLog.d("KociembaSolver", "Attempting to load tables from assets/$filename...")
                    
                    // List available assets for debugging (only in debug builds)
                    try {
                        val assetFiles = context.assets.list("")
                        if (assetFiles != null) {
                            val hasTableFile = assetFiles.contains(filename)
                            SolverLog.d("KociembaSolver", "Asset file '$filename' exists: $hasTableFile (found ${assetFiles.size} files in assets)")
                            if (!hasTableFile && assetFiles.isNotEmpty() && filename == possibleFilenames.first()) {
                                SolverLog.d("KociembaSolver", "Available asset files: ${assetFiles.take(10).joinToString()}")
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore - just for debugging
                    }
                    
                    val inputStream = context.assets.open(filename)
                    try {
                        SolverLog.d("KociembaSolver", "File opened, starting to load tables...")
                        if (PruningTables.loadFromStream(inputStream, filename.endsWith(".gz"))) {
                            // Verify both tables were loaded successfully
                            if (MoveTables.isInitialized() && PruningTables.isInitialized()) {
                                isInitialized = true
                                SolverLog.d("KociembaSolver", "✓ Tables loaded successfully from assets file: $filename")
                                loaded = true
                                break
                            } else {
                                SolverLog.e("KociembaSolver", "Tables partially loaded (MoveTables: ${MoveTables.isInitialized()}, PruningTables: ${PruningTables.isInitialized()}), will try next filename")
                                // Clear partial state - will try next filename or fall through to generation
                            }
                        } else {
                            SolverLog.d("KociembaSolver", "loadFromStream returned false for $filename, will try next filename")
                        }
                    } finally {
                        inputStream.close()
                    }
                } catch (e: FileNotFoundException) {
                    SolverLog.d("KociembaSolver", "Tables file not found: $filename, will try next filename")
                    // Continue to next filename
                } catch (e: Exception) {
                    SolverLog.e("KociembaSolver", "Error loading tables from assets/$filename: ${e.javaClass.simpleName}: ${e.message}")
                    e.printStackTrace()
                    // Continue to next filename
                }
            }
            
            if (loaded) {
                return@withContext
            }
            
            SolverLog.d("KociembaSolver", "Could not load tables from any asset file, will generate")
        } else {
            if (context == null) {
                SolverLog.d("KociembaSolver", "No context provided, will generate tables")
            } else if (MoveTables.isInitialized() || PruningTables.isInitialized()) {
                SolverLog.d("KociembaSolver", "Tables already initialized (MoveTables: ${MoveTables.isInitialized()}, PruningTables: ${PruningTables.isInitialized()}), skipping asset load")
            }
        }

        // Fall back to generating tables if not loaded from assets
        if (!MoveTables.isInitialized()) {
            SolverLog.d("KociembaSolver", "Generating move tables (this may take a few seconds)...")
            MoveTables.initialize()
        }

        if (!PruningTables.isInitialized()) {
            SolverLog.d("KociembaSolver", "Generating pruning tables (this may take a few seconds)...")
            PruningTables.initialize()
        }

        // Final verification
        if (!MoveTables.isInitialized() || !PruningTables.isInitialized()) {
            SolverLog.e("KociembaSolver", "Failed to initialize tables")
            return@withContext
        }

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
