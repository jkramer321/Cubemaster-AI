package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

/**
 * Systematically verify corner facelet indices by checking a solved cube
 */
class VerifyCornerIndicesTest {
    
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
    
    @Test
    fun verifyAllCornerIndices() {
        val cube = createSolvedCube()
        
        // Color mapping: U=2, R=5, F=1, D=3, L=4, B=6
        
        // Expected corner colors in solved state
        val expectedCorners = mapOf(
            "URF" to listOf(2, 5, 1),  // U, R, F
            "UFL" to listOf(2, 1, 4),  // U, F, L
            "ULB" to listOf(2, 4, 6),  // U, L, B
            "UBR" to listOf(2, 6, 5),  // U, B, R
            "DFR" to listOf(3, 1, 5),  // D, F, R
            "DLF" to listOf(3, 4, 1),  // D, L, F
            "DBL" to listOf(3, 6, 4),  // D, B, L
            "DRB" to listOf(3, 5, 6)   // D, R, B
        )
        
        // Current indices from CubeConverter
        val currentIndices = mapOf(
            "URF" to intArrayOf(2, 9, 20),
            "UFL" to intArrayOf(0, 18, 38),
            "ULB" to intArrayOf(6, 36, 47),
            "UBR" to intArrayOf(8, 45, 11),
            "DFR" to intArrayOf(29, 26, 15),
            "DLF" to intArrayOf(27, 44, 24),
            "DBL" to intArrayOf(33, 51, 42),
            "DRB" to intArrayOf(35, 17, 53)
        )
        
        // Read facelets
        val facelets = IntArray(54)
        for (i in 0..8) facelets[i] = cube.getCell("s2", i / 3, i % 3)  // U
        for (i in 0..8) facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)  // R
        for (i in 0..8) facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)  // F
        for (i in 0..8) facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)  // D
        for (i in 0..8) facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)  // L
        for (i in 0..8) facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)  // B
        
        println("Verifying corner facelet indices:\n")
        
        for ((name, expectedColors) in expectedCorners) {
            val indices = currentIndices[name]!!
            val actualColors = indices.map { facelets[it] }
            
            val match = actualColors == expectedColors
            val status = if (match) "✓ CORRECT" else "✗ WRONG"
            
            println("$name: $status")
            println("  Indices: ${indices.contentToString()}")
            println("  Expected: $expectedColors")
            println("  Actual:   $actualColors")
            
            if (!match) {
                // Try to find correct indices
                println("  Searching for correct indices...")
                for (i in facelets.indices) {
                    for (j in facelets.indices) {
                        for (k in facelets.indices) {
                            if (listOf(facelets[i], facelets[j], facelets[k]) == expectedColors) {
                                println("  Found: [$i, $j, $k]")
                                break
                            }
                        }
                    }
                }
            }
            println()
        }
    }
}
