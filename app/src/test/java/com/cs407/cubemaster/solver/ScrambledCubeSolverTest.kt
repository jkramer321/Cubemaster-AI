package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ScrambledCubeSolverTest {

    @Before
    fun setup() {
        // Force re-initialization of tables to ensure latest logic is used
        MoveTables.forceInitialize()
        PruningTables.forceInitialize()
    }

    @Test(timeout = 30000)
    fun testSimpleScramble() = runBlocking {
        println("Starting testSimpleScramble...")
        SolverLog.strategy = ConsoleLogStrategy
        
        println("Initializing solver...")
        val solver = KociembaSolver()
        solver.initialize()
        println("Solver initialized.")

        val cube = createSolvedCube()
        // Scramble: R U R' U'
        val scramble = listOf("R", "U", "R'", "U'")
        for (move in scramble) {
            CubeConverter.applyMoveString(cube, move)
        }
        
        val validation = CubeValidator.validate(cube)
        println("Cube Validity: ${validation.isValid}")
        if (!validation.isValid) {
            println("Error: ${validation.errorMessage}")
            fail("Cube is invalid before solving: ${validation.errorMessage}")
        }

        val state = CubeConverter.fromCube(cube)
        println("Scrambled State Details:")
        println("Corner Perm: ${state.cornerPermutation.contentToString()}")
        println("Corner Ori: ${state.cornerOrientation.contentToString()}")
        println("Edge Perm: ${state.edgePermutation.contentToString()}")
        println("Edge Ori: ${state.edgeOrientation.contentToString()}")

        println("Solving...")
        val solution = solver.solve(cube)
        println("Solve returned: $solution")
        assertNotNull("Solution should not be null", solution)
        
        applySolution(cube, solution!!)
        assertTrue("Cube should be solved after applying solution", cube.isSolved())
    }

    @Test(timeout = 30000)
    fun testMediumScramble() = runBlocking {
        println("Starting testMediumScramble...")
        SolverLog.strategy = ConsoleLogStrategy
        val solver = KociembaSolver()
        solver.initialize()

        val cube = createSolvedCube()
        // Scramble: F R U R' U' F'
        val scramble = listOf("F", "R", "U", "R'", "U'", "F'")
        for (move in scramble) {
            CubeConverter.applyMoveString(cube, move)
        }
        
        val validation = CubeValidator.validate(cube)
        if (!validation.isValid) fail("Cube is invalid: ${validation.errorMessage}")

        val solution = solver.solve(cube)
        assertNotNull("Solution should not be null", solution)
        println("Medium Scramble Solution: $solution")

        applySolution(cube, solution!!)
        assertTrue("Cube should be solved after applying solution", cube.isSolved())
    }

    @Test(timeout = 30000)
    fun testHardScramble() = runBlocking {
        println("Starting testHardScramble...")
        SolverLog.strategy = ConsoleLogStrategy
        val solver = KociembaSolver()
        solver.initialize()

        val cube = createSolvedCube()
        // Scramble: D2 F2 U' B2 F2 D' L2 U L2 B2 F' U' L' R' D L2 B' D2 U
        val scramble = listOf("D2", "F2", "U'", "B2", "F2", "D'", "L2", "U", "L2", "B2", "F'", "U'", "L'", "R'", "D", "L2", "B'", "D2", "U")
        for (move in scramble) {
            CubeConverter.applyMoveString(cube, move)
        }
        
        val validation = CubeValidator.validate(cube)
        if (!validation.isValid) fail("Cube is invalid: ${validation.errorMessage}")

        val solution = solver.solve(cube)
        assertNotNull("Solution should not be null", solution)
        println("Hard Scramble Solution: $solution")

        applySolution(cube, solution!!)
        assertTrue("Cube should be solved after applying solution", cube.isSolved())
    }

    private fun applySolution(cube: Cube, solution: List<String>) {
        for (move in solution) {
            CubeConverter.applyMoveString(cube, move)
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
}
