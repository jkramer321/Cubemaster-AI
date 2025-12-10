package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test
import org.junit.Assert.*

class MoveApplicationTest {

    @Test
    fun testMoveU() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "U")
        // U: F -> L -> B -> R -> F
        // s1(F) top row should be s5(R) top row (5,5,5)
        assertEquals(5, cube.getCell("s1", 0, 0))
        assertEquals(5, cube.getCell("s1", 0, 1))
        assertEquals(5, cube.getCell("s1", 0, 2))
        // s4(L) top row should be s1(F) top row (1,1,1)
        assertEquals(1, cube.getCell("s4", 0, 0))
    }

    @Test
    fun testMoveD() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "D")
        // D: F -> R -> B -> L -> F
        // s1(F) bottom row should be s4(L) bottom row (4,4,4)
        assertEquals(4, cube.getCell("s1", 2, 0))
        assertEquals(4, cube.getCell("s1", 2, 1))
        assertEquals(4, cube.getCell("s1", 2, 2))
        // s5(R) bottom row should be s1(F) bottom row (1,1,1)
        assertEquals(1, cube.getCell("s5", 2, 0))
    }

    @Test
    fun testMoveR() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "R")
        // R: D -> F -> U -> B -> D
        // s1(F) right col should be s3(D) right col (3,3,3)
        assertEquals(3, cube.getCell("s1", 0, 2))
        assertEquals(3, cube.getCell("s1", 1, 2))
        assertEquals(3, cube.getCell("s1", 2, 2))
        // s2(U) right col should be s1(F) right col (1,1,1)
        assertEquals(1, cube.getCell("s2", 0, 2))
    }

    @Test
    fun testMoveL() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "L")
        // L: U -> F -> D -> B -> U
        // s1(F) left col should be s2(U) left col (2,2,2)
        assertEquals(2, cube.getCell("s1", 0, 0))
        assertEquals(2, cube.getCell("s1", 1, 0))
        assertEquals(2, cube.getCell("s1", 2, 0))
        // s3(D) left col should be s1(F) left col (1,1,1)
        assertEquals(1, cube.getCell("s3", 0, 0))
    }

    @Test
    fun testMoveF() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "F")
        // F: U bottom -> R left -> D top -> L right -> U bottom
        // s5(R) left col should be s2(U) bottom row (2,2,2)
        assertEquals(2, cube.getCell("s5", 0, 0))
        assertEquals(2, cube.getCell("s5", 1, 0))
        assertEquals(2, cube.getCell("s5", 2, 0))
        // s3(D) top row should be s5(R) left col (5,5,5) (Reversed? R(2,0)->D(0,0))
        // R left is 5,5,5. Reversed is 5,5,5.
        assertEquals(5, cube.getCell("s3", 0, 0))
        assertEquals(5, cube.getCell("s3", 0, 1))
        assertEquals(5, cube.getCell("s3", 0, 2))
    }

    @Test
    fun testMoveB() {
        val cube = createSolvedCube()
        CubeConverter.applyMoveString(cube, "B")
        // B: U top -> L left -> D bottom -> R right -> U top
        // s4(L) left col should be s2(U) top row (2,2,2) (Reversed? U(0,2)->L(0,0))
        // U top is 2,2,2. Reversed is 2,2,2.
        assertEquals(2, cube.getCell("s4", 0, 0))
        assertEquals(2, cube.getCell("s4", 1, 0))
        assertEquals(2, cube.getCell("s4", 2, 0))
        // s3(D) bottom row should be s4(L) left col (4,4,4) (Straight? L(0,0)->D(2,0))
        // L left is 4,4,4.
        assertEquals(4, cube.getCell("s3", 2, 0))
        assertEquals(4, cube.getCell("s3", 2, 1))
        assertEquals(4, cube.getCell("s3", 2, 2))
    }

    @Test
    fun testTraceRURU() {
        val cube = createSolvedCube()
        // Scramble: R U R' U'
        
        // R
        CubeConverter.applyMoveString(cube, "R")
        println("After R:")
        println("UR pos: ${cube.getCell("s2", 1, 2)}, ${cube.getCell("s5", 0, 1)}")
        
        // U
        CubeConverter.applyMoveString(cube, "U")
        
        // R'
        CubeConverter.applyMoveString(cube, "R'")
        
        // U'
        CubeConverter.applyMoveString(cube, "U'")
        
        println("Final State:")
        println("s1 (F): ${cube.s1}")
        println("s2 (U): ${cube.s2}")
        println("s3 (D): ${cube.s3}")
        println("s4 (L): ${cube.s4}")
        println("s5 (R): ${cube.s5}")
        println("s6 (B): ${cube.s6}")
    }

    @Test
    fun testTraceBFace() {
        val cube = createSolvedCube()
        
        // Mark U facelets with unique values to trace them
        // U face is s2.
        // U(0,2) (UBR) -> 20.
        // U(1,2) (UR) -> 21.
        // U(2,2) (URF) -> 22.
        cube.s2[0][2] = 20
        cube.s2[1][2] = 21
        cube.s2[2][2] = 22
        
        println("Before R:")
        println("s2 (U) right col: ${cube.getCol("s2", 2).toList()}")
        println("s6 (B) left col: ${cube.getCol("s6", 2).toList()}")
        
        // Apply R
        CubeConverter.applyMoveString(cube, "R")
        
        println("After R:")
        println("s6 (B) left col: ${cube.getCol("s6", 2).toList()}")
        
        // Expected:
        // UBR (20) -> DRB (Bottom-Left of B Back view -> Bottom-Right of B Front view -> s6(2,2))
        // UR (21) -> BR (Middle-Left of B Back view -> Middle-Right of B Front view -> s6(1,2))
        // URF (22) -> UBR (Top-Left of B Back view -> Top-Right of B Front view -> s6(0,2))
        
        // Wait.
        // R move: UBR -> DRB.
        // UR -> BR.
        // URF -> UBR.
        
        // s6 (Back) is mirrored.
        // col 2 is Left (Back view).
        // UBR is Top-Left (Back view). So s6(0,2).
        // BR is Middle-Left (Back view). So s6(1,2).
        // DRB is Bottom-Left (Back view). So s6(2,2).
        
        // So:
        // s6(0,2) should get URF (22).
        // s6(1,2) should get UR (21).
        // s6(2,2) should get UBR (20).
        
        // So s6 col 2 should be [22, 21, 20].
        // This is Reversed U col [20, 21, 22].
        
        val bCol = cube.getCol("s6", 2).toList()
        println("B col: $bCol")
        
        if (bCol[0] == 22 && bCol[1] == 21 && bCol[2] == 20) {
            println("R move B face logic is CORRECT (Reversed)")
        } else if (bCol[0] == 20 && bCol[1] == 21 && bCol[2] == 22) {
            println("R move B face logic is WRONG (Straight)")
        } else {
            println("R move B face logic is WRONG (Other)")
        }
    }
    @Test
    fun testAllMovesValidity() {
        val moves = listOf("U", "D", "R", "L", "F", "B", "U'", "D'", "R'", "L'", "F'", "B'")
        for (move in moves) {
            val cube = createSolvedCube()
            CubeConverter.applyMoveString(cube, move)
            val state = CubeConverter.fromCube(cube)
            
            println("Testing move: $move")
            println("CO sum: ${state.cornerOrientation.sum()}")
            println("EO sum: ${state.edgeOrientation.sum()}")
            
            if (!state.isValid()) {
                println("Move $move produced INVALID state!")
            } else {
                println("Move $move produced VALID state.")
            }
        }
    }
    @Test
    fun testSequenceValidity() {
        val cube = createSolvedCube()
        val moves = listOf("R", "U", "R'", "U'")
        
        for (move in moves) {
            println("Applying move: $move")
            CubeConverter.applyMoveString(cube, move)
            val state = CubeConverter.fromCube(cube)
            
            println("State after $move:")
            println("CO sum: ${state.cornerOrientation.sum()}")
            println("EO sum: ${state.edgeOrientation.sum()}")
            println("CP: ${state.cornerPermutation.contentToString()}")
            println("EP: ${state.edgePermutation.contentToString()}")
            
            if (!state.isValid()) {
                println("Move $move produced INVALID state!")
                throw IllegalStateException("Invalid state after $move")
            } else {
                println("Move $move produced VALID state.")
            }
        }
    }
    @Test
    fun testCompareCubeAndCubeState() {
        val cube = createSolvedCube()
        // Apply R U R' U'
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")
        
        // Check Cube state
        // UR: s2(1,2), s5(0,1). Should be 8 (F, R).
        val ur1 = cube.getCell("s2", 1, 2)
        val ur2 = cube.getCell("s5", 0, 1)
        println("UR: $ur1, $ur2")
        // F is 1, R is 5. So should be 1, 5.
        
        // UF: s2(2,1), s1(0,1). Should be 1 (U, F).
        val uf1 = cube.getCell("s2", 2, 1)
        val uf2 = cube.getCell("s1", 0, 1)
        println("UF: $uf1, $uf2")
        // U is 2, F is 1. So should be 2, 1.
        
        // UL: s2(1,0), s4(0,1). Should be 2 (U, L).
        val ul1 = cube.getCell("s2", 1, 0)
        val ul2 = cube.getCell("s4", 0, 1)
        println("UL: $ul1, $ul2")
        // U is 2, L is 4. So should be 2, 4.
        
        // UB: s2(0,1), s6(0,1). Should be 0 (U, R).
        val ub1 = cube.getCell("s2", 0, 1)
        val ub2 = cube.getCell("s6", 0, 1)
        println("UB: $ub1, $ub2")
        // U is 2, R is 5. So should be 2, 5.
        
        assertEquals(1, ur1)
        assertEquals(5, ur2)
        
        assertEquals(2, uf1)
        assertEquals(1, uf2)
        
        assertEquals(2, ul1)
        assertEquals(4, ul2)
        
        assertEquals(2, ub1)
        assertEquals(5, ub2)
    }

    @Test
    fun testVerifySimpleSolution() {
        val cube = createSolvedCube()
        // Scramble: R U R' U'
        CubeConverter.applyMoveString(cube, "R")
        CubeConverter.applyMoveString(cube, "U")
        CubeConverter.applyMoveString(cube, "R'")
        CubeConverter.applyMoveString(cube, "U'")

        // Solution: U R U' R D U' B2 D' U
        val solution = listOf("U", "R", "U'", "R", "D", "U'", "B2", "D'", "U")
        for (move in solution) {
            CubeConverter.applyMoveString(cube, move)
        }

        if (!cube.isSolved()) {
            println("Cube not solved!")
            println("s1 (F): ${cube.s1}")
            println("s2 (U): ${cube.s2}")
            println("s3 (D): ${cube.s3}")
            println("s4 (L): ${cube.s4}")
            println("s5 (R): ${cube.s5}")
            println("s6 (B): ${cube.s6}")
        }
        assertTrue(cube.isSolved())
    }

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
}
