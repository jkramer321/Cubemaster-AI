package com.cs407.cubemaster

import com.cs407.cubemaster.data.Cube
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CubeTest {

    // Helper function to create a solved cube with distinct colors per face
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

    // Helper function to create a cube with unique values for tracking
    private fun createUniqueCube(): Cube {
        return Cube(
            s1 = mutableListOf(
                mutableListOf(11, 12, 13),
                mutableListOf(14, 15, 16),
                mutableListOf(17, 18, 19)
            ),
            s2 = mutableListOf(
                mutableListOf(21, 22, 23),
                mutableListOf(24, 25, 26),
                mutableListOf(27, 28, 29)
            ),
            s3 = mutableListOf(
                mutableListOf(31, 32, 33),
                mutableListOf(34, 35, 36),
                mutableListOf(37, 38, 39)
            ),
            s4 = mutableListOf(
                mutableListOf(41, 42, 43),
                mutableListOf(44, 45, 46),
                mutableListOf(47, 48, 49)
            ),
            s5 = mutableListOf(
                mutableListOf(51, 52, 53),
                mutableListOf(54, 55, 56),
                mutableListOf(57, 58, 59)
            ),
            s6 = mutableListOf(
                mutableListOf(61, 62, 63),
                mutableListOf(64, 65, 66),
                mutableListOf(67, 68, 69)
            )
        )
    }

    @Test
    fun testIsSolved_solvedCube() {
        val cube = createSolvedCube()
        assertTrue("Solved cube should return true", cube.isSolved())
    }

    @Test
    fun testIsSolved_unsolved() {
        val cube = createUniqueCube()
        assertFalse("Unique cube should not be solved", cube.isSolved())
    }

    // Setter tests removed - setCell, setRow, setCol are now private
    // The cube can only be modified through rotateCol() and rotateRow()

    // ========== COLUMN ROTATION TESTS ==========

    @Test
    fun testRotateCol_leftColumn_upOnce() {
        val cube = createUniqueCube()

        // Before: s1 col 0 = [11, 14, 17]
        assertEquals(listOf(11, 14, 17), cube.getCol("s1", 0))

        cube.rotateCol(0, true)

        // After rotating up: s1 col 0 should have values from s3 col 0
        // s3 col 0 = [31, 34, 37]
        assertEquals(listOf(31, 34, 37), cube.getCol("s1", 0))

        // s2 col 0 should have values from s1 col 0 (before rotation)
        assertEquals(listOf(11, 14, 17), cube.getCol("s2", 0))
    }

    @Test
    fun testRotateCol_leftColumn_fourTimesReturnsToOriginal() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate left column up 4 times (360 degrees)
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)

        // Should return to original state
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("s1[$row][$col] mismatch",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
                assertEquals("s2[$row][$col] mismatch",
                    originalState.getCell("s2", row, col),
                    cube.getCell("s2", row, col))
                assertEquals("s3[$row][$col] mismatch",
                    originalState.getCell("s3", row, col),
                    cube.getCell("s3", row, col))
                assertEquals("s4[$row][$col] mismatch",
                    originalState.getCell("s4", row, col),
                    cube.getCell("s4", row, col))
                assertEquals("s5[$row][$col] mismatch",
                    originalState.getCell("s5", row, col),
                    cube.getCell("s5", row, col))
                assertEquals("s6[$row][$col] mismatch",
                    originalState.getCell("s6", row, col),
                    cube.getCell("s6", row, col))
            }
        }
    }

    @Test
    fun testRotateCol_rightColumn_upOnce() {
        val cube = createUniqueCube()

        // Before: s1 col 2 = [13, 16, 19]
        assertEquals(listOf(13, 16, 19), cube.getCol("s1", 2))

        cube.rotateCol(2, true)

        // After rotating up: s1 col 2 should have values from s3 col 2
        assertEquals(listOf(33, 36, 39), cube.getCol("s1", 2))
    }

    @Test
    fun testRotateCol_rightColumn_fourTimesReturnsToOriginal() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate right column up 4 times
        for (i in 0..3) {
            cube.rotateCol(2, true)
        }

        // Should return to original state
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("s1[$row][$col] mismatch after 4 rotations",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testRotateCol_upThenDown() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate up then down should cancel out
        cube.rotateCol(0, true)
        cube.rotateCol(0, false)

        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("s1[$row][$col] mismatch",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testRotateCol_middleColumn_fourTimes() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Middle column (no face rotation)
        for (i in 0..3) {
            cube.rotateCol(1, true)
        }

        // Should return to original
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Middle column rotation failed at s1[$row][$col]",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testRotateCol_s6Reversal() {
        val cube = createUniqueCube()

        // Track a specific value through s6
        // s1 col 0 top = 11
        assertEquals(11, cube.getCell("s1", 0, 0))

        // Rotate up twice to get to s6
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)

        // Should now be at s6 col 2 (opposite column due to mirroring), BOTTOM position (due to reversal)
        // Values [11, 14, 17] are stored reversed in s6 as [17, 14, 11]
        // So top value 11 becomes bottom value
        assertEquals(11, cube.getCell("s6", 2, 2))
    }

    // ========== ROW ROTATION TESTS ==========

    @Test
    fun testRotateRow_topRow_rightOnce() {
        val cube = createUniqueCube()

        // Before: s1 row 0 = [11, 12, 13]
        assertEquals(listOf(11, 12, 13), cube.getRow("s1", 0))

        cube.rotateRow(0, true)

        // After rotating right: s1 row 0 should have values from s4 row 0
        // s4 row 0 = [41, 42, 43]
        assertEquals(listOf(41, 42, 43), cube.getRow("s1", 0))
    }

    @Test
    fun testRotateRow_topRow_fourTimesReturnsToOriginal() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate top row right 4 times
        for (i in 0..3) {
            cube.rotateRow(0, true)
        }

        // Should return to original state
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("s1[$row][$col] mismatch",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
                assertEquals("s2[$row][$col] mismatch",
                    originalState.getCell("s2", row, col),
                    cube.getCell("s2", row, col))
            }
        }
    }

    @Test
    fun testRotateRow_bottomRow_rightOnce() {
        val cube = createUniqueCube()

        // Before: s1 row 2 = [17, 18, 19]
        assertEquals(listOf(17, 18, 19), cube.getRow("s1", 2))

        cube.rotateRow(2, true)

        // After rotating right: s1 row 2 should have values from s4 row 2
        assertEquals(listOf(47, 48, 49), cube.getRow("s1", 2))
    }

    @Test
    fun testRotateRow_rightThenLeft() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate right then left should cancel out
        cube.rotateRow(0, true)
        cube.rotateRow(0, false)

        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("s1[$row][$col] mismatch",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testRotateRow_middleRow_fourTimes() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Middle row (no face rotation)
        for (i in 0..3) {
            cube.rotateRow(1, true)
        }

        // Should return to original
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Middle row rotation failed at s1[$row][$col]",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testRotateRow_s6Reversal() {
        val cube = createUniqueCube()

        // Track specific values through s6
        // s1 row 0 = [11, 12, 13]
        assertEquals(listOf(11, 12, 13), cube.getRow("s1", 0))

        // Rotate right twice to get to s6
        cube.rotateRow(0, true)
        cube.rotateRow(0, true)

        // Should now be at s6 row 0, but reversed: [13, 12, 11]
        assertEquals(listOf(13, 12, 11), cube.getRow("s6", 0))
    }

    // ========== FACE ROTATION TESTS ==========

    @Test
    fun testRotateCol_leftColumn_rotatesS4Face() {
        val cube = createUniqueCube()

        // Before: s4 top-left corner = 41
        assertEquals(41, cube.getCell("s4", 0, 0))

        cube.rotateCol(0, true)

        // After rotating col 0 up, s4 should rotate counterclockwise
        // Top-left (41) should move to bottom-left
        assertEquals(41, cube.getCell("s4", 2, 0))

        // Top-right (43) should move to top-left
        assertEquals(43, cube.getCell("s4", 0, 0))
    }

    @Test
    fun testRotateCol_rightColumn_rotatesS5Face() {
        val cube = createUniqueCube()

        // Before: s5 top-left corner = 51
        assertEquals(51, cube.getCell("s5", 0, 0))

        cube.rotateCol(2, true)

        // After rotating col 2 up, s5 should rotate clockwise
        // Top-left (51) should move to top-right
        assertEquals(51, cube.getCell("s5", 0, 2))

        // Bottom-left (57) should move to top-left
        assertEquals(57, cube.getCell("s5", 0, 0))
    }

    @Test
    fun testRotateRow_topRow_rotatesS2Face() {
        val cube = createUniqueCube()

        // Before: s2 top-left corner = 21
        assertEquals(21, cube.getCell("s2", 0, 0))

        cube.rotateRow(0, true)

        // After rotating row 0 right, s2 should rotate clockwise
        // Top-left (21) should move to top-right
        assertEquals(21, cube.getCell("s2", 0, 2))

        // Bottom-left (27) should move to top-left
        assertEquals(27, cube.getCell("s2", 0, 0))
    }

    @Test
    fun testRotateRow_bottomRow_rotatesS3Face() {
        val cube = createUniqueCube()

        // Before: s3 top-left corner = 31
        assertEquals(31, cube.getCell("s3", 0, 0))

        cube.rotateRow(2, true)

        // After rotating row 2 right, s3 should rotate clockwise
        // Top-left (31) should move to top-right
        assertEquals(31, cube.getCell("s3", 0, 2))
    }

    // ========== COMPLEX ROTATION TESTS ==========

    @Test
    fun testComplexRotation_eightRotations() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Perform 8 rotations of the same column (2 full cycles)
        for (i in 0..7) {
            cube.rotateCol(0, true)
        }

        // Should return to original state
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Failed after 8 rotations at s1[$row][$col]",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testComplexRotation_mixedRowsAndColumns() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Rotate col 0 up, col 2 up, col 0 down, col 2 down
        // Using non-overlapping rotations (col 0 and col 2 share no cells)
        cube.rotateCol(0, true)
        cube.rotateCol(2, true)
        cube.rotateCol(0, false)
        cube.rotateCol(2, false)

        // Should return to original state
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Mixed rotation failed at s1[$row][$col]",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testComplexRotation_allColumnsAndRows() {
        val cube = createUniqueCube()

        // Rotate all columns up once
        cube.rotateCol(0, true)
        cube.rotateCol(1, true)
        cube.rotateCol(2, true)

        // Then rotate all columns down once
        cube.rotateCol(0, false)
        cube.rotateCol(1, false)
        cube.rotateCol(2, false)

        // Rotate all rows right once
        cube.rotateRow(0, true)
        cube.rotateRow(1, true)
        cube.rotateRow(2, true)

        // Then rotate all rows left once
        cube.rotateRow(0, false)
        cube.rotateRow(1, false)
        cube.rotateRow(2, false)

        // Create a fresh cube - should match
        val expectedCube = createUniqueCube()

        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("All rotation test failed at s1[$row][$col]",
                    expectedCube.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testFreeze() {
        val cube = createUniqueCube()
        val frozen = cube.freeze()

        // Modify original
        cube.rotateCol(0, true)

        // Frozen should remain unchanged
        assertEquals(11, frozen.getCell("s1", 0, 0))
        assertNotEquals(cube.getCell("s1", 0, 0), frozen.getCell("s1", 0, 0))
    }

    @Test
    fun testSolvedCube_remainsSolvedAfterFullRotation() {
        val cube = createSolvedCube()

        // Four rotations should keep it solved
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)
        cube.rotateCol(0, true)

        assertTrue("Cube should still be solved after 4 rotations", cube.isSolved())
    }

    // ========== STRESS TESTS ==========

    @Test
    fun testStress_manyRotations() {
        val cube = createUniqueCube()
        val originalState = cube.freeze()

        // Perform a sequence that should return to original
        // (col 0 up * 4) + (row 0 right * 4) = full cycles
        repeat(4) { cube.rotateCol(0, true) }
        repeat(4) { cube.rotateRow(0, true) }

        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Stress test failed at s1[$row][$col]",
                    originalState.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testStress_randomSequence() {
        val cube = createUniqueCube()

        // Perform random rotations and their inverses
        cube.rotateCol(0, true)
        cube.rotateRow(1, false)
        cube.rotateCol(2, false)
        cube.rotateRow(0, true)

        // Now reverse them all
        cube.rotateRow(0, false)
        cube.rotateCol(2, true)
        cube.rotateRow(1, true)
        cube.rotateCol(0, false)

        val expectedCube = createUniqueCube()

        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals("Random sequence test failed at s1[$row][$col]",
                    expectedCube.getCell("s1", row, col),
                    cube.getCell("s1", row, col))
            }
        }
    }

    @Test
    fun testDebug_singleColumnRotation() {
        val cube = createUniqueCube()

        println("=== BEFORE ROTATION ===")
        println("s1 col 0: ${cube.getCol("s1", 0)}")
        println("s2 col 0: ${cube.getCol("s2", 0)}")
        println("s6 col 2: ${cube.getCol("s6", 2)}")
        println("s6 col 0: ${cube.getCol("s6", 0)}")
        println("s3 col 0: ${cube.getCol("s3", 0)}")

        cube.rotateCol(0, true)

        println("\n=== AFTER ROTATING COL 0 UP ===")
        println("s1 col 0: ${cube.getCol("s1", 0)} (should be [31, 34, 37] from s3)")
        println("s2 col 0: ${cube.getCol("s2", 0)} (should be [11, 14, 17] from s1)")
        println("s6 col 2: ${cube.getCol("s6", 2)} (should be reversed from s2)")
        println("s6 col 0: ${cube.getCol("s6", 0)}")
        println("s3 col 0: ${cube.getCol("s3", 0)} (should be from s6 col 2 reversed)")
    }

    @Test
    fun testDebug_singleRowRotation() {
        val cube = createUniqueCube()

        println("=== BEFORE ROTATION ===")
        println("s1 row 0: ${cube.getRow("s1", 0)}")
        println("s5 row 0: ${cube.getRow("s5", 0)}")
        println("s6 row 0: ${cube.getRow("s6", 0)}")
        println("s4 row 0: ${cube.getRow("s4", 0)}")

        cube.rotateRow(0, true)

        println("\n=== AFTER ROTATING ROW 0 RIGHT ===")
        println("s1 row 0: ${cube.getRow("s1", 0)} (should be [41, 42, 43] from s4)")
        println("s5 row 0: ${cube.getRow("s5", 0)} (should be [11, 12, 13] from s1)")
        println("s6 row 0: ${cube.getRow("s6", 0)} (should be [51, 52, 53] from s5, but reversed)")
        println("s4 row 0: ${cube.getRow("s4", 0)} (should be from s6 reversed)")
    }

    @Test
    fun testDebug_fourColumnRotations() {
        val cube = createUniqueCube()

        println("=== INITIAL STATE ===")
        println("s1 col 0: ${cube.getCol("s1", 0)}")

        for (i in 1..4) {
            cube.rotateCol(0, true)
            println("\n=== AFTER ROTATION $i ===")
            println("s1 col 0: ${cube.getCol("s1", 0)}")
            println("s2 col 0: ${cube.getCol("s2", 0)}")
            println("s6 col 2: ${cube.getCol("s6", 2)}")
            println("s3 col 0: ${cube.getCol("s3", 0)}")
        }

        println("\n=== FINAL (should match initial) ===")
        println("s1 col 0: ${cube.getCol("s1", 0)} (should be [11, 14, 17])")
    }

    @Test
    fun testDebug_fourRowRotations() {
        val cube = createUniqueCube()

        println("=== INITIAL STATE ===")
        println("s1 row 0: ${cube.getRow("s1", 0)}")

        for (i in 1..4) {
            cube.rotateRow(0, true)
            println("\n=== AFTER ROTATION $i ===")
            println("s1 row 0: ${cube.getRow("s1", 0)}")
            println("s5 row 0: ${cube.getRow("s5", 0)}")
            println("s6 row 0: ${cube.getRow("s6", 0)}")
            println("s4 row 0: ${cube.getRow("s4", 0)}")
        }

        println("\n=== FINAL (should match initial) ===")
        println("s1 row 0: ${cube.getRow("s1", 0)} (should be [11, 12, 13])")
    }

    @Test
    fun testDebug_trackSingleValue() {
        val cube = createUniqueCube()

        println("=== TRACKING VALUE 11 (s1[0][0]) through column rotation ===")
        println("Initial: s1[0][0] = ${cube.getCell("s1", 0, 0)}")

        cube.rotateCol(0, true)
        println("After 1 rotation up: s2[0][0] = ${cube.getCell("s2", 0, 0)} (should be 11)")

        cube.rotateCol(0, true)
        println("After 2 rotations up: s6[0][2] = ${cube.getCell("s6", 0, 2)} (should be 11)")

        cube.rotateCol(0, true)
        println("After 3 rotations up: s3[0][0] = ${cube.getCell("s3", 0, 0)} (should be 11)")

        cube.rotateCol(0, true)
        println("After 4 rotations up: s1[0][0] = ${cube.getCell("s1", 0, 0)} (should be 11)")
    }
}