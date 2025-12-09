package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class VerifyEdgeIndicesTest {
    
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
    fun verifyAllEdgeIndices() {
        val cube = createSolvedCube()
        
        // Color mapping: U=2, R=5, F=1, D=3, L=4, B=6
        
        // Expected edge colors in solved state
        val expectedEdges = mapOf(
            "UR" to listOf(2, 5),  // U, R
            "UF" to listOf(2, 1),  // U, F
            "UL" to listOf(2, 4),  // U, L
            "UB" to listOf(2, 6),  // U, B
            "DR" to listOf(3, 5),  // D, R
            "DF" to listOf(3, 1),  // D, F
            "DL" to listOf(3, 4),  // D, L
            "DB" to listOf(3, 6),  // D, B
            "FR" to listOf(1, 5),  // F, R
            "FL" to listOf(1, 4),  // F, L
            "BL" to listOf(6, 4),  // B, L
            "BR" to listOf(6, 5)   // B, R
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
