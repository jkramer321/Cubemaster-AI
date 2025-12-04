package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class KociembaSolverTest {

    @Test
    fun testSolvedCube() = runBlocking {
        val solver = KociembaSolver()
        solver.initialize()

        val solvedCube = createSolvedCube()
        val solution = solver.solve(solvedCube)

        assertNotNull(solution)
        assertTrue(solution!!.isEmpty())
    }

    @Test
    fun testCubeStateMovesWork() {
        val state = CubeState.solved()
        assertTrue(state.isSolved())

        // Apply R move
        val afterR = state.applyMove(CubeMove.R)
        assertFalse(afterR.isSolved())

        // Apply R' to undo
        val afterRPrime = afterR.applyMove(CubeMove.RP)
        assertTrue(afterRPrime.isSolved())
    }

    @Test
    fun testCoordinates() {
        val state = CubeState.solved()

        // Solved state should have coordinate 0
        assertEquals(0, CoordinateSystem.getTwistCoordinate(state))
        assertEquals(0, CoordinateSystem.getFlipCoordinate(state))
        assertEquals(0, CoordinateSystem.getSliceCoordinate(state))
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

