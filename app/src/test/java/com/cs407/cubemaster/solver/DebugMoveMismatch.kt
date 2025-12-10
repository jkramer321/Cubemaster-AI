package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class DebugMoveMismatch {

    @Test
    fun testCompareSingleMoves() {
        // Test each basic move to see if Cube and CubeState match
        val moves = listOf(
            "R" to CubeMove.R,
            "U" to CubeMove.U,
            "F" to CubeMove.F,
            "L" to CubeMove.L,
            "D" to CubeMove.D,
            "B" to CubeMove.B
        )

        for ((moveStr, cubeMove) in moves) {
            val cube = createSolvedCube()
            var state = CubeState.solved()

            // Apply move to both
            CubeConverter.applyMoveString(cube, moveStr)
            state = state.applyMove(cubeMove)

            // Convert cube to state and compare
            val cubeAsState = CubeConverter.fromCube(cube)

            println("\n=== Testing move $moveStr ===")
            println("CubeState CP: ${state.cornerPermutation.toList()}")
            println("Cube CP:      ${cubeAsState.cornerPermutation.toList()}")
            println("CubeState CO: ${state.cornerOrientation.toList()}")
            println("Cube CO:      ${cubeAsState.cornerOrientation.toList()}")
            println("CubeState EP: ${state.edgePermutation.toList()}")
            println("Cube EP:      ${cubeAsState.edgePermutation.toList()}")
            println("CubeState EO: ${state.edgeOrientation.toList()}")
            println("Cube EO:      ${cubeAsState.edgeOrientation.toList()}")

            // Compare
            assertArrayEquals("$moveStr: Corner permutation mismatch",
                state.cornerPermutation, cubeAsState.cornerPermutation)
            assertArrayEquals("$moveStr: Corner orientation mismatch",
                state.cornerOrientation, cubeAsState.cornerOrientation)
            assertArrayEquals("$moveStr: Edge permutation mismatch",
                state.edgePermutation, cubeAsState.edgePermutation)
            assertArrayEquals("$moveStr: Edge orientation mismatch",
                state.edgeOrientation, cubeAsState.edgeOrientation)
        }
    }

    @Test
    fun testScrambleRURU() {
        val cube = createSolvedCube()
        var state = CubeState.solved()

        val moves = listOf("R", "U", "R'", "U'")
        val cubeMoves = listOf(CubeMove.R, CubeMove.U, CubeMove.RP, CubeMove.UP)

        for (i in moves.indices) {
            CubeConverter.applyMoveString(cube, moves[i])
            state = state.applyMove(cubeMoves[i])

            println("\nAfter ${moves[i]}:")
            val cubeAsState = CubeConverter.fromCube(cube)
            println("  CubeState EP: ${state.edgePermutation.toList()}")
            println("  Cube EP:      ${cubeAsState.edgePermutation.toList()}")
        }

        val cubeAsState = CubeConverter.fromCube(cube)
        println("\nFinal comparison:")
        println("CubeState: CP=${state.cornerPermutation.toList()}, EP=${state.edgePermutation.toList()}")
        println("Cube:      CP=${cubeAsState.cornerPermutation.toList()}, EP=${cubeAsState.edgePermutation.toList()}")

        assertArrayEquals("Corner permutation mismatch",
            state.cornerPermutation, cubeAsState.cornerPermutation)
        assertArrayEquals("Edge permutation mismatch",
            state.edgePermutation, cubeAsState.edgePermutation)
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
