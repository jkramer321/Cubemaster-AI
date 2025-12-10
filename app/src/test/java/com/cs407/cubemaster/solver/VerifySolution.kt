package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class VerifySolution {

    @Test
    fun testSolutionUsingCubeState() = runBlocking {
        // Start with solved state
        var state = CubeState.solved()

        // Apply scramble R U R' U' using CubeState
        println("=== Applying scramble R U R' U' ===")
        state = state.applyMove(CubeMove.R)
        state = state.applyMove(CubeMove.U)
        state = state.applyMove(CubeMove.RP)
        state = state.applyMove(CubeMove.UP)

        println("Scrambled state:")
        println("  CP: ${state.cornerPermutation.toList()}")
        println("  EP: ${state.edgePermutation.toList()}")

        // Apply solution: U R U' R D U' B2 D' U
        println("\n=== Applying solution U R U' R D U' B2 D' U ===")
        state = state.applyMove(CubeMove.U)
        println("After U: EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.R)
        println("After R: EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.UP)
        println("After U': EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.R)
        println("After R: EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.D)
        println("After D: EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.UP)
        println("After U': EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.B2)
        println("After B2: EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.DP)
        println("After D': EP=${state.edgePermutation.toList()}")
        state = state.applyMove(CubeMove.U)
        println("After U: EP=${state.edgePermutation.toList()}")

        println("\nFinal state:")
        println("  CP: ${state.cornerPermutation.toList()}")
        println("  CO: ${state.cornerOrientation.toList()}")
        println("  EP: ${state.edgePermutation.toList()}")
        println("  EO: ${state.edgeOrientation.toList()}")

        val isSolved = state.isSolved()
        println("\nIs solved: $isSolved")

        assertTrue("Solution should solve the cube using CubeState", isSolved)
    }

    @Test
    fun testSolutionUsingCube() {
        val cube = createSolvedCube()

        // Apply scramble
        println("=== Applying scramble R U R' U' ===")
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        val scrambledState = CubeConverter.fromCube(cube)
        println("Scrambled EP: ${scrambledState.edgePermutation.toList()}")

        // Apply solution
        println("\n=== Applying solution U R U' R D U' B2 D' U ===")
        CubeConverter.applyMoveString(cube, "U")
        var state = CubeConverter.fromCube(cube)
        println("After U: EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "R")
        state = CubeConverter.fromCube(cube)
        println("After R: EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "U'")
        state = CubeConverter.fromCube(cube)
        println("After U': EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "R")
        state = CubeConverter.fromCube(cube)
        println("After R: EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "D")
        state = CubeConverter.fromCube(cube)
        println("After D: EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "U'")
        state = CubeConverter.fromCube(cube)
        println("After U': EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "B2")
        state = CubeConverter.fromCube(cube)
        println("After B2: EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "D'")
        state = CubeConverter.fromCube(cube)
        println("After D': EP=${state.edgePermutation.toList()}")

        CubeConverter.applyMoveString(cube, "U")
        state = CubeConverter.fromCube(cube)
        println("After U: EP=${state.edgePermutation.toList()}")

        val finalState = CubeConverter.fromCube(cube)
        println("\nFinal state:")
        println("  CP: ${finalState.cornerPermutation.toList()}")
        println("  CO: ${finalState.cornerOrientation.toList()}")
        println("  EP: ${finalState.edgePermutation.toList()}")
        println("  EO: ${finalState.edgeOrientation.toList()}")

        val isSolved = finalState.cornerOrientation.all { it == 0 } &&
                      finalState.edgeOrientation.all { it == 0 } &&
                      finalState.cornerPermutation.indices.all { finalState.cornerPermutation[it] == it } &&
                      finalState.edgePermutation.indices.all { finalState.edgePermutation[it] == it }

        println("\nIs solved: $isSolved")

        assertTrue("Solution should solve the cube using Cube", isSolved)
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
