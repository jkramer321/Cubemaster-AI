package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class DebugFaceRotation {

    @Test
    fun testFrontFaceRotation() {
        val cube = createSolvedCube()

        println("Before F move:")
        printFrontFaceCorners(cube)

        // Apply F move
        cube.rotateDepth(0, true)

        println("\nAfter F move:")
        printFrontFaceCorners(cube)

        // Convert to CubeState
        val state = CubeConverter.fromCube(cube)
        println("\nCorner permutation from converter: ${state.cornerPermutation.toList()}")
        println("Corner orientation from converter: ${state.cornerOrientation.toList()}")

        // Expected from CubeState
        println("\nExpected corner permutation: [1, 5, 2, 3, 0, 4, 6, 7]")
    }

    private fun printFrontFaceCorners(cube: Cube) {
        println("Front face (s1):")
        println("  Top-left [0][0] = ${cube.getCell("s1", 0, 0)} (should be UFL = corner 1)")
        println("  Top-right [0][2] = ${cube.getCell("s1", 0, 2)} (should be URF = corner 0)")
        println("  Bottom-left [2][0] = ${cube.getCell("s1", 2, 0)} (should be DLF = corner 5)")
        println("  Bottom-right [2][2] = ${cube.getCell("s1", 2, 2)} (should be DFR = corner 4)")

        println("Top face (s2) bottom row:")
        println("  [2][0] = ${cube.getCell("s2", 2, 0)} (UFL)")
        println("  [2][2] = ${cube.getCell("s2", 2, 2)} (URF)")

        println("Bottom face (s3) top row:")
        println("  [0][0] = ${cube.getCell("s3", 0, 0)} (DLF)")
        println("  [0][2] = ${cube.getCell("s3", 0, 2)} (DFR)")

        println("Left face (s4) right column:")
        println("  [0][2] = ${cube.getCell("s4", 0, 2)} (UFL)")
        println("  [2][2] = ${cube.getCell("s4", 2, 2)} (DLF)")

        println("Right face (s5) left column:")
        println("  [0][0] = ${cube.getCell("s5", 0, 0)} (URF)")
        println("  [2][0] = ${cube.getCell("s5", 2, 0)} (DFR)")
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
