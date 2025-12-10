package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class DebugSimpleScrambleTest {

    @Test
    fun debugSimpleScramble() {
        val cube = createSolvedCube()

        println("=== Testing R U R' U' Step by Step ===")
        println("Initial cube is valid")

        // Apply R
        println("\n1. Applying R...")
        CubeConverter.applyMoveString(cube, "R")
        try {
            val state1 = CubeConverter.fromCube(cube)
            println("  ✓ Valid after R")
            println("    CO sum: ${state1.cornerOrientation.sum()}, EO sum: ${state1.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println("  ❌ INVALID after R: ${e.message}")
            throw e
        }

        // Apply U
        println("\n2. Applying U...")
        CubeConverter.applyMoveString(cube, "U")
        try {
            val state2 = CubeConverter.fromCube(cube)
            println("  ✓ Valid after R U")
            println("    CO sum: ${state2.cornerOrientation.sum()}, EO sum: ${state2.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println("  ❌ INVALID after R U: ${e.message}")
            throw e
        }

        // Apply R'
        println("\n3. Applying R'...")
        CubeConverter.applyMoveString(cube, "R'")
        try {
            val state3 = CubeConverter.fromCube(cube)
            println("  ✓ Valid after R U R'")
            println("    CO sum: ${state3.cornerOrientation.sum()}, EO sum: ${state3.edgeOrientation.sum()}")
        } catch (e: Exception) {
            println("  ❌ INVALID after R U R': ${e.message}")
            throw e
        }

        // Apply U'
        println("\n4. Applying U'...")
        CubeConverter.applyMoveString(cube, "U'")
        try {
            val state4 = CubeConverter.fromCube(cube)
            println("  ✓ Valid after R U R' U'")
            println("    CO sum: ${state4.cornerOrientation.sum()}, EO sum: ${state4.edgeOrientation.sum()}")
            println("\n✅ All moves valid!")
        } catch (e: Exception) {
            println("  ❌ INVALID after R U R' U': ${e.message}")
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
