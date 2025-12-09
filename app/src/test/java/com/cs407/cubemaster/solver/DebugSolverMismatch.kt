package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Debug why the solver's solution doesn't fully solve the cube
 */
class DebugSolverMismatch {

    @Test
    fun testCompareScramblesAndSolutions() = runBlocking {
        val solver = KociembaSolver()
        solver.initialize()

        // Create solved cube
        val cube = createSolvedCube()

        // Apply scramble using CUBE methods
        println("=== Applying scramble R U R' U' using Cube methods ===")
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        val scrambledState = CubeConverter.fromCube(cube)
        println("Scrambled state from Cube:")
        println("  CP: ${scrambledState.cornerPermutation.toList()}")
        println("  CO: ${scrambledState.cornerOrientation.toList()}")
        println("  EP: ${scrambledState.edgePermutation.toList()}")
        println("  EO: ${scrambledState.edgeOrientation.toList()}")

        // Now apply same scramble using CUBESTATE methods
        println("\n=== Applying scramble R U R' U' using CubeState methods ===")
        var state2 = CubeState.solved()
        state2 = state2.applyMove(CubeMove.R)
        state2 = state2.applyMove(CubeMove.U)
        state2 = state2.applyMove(CubeMove.RP)
        state2 = state2.applyMove(CubeMove.UP)
        println("Scrambled state from CubeState:")
        println("  CP: ${state2.cornerPermutation.toList()}")
        println("  CO: ${state2.cornerOrientation.toList()}")
        println("  EP: ${state2.edgePermutation.toList()}")
        println("  EO: ${state2.edgeOrientation.toList()}")

        println("\n=== Comparison ===")
        val cpMatch = scrambledState.cornerPermutation.contentEquals(state2.cornerPermutation)
        val coMatch = scrambledState.cornerOrientation.contentEquals(state2.cornerOrientation)
        val epMatch = scrambledState.edgePermutation.contentEquals(state2.edgePermutation)
        val eoMatch = scrambledState.edgeOrientation.contentEquals(state2.edgeOrientation)

        println("CP match: $cpMatch")
        println("CO match: $coMatch")
        println("EP match: $epMatch")
        println("EO match: $eoMatch")

        if (!epMatch) {
            println("\nEP differences:")
            for (i in 0..11) {
                if (scrambledState.edgePermutation[i] != state2.edgePermutation[i]) {
                    println("  Position $i: Cube=${scrambledState.edgePermutation[i]}, CubeState=${state2.edgePermutation[i]}")
                }
            }
        }

        // Get solution from solver
        println("\n=== Getting solution from solver ===")
        val solution = solver.solve(cube)
        println("Solution: $solution")
    }

    private fun createSolvedCube(): Cube {
        return Cube(
            s1 = mutableListOf(mutableListOf(1, 1, 1), mutableListOf(1, 1, 1), mutableListOf(1, 1, 1)),
            s2 = mutableListOf(mutableListOf(2, 2, 2), mutableListOf(2, 2, 2), mutableListOf(2, 2, 2)),
            s3 = mutableListOf(mutableListOf(3, 3, 3), mutableListOf(3, 3, 3), mutableListOf(3, 3, 3)),
            s4 = mutableListOf(mutableListOf(4, 4, 4), mutableListOf(4, 4, 4), mutableListOf(4, 4, 4)),
            s5 = mutableListOf(mutableListOf(5, 5, 5), mutableListOf(5, 5, 5), mutableListOf(5, 5, 5)),
            s6 = mutableListOf(mutableListOf(6, 6, 6), mutableListOf(6, 6, 6), mutableListOf(6, 6, 6))
        )
    }
}
