package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

/**
 * Test R move against known correct output
 * 
 * Standard R move (clockwise when viewing right face):
 * - Right face rotates clockwise
 * - Right column cycles: U -> B -> D -> F -> U
 * 
 * For a solved cube (all faces same color):
 * After R move, the right column of each face should have:
 * - U gets F (1)
 * - B gets U (2) 
 * - D gets B (6)
 * - F gets D (3)
 */
class ReferenceRMoveTest {
    
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
    fun testRMoveExpectedOutput() {
        val cube = createSolvedCube()
        
        // Apply R move
        CubeConverter.applyMoveString(cube, "R")
        
        println("=== EXPECTED vs ACTUAL ===\n")
        
        // Check F face (s1) - right column should be D (3)
        println("F face right column (col 2):")
        println("  Expected: [3, 3, 3]")
        val fCol = listOf(cube.getCell("s1", 0, 2), cube.getCell("s1", 1, 2), cube.getCell("s1", 2, 2))
        println("  Actual:   $fCol")
        assertEquals("F right column should be D", listOf(3, 3, 3), fCol)
        
        // Check U face (s2) - right column should be F (1)
        println("\nU face right column (col 2):")
        println("  Expected: [1, 1, 1]")
        val uCol = listOf(cube.getCell("s2", 0, 2), cube.getCell("s2", 1, 2), cube.getCell("s2", 2, 2))
        println("  Actual:   $uCol")
        assertEquals("U right column should be F", listOf(1, 1, 1), uCol)
        
        // Check D face (s3) - right column should be B (6)
        println("\nD face right column (col 2):")
        println("  Expected: [6, 6, 6]")
        val dCol = listOf(cube.getCell("s3", 0, 2), cube.getCell("s3", 1, 2), cube.getCell("s3", 2, 2))
        println("  Actual:   $dCol")
        assertEquals("D right column should be B", listOf(6, 6, 6), dCol)
        
        // Check B face (s6) - which column should have U (2)?
        // B is mirrored, so right side from FRONT is LEFT side from BEHIND
        // That's col 0 when viewing from behind
        println("\nB face col 0 (right from front, left from behind):")
        println("  Expected: [2, 2, 2]")
        val bCol0 = listOf(cube.getCell("s6", 0, 0), cube.getCell("s6", 1, 0), cube.getCell("s6", 2, 0))
        println("  Actual:   $bCol0")
        
        println("\nB face col 2 (left from front, right from behind):")
        val bCol2 = listOf(cube.getCell("s6", 0, 2), cube.getCell("s6", 1, 2), cube.getCell("s6", 2, 2))
        println("  Actual:   $bCol2")
        
        // The correct column should have U color
        val hasUInCol0 = bCol0 == listOf(2, 2, 2)
        val hasUInCol2 = bCol2 == listOf(2, 2, 2)
        
        println("\nB face has U in col 0: $hasUInCol0")
        println("B face has U in col 2: $hasUInCol2")
        
        assertTrue("B face should have U in exactly one column", hasUInCol0 xor hasUInCol2)
        
        if (hasUInCol0) {
            println("\n✓ CORRECT: B face col 0 has U (swap IS needed)")
        } else {
            println("\n✓ CORRECT: B face col 2 has U (swap NOT needed)")
        }
    }
}
