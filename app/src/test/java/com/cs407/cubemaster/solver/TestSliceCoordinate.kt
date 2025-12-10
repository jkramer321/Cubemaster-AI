package com.cs407.cubemaster.solver

import org.junit.Test

class TestSliceCoordinate {

    @Test
    fun testSliceSortedForFailedSolution() {
        // The failed solution ends with EP=[0,1,2,3,4,5,6,7,11,9,8,10]
        val state = CubeState(
            cornerPermutation = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7),
            cornerOrientation = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            edgePermutation = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 11, 9, 8, 10),
            edgeOrientation = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        )

        val sliceSorted = CoordinateSystem.getSliceSortedCoordinate(state)
        println("EP: ${state.edgePermutation.toList()}")
        println("sliceSorted coordinate: $sliceSorted")
        println("Expected: 0 for solved, but edges 8-11 are [11,9,8,10]")

        // Extract slice edges
        val sliceEdges = mutableListOf<Int>()
        for (i in 0..11) {
            val edge = state.edgePermutation[i]
            if (edge in 8..11) {
                sliceEdges.add(edge)
                println("Position $i has slice edge $edge")
            }
        }
        println("Slice edges in order: $sliceEdges")
        println("Slice edges - 8: ${sliceEdges.map { it - 8 }}")
    }

    @Test
    fun testSliceSortedForSolvedState() {
        val state = CubeState.solved()

        val sliceSorted = CoordinateSystem.getSliceSortedCoordinate(state)
        println("Solved EP: ${state.edgePermutation.toList()}")
        println("Solved sliceSorted coordinate: $sliceSorted")
    }
}
