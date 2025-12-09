package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class TestSolutionApplication {

    @Test
    fun testApplySimpleScrambleAndSolution() {
        val cube = createSolvedCube()

        // Apply scramble R U R' U'
        println("Applying scramble: R U R' U'")
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        // Verify scrambled state is valid
        val scrambledState = CubeConverter.fromCube(cube)
        println("Scrambled state valid")

        // Apply solution: U R U' R D U' B2 D' U
        println("\nApplying solution: U R U' R D U' B2 D' U")
        val solution = listOf("U", "R", "U'", "R", "D", "U'", "B2", "D'", "U")
        for (move in solution) {
            println("  Applying $move...")
            CubeConverter.applyMoveString(cube, move)
        }

        // Check if cube is solved
        val finalState = CubeConverter.fromCube(cube)
        val isSolved = finalState.cornerOrientation.all { it == 0 } &&
                      finalState.edgeOrientation.all { it == 0 } &&
                      finalState.cornerPermutation.indices.all { finalState.cornerPermutation[it] == it } &&
                      finalState.edgePermutation.indices.all { finalState.edgePermutation[it] == it }

        println("\nFinal state:")
        println("  CO: ${finalState.cornerOrientation.toList()}")
        println("  CP: ${finalState.cornerPermutation.toList()}")
        println("  EO: ${finalState.edgeOrientation.toList()}")
        println("  EP: ${finalState.edgePermutation.toList()}")
        println("  Solved: $isSolved")

        assertTrue("Cube should be solved after applying scramble and solution", isSolved)
    }

    private fun createSolvedCube(): Cube {
        return Cube(
            s1 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s2 = mutableListOf(
                mutableListOf(2, 2, 2),
                mutableListOf(2, 2, 2),
                mutableListOf(2, 2, 2)
            ),
            s3 = mutableListOf(
                mutableListOf(3, 3, 3),
                mutableListOf(3, 3, 3),
                mutableListOf(3, 3, 3)
            ),
            s4 = mutableListOf(
                mutableListOf(4, 4, 4),
                mutableListOf(4, 4, 4),
                mutableListOf(4, 4, 4)
            ),
            s5 = mutableListOf(
                mutableListOf(5, 5, 5),
                mutableListOf(5, 5, 5),
                mutableListOf(5, 5, 5)
            ),
            s6 = mutableListOf(
                mutableListOf(6, 6, 6),
                mutableListOf(6, 6, 6),
                mutableListOf(6, 6, 6)
            )
        )
    }
}
