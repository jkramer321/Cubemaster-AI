package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class TestFMoveOnly {

    @Test
    fun testFMove() {
        val cube = createSolvedCube()
        println("Testing F move...")

        // Print relevant pieces BEFORE F move
        println("BEFORE F move:")
        println("  U bottom row (s2[2]): ${cube.s2[2]}")
        println("  R left col (s5 col 0): [${cube.s5[0][0]}, ${cube.s5[1][0]}, ${cube.s5[2][0]}]")
        println("  D top row (s3[0]): ${cube.s3[0]}")
        println("  L right col (s4 col 2): [${cube.s4[0][2]}, ${cube.s4[1][2]}, ${cube.s4[2][2]}]")

        CubeConverter.applyMoveString(cube, "F")

        // Print relevant pieces AFTER F move
        println("\nAFTER F move:")
        println("  U bottom row (s2[2]): ${cube.s2[2]}")
        println("  R left col (s5 col 0): [${cube.s5[0][0]}, ${cube.s5[1][0]}, ${cube.s5[2][0]}]")
        println("  D top row (s3[0]): ${cube.s3[0]}")
        println("  L right col (s4 col 2): [${cube.s4[0][2]}, ${cube.s4[1][2]}, ${cube.s4[2][2]}]")
        println("  Front face (s1): ${cube.s1}")

        try {
            val state = CubeConverter.fromCube(cube)
            println("\n✓ F move produced VALID state")
            println("  CO sum: ${state.cornerOrientation.sum()}, EO sum: ${state.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println("\n F move produced INVALID state:")
            println("  ${e.message}")
            throw e
        }
    }

    @Test
    fun testFPrimeMove() {
        val cube = createSolvedCube()
        println("Testing F' move...")
        CubeConverter.applyMoveString(cube, "F'")

        try {
            val state = CubeConverter.fromCube(cube)
            println("✓ F' move produced VALID state")
            println("  CO sum: ${state.cornerOrientation.sum()}, EO sum: ${state.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println(" F' move produced INVALID state:")
            println("  ${e.message}")
            throw e
        }
    }

    @Test
    fun testFIdentity() {
        val cube = createSolvedCube()
        println("Testing F F F F = identity...")

        repeat(4) { i ->
            CubeConverter.applyMoveString(cube, "F")
            println("After ${i+1} F move(s)...")
        }

        try {
            val state = CubeConverter.fromCube(cube)
            println("✓ F F F F produced VALID state")
            println("  CO sum: ${state.cornerOrientation.sum()}, EO sum: ${state.edgeOrientation.sum()}")

            // Check if it's the solved state
            val isSolved = state.cornerOrientation.all { it == 0 } &&
                          state.edgeOrientation.all { it == 0 } &&
                          state.cornerPermutation.indices.all { state.cornerPermutation[it] == it } &&
                          state.edgePermutation.indices.all { state.edgePermutation[it] == it }

            if (isSolved) {
                println("✓ F F F F = identity (cube returned to solved state)")
            } else {
                println(" F F F F != identity")
                println("  CP: ${state.cornerPermutation.toList()}")
                println("  EP: ${state.edgePermutation.toList()}")
            }
        } catch (e: Exception) {
            println("F F F F produced INVALID state:")
            println("  ${e.message}")
            throw e
        }
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
