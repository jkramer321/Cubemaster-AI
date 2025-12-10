package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class TestMoveIdentities {
    
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
    fun testAllMoveIdentities() {
        val moves = listOf("U", "D", "R", "L", "F", "B")
        
        for (move in moves) {
            val cube = createSolvedCube()
            // Apply move 4 times - should return to solved
            repeat(4) {
                CubeConverter.applyMoveString(cube, move)
            }
            assertTrue("Cube should be solved after $move x4", cube.isSolved())
            println("✅ $move x4 = identity")
        }
    }
    
    @Test
    fun testRUSequence() {
        val cube = createSolvedCube()
        
        // Apply R U 6 times - should return to solved (R U has order 6)
        repeat(6) {
            CubeConverter.applyMoveString(cube, "R U")
        }
        
        assertTrue("Cube should be solved after (R U)x6", cube.isSolved())
        println("✅ (R U)x6 = identity")
    }
}
