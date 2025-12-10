package com.cs407.cubemaster.solver

import org.junit.Test

class TracePhase2Coordinates {

    @Test
    fun testPhase2CoordinatesForSolution() {
        // Start with scrambled state after R U R' U'
        var state = CubeState.solved()
        state = state.applyMove(CubeMove.R)
        state = state.applyMove(CubeMove.U)
        state = state.applyMove(CubeMove.RP)
        state = state.applyMove(CubeMove.UP)

        println("=== Initial scrambled state ===")
        var coord = CoordinateSystem.Phase2Coordinate.from(state)
        println("CP: ${coord.cornerPerm}, UDP: ${coord.udEdgePerm}, SS: ${coord.sliceSorted}")
        println("EP: ${state.edgePermutation.toList()}")

        // Apply solution: U R U' R D U' B2 D' U
        val moves = listOf(CubeMove.U, CubeMove.R, CubeMove.UP, CubeMove.R, CubeMove.D, CubeMove.UP, CubeMove.B2, CubeMove.DP, CubeMove.U)

        for (move in moves) {
            state = state.applyMove(move)
            coord = CoordinateSystem.Phase2Coordinate.from(state)
            val actualSS = CoordinateSystem.getSliceSortedCoordinate(state)
            println("\nAfter $move:")
            println("  Coord from table: CP=${coord.cornerPerm}, UDP=${coord.udEdgePerm}, SS=${coord.sliceSorted}")
            println("  Actual SS from state: $actualSS")
            println("  EP: ${state.edgePermutation.toList()}")
            println("  Is solved: ${state.isSolved()}")

            if (coord.sliceSorted != actualSS) {
                println("  ❌ MISMATCH! Phase2Coordinate.from() gives SS=${coord.sliceSorted}, but actual is $actualSS")
            }
        }

        println("\n=== Final check ===")
        println("Final coord: CP=${coord.cornerPerm}, UDP=${coord.udEdgePerm}, SS=${coord.sliceSorted}")
        println("Goal check (coord == 0): ${coord.cornerPerm == 0 && coord.udEdgePerm == 0 && coord.sliceSorted == 0}")
        println("Actual solved: ${state.isSolved()}")
    }
}
