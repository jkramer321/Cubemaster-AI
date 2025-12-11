package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class VerifyEdgeIndicesTest {
    
    private fun createSolvedCube(): Cube {
        return Cube(
            s1 = mutableListOf(mutableListOf(2, 2, 2), mutableListOf(2, 2, 2), mutableListOf(2, 2, 2)), // Front
            s2 = mutableListOf(mutableListOf(0, 0, 0), mutableListOf(0, 0, 0), mutableListOf(0, 0, 0)), // Up
            s3 = mutableListOf(mutableListOf(3, 3, 3), mutableListOf(3, 3, 3), mutableListOf(3, 3, 3)), // Down
            s4 = mutableListOf(mutableListOf(4, 4, 4), mutableListOf(4, 4, 4), mutableListOf(4, 4, 4)), // Left
            s5 = mutableListOf(mutableListOf(1, 1, 1), mutableListOf(1, 1, 1), mutableListOf(1, 1, 1)), // Right
            s6 = mutableListOf(mutableListOf(5, 5, 5), mutableListOf(5, 5, 5), mutableListOf(5, 5, 5))  // Back
        )
    }
    
    @Test
    fun verifyAllEdgeIndices() {
        val cube = createSolvedCube()
        
        // Color mapping (URFDLB): U=0, R=1, F=2, D=3, L=4, B=5
        
        // Expected edge colors in solved state
        val expectedEdges = mapOf(
            "UR" to listOf(0, 1),  // U, R
            "UF" to listOf(0, 2),  // U, F
            "UL" to listOf(0, 4),  // U, L
            "UB" to listOf(0, 5),  // U, B
            "DR" to listOf(3, 1),  // D, R
            "DF" to listOf(3, 2),  // D, F
            "DL" to listOf(3, 4),  // D, L
            "DB" to listOf(3, 5),  // D, B
            "FR" to listOf(2, 1),  // F, R
            "FL" to listOf(2, 4),  // F, L
            "BL" to listOf(5, 4),  // B, L
            "BR" to listOf(5, 1)   // B, R
        )
        
        // Current indices from CubeConverter
        val currentIndices = mapOf(
            "UR" to intArrayOf(5, 10),
            "UF" to intArrayOf(7, 19),
            "UL" to intArrayOf(3, 37),
            "UB" to intArrayOf(1, 46),
            "DR" to intArrayOf(32, 16),
            "DF" to intArrayOf(28, 25),
            "DL" to intArrayOf(30, 43),
            "DB" to intArrayOf(34, 52),
            "FR" to intArrayOf(23, 12),
            "FL" to intArrayOf(21, 41),
            "BL" to intArrayOf(50, 39),
            "BR" to intArrayOf(48, 14)
        )
        
        // Read facelets
        val facelets = IntArray(54)
        for (i in 0..8) facelets[i] = cube.getCell("s2", i / 3, i % 3)  // U
        for (i in 0..8) facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)  // R
        for (i in 0..8) facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)  // F
        for (i in 0..8) facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)  // D
        for (i in 0..8) facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)  // L
        for (i in 0..8) facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)  // B
        
        println("Verifying edge facelet indices:\n")
        
        var allCorrect = true
        for ((name, expectedColors) in expectedEdges) {
            val indices = currentIndices[name]!!
            val actualColors = indices.map { facelets[it] }
            
            val match = actualColors == expectedColors
            val status = if (match) "✓" else "✗"
            
            println("$name: $status ${if (match) "CORRECT" else "WRONG"}")
            println("  Indices: ${indices.contentToString()}")
            println("  Expected: $expectedColors")
            println("  Actual:   $actualColors")
            
            if (!match) {
                allCorrect = false
            }
            println()
        }
        
        if (allCorrect) {
            println(" ALL EDGE INDICES ARE CORRECT!")
        } else {
            println(" SOME EDGE INDICES ARE INCORRECT - SEE ABOVE")
        }
    }
}
