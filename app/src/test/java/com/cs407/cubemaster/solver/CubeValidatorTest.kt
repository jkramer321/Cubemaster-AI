package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class CubeValidatorTest {

    @Test
    fun testSolvedCubeIsValid() {
        val cube = createSolvedCube()
        val result = CubeValidator.validate(cube)
        assertTrue("Solved cube should be valid", result.isValid)
        assertNull("Valid cube should have no error", result.errorCode)
    }

    @Test
    fun testScrambledCubeIsValid() {
        val cube = createSolvedCube()

        // Apply some moves
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "F")

        val result = CubeValidator.validate(cube)
        assertTrue("Scrambled cube should be valid", result.isValid)
    }

    @Test
    fun testInvalidColorCount() {
        val cube = Cube(
            s1 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s2 = mutableListOf(
                mutableListOf(1, 1, 1),  // All same color - invalid
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
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

        val result = CubeValidator.validate(cube)
        assertFalse("Cube with wrong color count should be invalid", result.isValid)
        assertEquals(
            CubeValidator.ValidationResult.ErrorCode.INVALID_COLOR_COUNT,
            result.errorCode
        )
    }

    @Test
    fun testQuickValidate() {
        val validCube = createSolvedCube()
        assertTrue("Quick validate should pass for valid cube", CubeValidator.quickValidate(validCube))

        val invalidCube = Cube(
            s1 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s2 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s3 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s4 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s5 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            ),
            s6 = mutableListOf(
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1),
                mutableListOf(1, 1, 1)
            )
        )

        assertFalse("Quick validate should fail for invalid cube", CubeValidator.quickValidate(invalidCube))
    }

    @Test
    fun testGetErrorMessage() {
        val validResult = CubeValidator.ValidationResult(true)
        assertEquals("Cube is valid", CubeValidator.getErrorMessage(validResult))

        val invalidResult = CubeValidator.ValidationResult(
            false,
            CubeValidator.ValidationResult.ErrorCode.INVALID_COLOR_COUNT,
            "Color 1 appears 18 times (expected 9)"
        )

        val message = CubeValidator.getErrorMessage(invalidResult)
        assertTrue("Error message should mention color distribution", message.contains("color"))
        assertTrue("Error message should mention the specific error", message.contains("18"))
    }

    @Test
    fun testMultipleMoves() {
        val cube = createSolvedCube()

        // Apply a series of moves
        val moves = listOf("R", "U", "R'", "U'", "R'", "F", "R2", "U'", "R'", "U'", "R", "U", "R'", "F'")
        for (move in moves) {
            CubeConverter.applyMoveString(cube, move)
        }

        val result = CubeValidator.validate(cube)
        assertTrue("Cube after valid move sequence should be valid", result.isValid)
    }

    @Test
    fun testCornerOrientationConstraint() {
        val cube = createSolvedCube()

        // Apply moves that should maintain corner orientation constraint
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")

        val result = CubeValidator.validate(cube)
        assertTrue("Cube should maintain corner orientation constraint", result.isValid)
    }

    @Test
    fun testEdgeOrientationConstraint() {
        val cube = createSolvedCube()

        // Apply moves that should maintain edge orientation constraint
        CubeConverter.applyMoveString(cube, "F")
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")

        val result = CubeValidator.validate(cube)
        assertTrue("Cube should maintain edge orientation constraint", result.isValid)
    }

    @Test
    fun testPermutationParityConstraint() {
        val cube = createSolvedCube()

        // Apply a series of moves (should maintain parity)
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R2")
        CubeConverter.applyMoveString(cube, "U'")
        CubeConverter.applyMoveString(cube, "R'")

        val result = CubeValidator.validate(cube)
        assertTrue("Cube should maintain permutation parity", result.isValid)
    }

    @Test
    fun testValidationAfterManyMoves() {
        val cube = createSolvedCube()

        // Apply 100 random-ish moves
        val moves = listOf("R", "U", "F", "L", "D", "B")
        for (i in 1..100) {
            val move = moves[i % moves.size]
            CubeConverter.applyMoveString(cube, move)
        }

        val result = CubeValidator.validate(cube)
        assertTrue("Cube should remain valid after many moves", result.isValid)
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
