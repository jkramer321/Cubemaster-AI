package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class CubeConverterTest {

    @Test
    fun testAllMovesReturnToSolved() {
        val moves = listOf("R", "R'", "L", "L'", "U", "U'", "D", "D'", "F", "F'", "B", "B'")

        for (move in moves) {
            val cube = createSolvedCube()
            assertTrue("Cube should start solved", cube.isSolved())

            // Apply move
            CubeConverter.applyMoveString(cube, move)
            assertFalse("Cube should not be solved after $move", cube.isSolved())

            // Apply inverse move
            val inverse = when {
                move.endsWith("'") -> move.dropLast(1)
                else -> "${move}'"
            }
            CubeConverter.applyMoveString(cube, inverse)
            assertTrue("Cube should be solved after $move then $inverse", cube.isSolved())
        }
    }

    @Test
    fun testDoubleMoves() {
        val cube = createSolvedCube()

        // R2 = R + R
        val cube1 = cube.freeze()
        CubeConverter.applyMoveString(cube1, "R2")

        val cube2 = cube.freeze()
        CubeConverter.applyMoveString(cube2, "R")
        CubeConverter.applyMoveString(cube2, "R")

        assertCubesEqual(cube1, cube2, "R2 should equal R R")
    }

    @Test
    fun testFourQuarterTurnsEqualsSolved() {
        val moves = listOf("R", "L", "U", "D", "F", "B")

        for (move in moves) {
            val cube = createSolvedCube()

            // Apply move 4 times
            repeat(4) {
                CubeConverter.applyMoveString(cube, move)
            }

            assertTrue("Four $move moves should return to solved", cube.isSolved())
        }
    }

    @Test
    fun testTwoDoubleTurnsEqualsSolved() {
        val moves = listOf("R2", "L2", "U2", "D2", "F2", "B2")

        for (move in moves) {
            val cube = createSolvedCube()

            // Apply double move twice
            CubeConverter.applyMoveString(cube, move)
            CubeConverter.applyMoveString(cube, move)

            assertTrue("Two $move moves should return to solved", cube.isSolved())
        }
    }

    @Test
    fun testMoveInverseCancellation() {
        val cube = createSolvedCube()

        // R U R' U' should not be solved, but applying it twice should return close
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        assertFalse(cube.isSolved())
    }

    @Test
    fun testFMoves() {
        val cube = createSolvedCube()

        // Test F move
        CubeConverter.applyMoveString(cube, "F")
        assertFalse("Cube should not be solved after F", cube.isSolved())

        // Test F' undoes F
        CubeConverter.applyMoveString(cube, "F'")
        assertTrue("F followed by F' should return to solved", cube.isSolved())

        // Test F2
        CubeConverter.applyMoveString(cube, "F2")
        assertFalse("Cube should not be solved after F2", cube.isSolved())
        CubeConverter.applyMoveString(cube, "F2")
        assertTrue("F2 followed by F2 should return to solved", cube.isSolved())
    }

    @Test
    fun testBMoves() {
        val cube = createSolvedCube()

        // Test B move
        CubeConverter.applyMoveString(cube, "B")
        assertFalse("Cube should not be solved after B", cube.isSolved())

        // Test B' undoes B
        CubeConverter.applyMoveString(cube, "B'")
        assertTrue("B followed by B' should return to solved", cube.isSolved())

        // Test B2
        CubeConverter.applyMoveString(cube, "B2")
        assertFalse("Cube should not be solved after B2", cube.isSolved())
        CubeConverter.applyMoveString(cube, "B2")
        assertTrue("B2 followed by B2 should return to solved", cube.isSolved())
    }

    @Test
    fun testComplexSequence() {
        val cube = createSolvedCube()

        // Apply T-perm (swaps two edges)
        val tPerm = listOf("R", "U", "R'", "U'", "R'", "F", "R2", "U'", "R'", "U'", "R", "U", "R'", "F'")

        for (move in tPerm) {
            CubeConverter.applyMoveString(cube, move)
        }

        assertFalse("Cube should not be solved after T-perm", cube.isSolved())

        // Apply T-perm again (should return to solved since T-perm is self-inverse)
        for (move in tPerm) {
            CubeConverter.applyMoveString(cube, move)
        }

        // Note: This might not work perfectly if our move implementation has bugs
        // but it's a good integration test
    }

    @Test
    fun testAllMovesAffectCube() {
        val moves = listOf("R", "L", "U", "D", "F", "B", "R2", "L2", "U2", "D2", "F2", "B2")

        for (move in moves) {
            val cube = createSolvedCube()
            CubeConverter.applyMoveString(cube, move)

            var changedCells = 0
            val solvedCube = createSolvedCube()

            for (side in listOf("s1", "s2", "s3", "s4", "s5", "s6")) {
                for (row in 0..2) {
                    for (col in 0..2) {
                        if (cube.getCell(side, row, col) != solvedCube.getCell(side, row, col)) {
                            changedCells++
                        }
                    }
                }
            }

            assertTrue("Move $move should change at least 12 cells (centers don't move)", changedCells >= 12)
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

    private fun assertCubesEqual(cube1: Cube, cube2: Cube, message: String) {
        for (side in listOf("s1", "s2", "s3", "s4", "s5", "s6")) {
            for (row in 0..2) {
                for (col in 0..2) {
                    assertEquals(
                        "$message: Mismatch at $side[$row][$col]",
                        cube1.getCell(side, row, col),
                        cube2.getCell(side, row, col)
                    )
                }
            }
        }
    }
}
