package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class TestWithoutSimplify {

    @Test
    fun testSolverWithDebug() = runBlocking {
        val solver = KociembaSolver()
        solver.initialize()

        val cube = createSolvedCube()

        // Apply scramble
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        val cubeState = CubeConverter.fromCube(cube)
        println("Scrambled state: EP=${cubeState.edgePermutation.toList()}")

        // Manually run Phase 1
        val phase1Solution = Search.searchPhase1(cubeState, 12)
        println("\nPhase 1 solution: ${phase1Solution?.map { it.notation }}")

        var intermediateState = cubeState
        for (move in phase1Solution!!) {
            intermediateState = intermediateState.applyMove(move)
        }
        println("After Phase 1: EP=${intermediateState.edgePermutation.toList()}")
        println("Intermediate is solved: ${intermediateState.isSolved()}")

        val coord = CoordinateSystem.Phase2Coordinate.from(intermediateState)
        println("Phase 2 starting coord: CP=${coord.cornerPerm}, UDP=${coord.udEdgePerm}, SS=${coord.sliceSorted}")

        // Manually run Phase 2
        val phase2Solution = Search.searchPhase2(intermediateState, 18)
        println("\nPhase 2 solution: ${phase2Solution?.map { it.notation }}")

        var finalState = intermediateState
        for (move in phase2Solution!!) {
            finalState = finalState.applyMove(move)
        }
        println("After Phase 2: EP=${finalState.edgePermutation.toList()}")
        println("Final is solved: ${finalState.isSolved()}")

        val finalCoord = CoordinateSystem.Phase2Coordinate.from(finalState)
        println("Final coord: CP=${finalCoord.cornerPerm}, UDP=${finalCoord.udEdgePerm}, SS=${finalCoord.sliceSorted}")

        // Test full combined solution WITHOUT simplification
        val fullSolution = phase1Solution + phase2Solution
        println("\n=== Testing full unsimplified solution ===")
        println("Full solution (${fullSolution.size} moves): ${fullSolution.map { it.notation }}")

        var testState = cubeState
        for (move in fullSolution) {
            testState = testState.applyMove(move)
        }
        println("After full solution: EP=${testState.edgePermutation.toList()}")
        println("Solved: ${testState.isSolved()}")

        assertTrue("Full unsimplified solution should solve the cube", testState.isSolved())
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
