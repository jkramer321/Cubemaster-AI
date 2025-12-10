package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class TestCommutator {

    @Test
    fun testRURUCommutator() {
        val cube = createSolvedCube()

        // Apply R U R' U' 6 times (should return to solved)
        println("Applying (R U R' U') 6 times...")
        repeat(6) { i ->
            CubeConverter.applyMoveString(cube, "R")
            CubeConverter.applyMoveString(cube, "U")
            CubeConverter.applyMoveString(cube, "R'")
            CubeConverter.applyMoveString(cube, "U'")

            val state = CubeConverter.fromCube(cube)
            val isSolved = state.cornerOrientation.all { it == 0 } &&
                          state.edgeOrientation.all { it == 0 } &&
                          state.cornerPermutation.indices.all { state.cornerPermutation[it] == it } &&
                          state.edgePermutation.indices.all { state.edgePermutation[it] == it }

            println("After ${i+1} applications: solved=$isSolved, CP=${state.cornerPermutation.toList()}")
        }

        val finalState = CubeConverter.fromCube(cube)
        val isSolved = finalState.cornerOrientation.all { it == 0 } &&
                      finalState.edgeOrientation.all { it == 0 } &&
                      finalState.cornerPermutation.indices.all { finalState.cornerPermutation[it] == it } &&
                      finalState.edgePermutation.indices.all { finalState.edgePermutation[it] == it }

        assertTrue("Cube should be solved after 6 applications of R U R' U'", isSolved)
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
