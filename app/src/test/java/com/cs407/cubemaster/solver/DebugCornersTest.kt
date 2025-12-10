package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube
import org.junit.Test

class DebugCornersTest {
    
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
    
    @Test
    fun printSolvedCubeFacelets() {
        val cube = createSolvedCube()
        
        // Print facelet array
        val facelets = IntArray(54)
        
        // U (s2)
        for (i in 0..8) {
            facelets[i] = cube.getCell("s2", i / 3, i % 3)
        }
        
        // R (s5)
        for (i in 0..8) {
            facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)
        }
        
        // F (s1)
        for (i in 0..8) {
            facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)
        }
        
        // D (s3)
        for (i in 0..8) {
            facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)
        }
        
        // L (s4)
        for (i in 0..8) {
            facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)
        }
        
        // B (s6)
        for (i in 0..8) {
            facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)
        }
        
        println("Facelet array for solved cube:")
        for (i in facelets.indices) {
            if (i % 9 == 0) {
                val face = when (i / 9) {
                    0 -> "U"
                    1 -> "R"
                    2 -> "F"
                    3 -> "D"
                    4 -> "L"
                    5 -> "B"
                    else -> "?"
                }
                println("\n$face face (indices ${i}-${i+8}):")
            }
            print("${facelets[i]} ")
        }
        println()
        
        // Now print what each corner position should read
        println("\n\nCorner facelets for solved cube:")
        val corners = arrayOf(
            "URF" to intArrayOf(8, 9, 20),
            "UFL" to intArrayOf(6, 18, 38),
            "ULB" to intArrayOf(0, 36, 45),  // Current
            "UBR" to intArrayOf(2, 47, 11),  // Current
            "DFR" to intArrayOf(29, 26, 15),
            "DLF" to intArrayOf(27, 44, 24),
            "DBL" to intArrayOf(33, 53, 42),
            "DRB" to intArrayOf(35, 17, 51)
        )
        
        for ((name, indices) in corners) {
            val colors = indices.map { facelets[it] }
            println("$name: indices ${indices.contentToString()} -> colors $colors")
        }
    }
    
    @Test
    fun printAfterRU() {
        val cube = createSolvedCube()
        
        println("=== After R ===")
        CubeConverter.applyMoveString(cube, "R")
        printAllFaces(cube)
        printCornerFacelets(cube)
        
        println("\n=== After R U ===")
        CubeConverter.applyMoveString(cube, "U")
        printAllFaces(cube)
        printCornerFacelets(cube)
    }
    
    private fun printAllFaces(cube: Cube) {
        println("F face (s1):")
        for (row in 0..2) {
            for (col in 0..2) {
                print("${cube.getCell("s1", row, col)} ")
            }
            println()
        }
        println("U face (s2):")
        for (row in 0..2) {
            for (col in 0..2) {
                print("${cube.getCell("s2", row, col)} ")
            }
            println()
        }
        println("L face (s4):")
        for (row in 0..2) {
            for (col in 0..2) {
                print("${cube.getCell("s4", row, col)} ")
            }
            println()
        }
        println("B face (s6):")
        for (row in 0..2) {
            for (col in 0..2) {
                print("${cube.getCell("s6", row, col)} ")
            }
            println()
        }
    }
    
    private fun printCornerFacelets(cube: Cube) {
        val facelets = IntArray(54)
        
        // U (s2)
        for (i in 0..8) {
            facelets[i] = cube.getCell("s2", i / 3, i % 3)
        }
        
        // R (s5)
        for (i in 0..8) {
            facelets[9 + i] = cube.getCell("s5", i / 3, i % 3)
        }
        
        // F (s1)
        for (i in 0..8) {
            facelets[18 + i] = cube.getCell("s1", i / 3, i % 3)
        }
        
        // D (s3)
        for (i in 0..8) {
            facelets[27 + i] = cube.getCell("s3", i / 3, i % 3)
        }
        
        // L (s4)
        for (i in 0..8) {
            facelets[36 + i] = cube.getCell("s4", i / 3, i % 3)
        }
        
        // B (s6)
        for (i in 0..8) {
            facelets[45 + i] = cube.getCell("s6", i / 3, i % 3)
        }
        
        val corners = arrayOf(
            "URF" to intArrayOf(8, 9, 20),
            "UFL" to intArrayOf(6, 18, 38),
            "ULB" to intArrayOf(0, 36, 45),
            "UBR" to intArrayOf(2, 47, 11),
            "DFR" to intArrayOf(29, 26, 15),
            "DLF" to intArrayOf(27, 44, 24),
            "DBL" to intArrayOf(33, 53, 42),
            "DRB" to intArrayOf(35, 17, 51)
        )
        
        for ((name, indices) in corners) {
            val colors = indices.map { facelets[it] }
            println("$name: $colors")
        }
    }
}
