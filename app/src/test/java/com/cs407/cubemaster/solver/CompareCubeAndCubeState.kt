package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

/**
 * Compare Cube.rotateX methods with CubeState.applyMove to ensure they match
 */
class CompareCubeAndCubeState {

    @Test
    fun testUMoveMatch() {
        // Apply U using Cube
        val cube1 = createSolvedCube()
        CubeConverter.applyMoveString(cube1, "U")
        val state1 = CubeConverter.fromCube(cube1)

        // Apply U using CubeState
        val cube2 = createSolvedCube()
        val cubeState2 = CubeConverter.fromCube(cube2)
        val newCubeState2 = cubeState2.applyMove(CubeMove.U)

        println("U move comparison:")
        println("Cube:  CP=${state1.cornerPermutation.toList()}, EP=${state1.edgePermutation.toList()}")
        println("State: CP=${newCubeState2.cornerPermutation.toList()}, EP=${newCubeState2.edgePermutation.toList()}")

        assertArrayEquals("Corner permutation should match", state1.cornerPermutation, newCubeState2.cornerPermutation)
        assertArrayEquals("Edge permutation should match", state1.edgePermutation, newCubeState2.edgePermutation)
        assertArrayEquals("Corner orientation should match", state1.cornerOrientation, newCubeState2.cornerOrientation)
        assertArrayEquals("Edge orientation should match", state1.edgeOrientation, newCubeState2.edgeOrientation)
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
