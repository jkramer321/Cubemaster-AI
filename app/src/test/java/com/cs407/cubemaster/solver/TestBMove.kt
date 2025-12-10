package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class TestBMove {

    @Test
    fun testB2Move() {
        val cube = createSolvedCube()
        println("Testing B2 move...")

        CubeConverter.applyMoveString(cube, "B2")

        try {
            val state = CubeConverter.fromCube(cube)
            println("✓ B2 move produced VALID state")
            println("  CO sum: ${state.cornerOrientation.sum()}, EO sum: ${state.edgeOrientation.sum()}")
            println("  CP: ${state.cornerPermutation.toList()}")
            println("  EP: ${state.edgePermutation.toList()}")
        } catch (e: Exception) {
            println("❌ B2 move produced INVALID state:")
            println("  ${e.message}")
            throw e
        }
    }

    @Test
    fun testBMove() {
        val cube = createSolvedCube()
        println("Testing B move...")

        CubeConverter.applyMoveString(cube, "B")

        try {
            val state = CubeConverter.fromCube(cube)
            println("✓ B move produced VALID state")
            println("  CO sum: ${state.cornerOrientation.sum()}, EO sum: ${state.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println("❌ B move produced INVALID state:")
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
