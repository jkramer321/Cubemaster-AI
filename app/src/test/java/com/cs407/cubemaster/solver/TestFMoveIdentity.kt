package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class TestFMoveIdentity {
    
    @Test
    fun testFFFFReturnToSolved() {
        val cube = Cube(
            s1 = mutableListOf(mutableListOf(1, 1, 1), mutableListOf(1, 1, 1), mutableListOf(1, 1, 1)),
            s2 = mutableListOf(mutableListOf(2, 2, 2), mutableListOf(2, 2, 2), mutableListOf(2, 2, 2)),
            s3 = mutableListOf(mutableListOf(3, 3, 3), mutableListOf(3, 3, 3), mutableListOf(3, 3, 3)),
            s4 = mutableListOf(mutableListOf(4, 4, 4), mutableListOf(4, 4, 4), mutableListOf(4, 4, 4)),
            s5 = mutableListOf(mutableListOf(5, 5, 5), mutableListOf(5, 5, 5), mutableListOf(5, 5, 5)),
            s6 = mutableListOf(mutableListOf(6, 6, 6), mutableListOf(6, 6, 6), mutableListOf(6, 6, 6))
        )
        
        // Apply F 4 times - should return to solved
        CubeConverter.applyMoveString(cube, "F F F F")
        
        assertTrue("Cube should be solved after F F F F", cube.isSolved())
    }
    
    @Test
    fun testRURprimeUprime() {
        val cube = Cube(
            s1 = mutableListOf(mutableListOf(1, 1, 1), mutableListOf(1, 1, 1), mutableListOf(1, 1, 1)),
            s2 = mutableListOf(mutableListOf(2, 2, 2), mutableListOf(2, 2, 2), mutableListOf(2, 2, 2)),
            s3 = mutableListOf(mutableListOf(3, 3, 3), mutableListOf(3, 3, 3), mutableListOf(3, 3, 3)),
            s4 = mutableListOf(mutableListOf(4, 4, 4), mutableListOf(4, 4, 4), mutableListOf(4, 4, 4)),
            s5 = mutableListOf(mutableListOf(5, 5, 5), mutableListOf(5, 5, 5), mutableListOf(5, 5, 5)),
            s6 = mutableListOf(mutableListOf(6, 6, 6), mutableListOf(6, 6, 6), mutableListOf(6, 6, 6))
        )
        
        // Apply R U R' U' - should produce valid state at each step
        println("Applying R...")
        CubeConverter.applyMoveString(cube, "R")
        val state1 = CubeConverter.fromCube(cube)
        println("After R: Valid!")
        
        println("Applying U...")
        CubeConverter.applyMoveString(cube, "U")
        val state2 = CubeConverter.fromCube(cube)
        println("After R U: Valid!")
        
        println("Applying R'...")
        CubeConverter.applyMoveString(cube, "R'")
        val state3 = CubeConverter.fromCube(cube)
        println("After R U R': Valid!")
        
        println("Applying U'...")
        CubeConverter.applyMoveString(cube, "U'")
        val state4 = CubeConverter.fromCube(cube)
        println("After R U R' U': Valid!")
        
        println("✅ R U R' U' sequence works!")
    }
}
