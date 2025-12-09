package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class DebugScrambledTest {

    @Test
    fun debugScrambledCube() {
        val cube = createSolvedCube()
        
        // Apply F
        CubeConverter.applyMoveString(cube, "F")
        
        val result = CubeValidator.validate(cube)
        
        if (!result.isValid && result.errorCode != CubeValidator.ValidationResult.ErrorCode.DUPLICATE_CORNER) {
            val state = CubeConverter.fromCube(cube)
            val debugInfo = """
                Validation failed!
                Error code: ${result.errorCode}
                Error message: ${result.errorMessage}
                Corner Perm: ${state.cornerPermutation.contentToString()}
                Corner Ori: ${state.cornerOrientation.contentToString()}
                Edge Perm: ${state.edgePermutation.contentToString()}
                Edge Ori: ${state.edgeOrientation.contentToString()}
            """.trimIndent()
            throw RuntimeException(debugInfo)
        }
        
        // Check edges specifically
        val state = CubeConverter.fromCube(cube)
        // R U R' F
        // Edges should be permuted.
        // Let's just print them.
        println("Edge Perm: ${state.edgePermutation.contentToString()}")
        println("Edge Ori: ${state.edgeOrientation.contentToString()}")
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
