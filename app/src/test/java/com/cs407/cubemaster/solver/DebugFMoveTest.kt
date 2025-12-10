package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class DebugFMoveTest {
    
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
    
    @Test
    fun debugFMove() {
        val cube = createSolvedCube()
        
        println("=== BEFORE F MOVE ===")
        printAllFaces(cube)
        printAllCorners(cube)
        
        // Apply F move
        CubeConverter.applyMoveString(cube, "F")
        
        println("\n=== AFTER F MOVE ===")
        printAllFaces(cube)
        printAllCorners(cube)
        
        // Try to convert to CubeState
        println("\n=== ATTEMPTING CONVERSION ===")
        try {
            val state = CubeConverter.fromCube(cube)
            println("✅ Conversion successful!")
            println("CO sum: ${state.cornerOrientation.sum()}")
            println("EO sum: ${state.edgeOrientation.sum()}")
            println("CP: ${state.cornerPermutation.contentToString()}")
            println("EP: ${state.edgePermutation.contentToString()}")
        } catch (e: Exception) {
            println("❌ Conversion failed: ${e.message}")
        }
    }
    
    private fun printAllFaces(cube: Cube) {
        val faces = listOf("s1" to "F", "s2" to "U", "s3" to "D", "s4" to "L", "s5" to "R")
        for ((side, name) in faces) {
            println("$name face ($side):")
            for (row in 0..2) {
                print("  ")
                for (col in 0..2) {
                    print("${cube.getCell(side, row, col)} ")
                }
                println()
            }
        }
    }
    
    private fun printAllCorners(cube: Cube) {
        // Read facelets
        val facelets = IntArray(54)
        for (i in 0..8) facelets[i] = cube.getCell("s2", i / 3, i % 3)
        for (i in 0..8) facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)
        for (i in 0..8) facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)
        for (i in 0..8) facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)
        for (i in 0..8) facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)
        for (i in 0..8) facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)
        
        val corners = mapOf(
            "URF" to intArrayOf(2, 9, 20),
            "UFL" to intArrayOf(0, 18, 38),
            "ULB" to intArrayOf(6, 36, 47),
            "UBR" to intArrayOf(8, 45, 11),
            "DFR" to intArrayOf(29, 26, 15),
            "DLF" to intArrayOf(27, 44, 24),
            "DBL" to intArrayOf(33, 53, 42),
            "DRB" to intArrayOf(35, 17, 51)
        )
        
        println("\nCorner facelets:")
        for ((name, indices) in corners) {
            val colors = indices.map { facelets[it] }
            println("  $name: $colors")
        }
    }
}
