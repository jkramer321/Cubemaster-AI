package com.cs407.cubemaster.solver

import org.junit.Test
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

/**
 * JUnit test that generates pre-computed Kociemba tables and saves them
 * as a compressed binary file in the assets folder.
 * 
 * Run this test on your development machine (not on Android) to generate
 * the tables. The test will take 5-15 minutes to complete depending on
 * CPU speed. The pruning tables are generated up to depth 12 for Phase 1
 * and depth 18 for Phase 2.
 * 
 * After running, the file `kociemba_tables.bin.gz` will be created in
 * `app/src/main/assets/` and can be loaded at runtime on Android.
 */
class TableGenerator {

    @Test
    fun generateAndSaveTables() {
        println("=== Kociemba Table Generator ===")
        println()
        
        // Step 1: Generate move tables
        println("Step 1: Generating move tables...")
        val moveStartTime = System.currentTimeMillis()
        MoveTables.initialize()
        val moveElapsed = System.currentTimeMillis() - moveStartTime
        println("  Move tables generated in ${moveElapsed}ms")
        
        // Step 2: Generate pruning tables
        println("Step 2: Generating pruning tables...")
        val pruneStartTime = System.currentTimeMillis()
        PruningTables.initialize()
        val pruneElapsed = System.currentTimeMillis() - pruneStartTime
        println("  Pruning tables generated in ${pruneElapsed}ms")
        
        // Step 3: Save to compressed binary file
        println("Step 3: Saving tables to compressed binary file...")
        
        val projectDir = File(System.getProperty("user.dir"))
        val outputDir = if (projectDir.name == "app") {
            File(projectDir, "src/main/assets")
        } else {
            File(projectDir, "app/src/main/assets")
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val outputFile = File(outputDir, "kociemba_tables.bin.gz")
        
        val saveStartTime = System.currentTimeMillis()
        saveTables(outputFile)
        val saveElapsed = System.currentTimeMillis() - saveStartTime
        
        val fileSizeKB = outputFile.length() / 1024
        println("  Tables saved in ${saveElapsed}ms")
        println("  Output file: ${outputFile.absolutePath}")
        println("  File size: ${fileSizeKB} KB")
        
        println()
        println("=== Generation Complete ===")
        println("Total time: ${moveElapsed + pruneElapsed + saveElapsed}ms")
    }
    
    private fun saveTables(outputFile: File) {
        DataOutputStream(GZIPOutputStream(FileOutputStream(outputFile))).use { out ->
            // Write magic number and version
            out.writeInt(0x4B4F4349) // "KOCI" in hex
            out.writeInt(1) // Version 1
            
            // === Move Tables ===
            
            // Twist move table [2187][18]
            val twistMove = MoveTables.getTwistMoveTable()
            out.writeInt(twistMove.size)
            out.writeInt(twistMove[0].size)
            for (row in twistMove) {
                for (value in row) {
                    out.writeShort(value)
                }
            }
            
            // Flip move table [2048][18]
            val flipMove = MoveTables.getFlipMoveTable()
            out.writeInt(flipMove.size)
            out.writeInt(flipMove[0].size)
            for (row in flipMove) {
                for (value in row) {
                    out.writeShort(value)
                }
            }
            
            // Slice move table [495][18]
            val sliceMove = MoveTables.getSliceMoveTable()
            out.writeInt(sliceMove.size)
            out.writeInt(sliceMove[0].size)
            for (row in sliceMove) {
                for (value in row) {
                    out.writeShort(value)
                }
            }
            
            // Corner perm move table [40320][10]
            // Note: values can be 0-40319, exceeds short range, must use int
            val cornerPermMove = MoveTables.getCornerPermMoveTable()
            out.writeInt(cornerPermMove.size)
            out.writeInt(cornerPermMove[0].size)
            for (row in cornerPermMove) {
                for (value in row) {
                    out.writeInt(value)
                }
            }
            
            // UD edge perm move table [40320][10]
            // Note: values can be 0-40319, exceeds short range, must use int
            val udEdgePermMove = MoveTables.getUDEdgePermMoveTable()
            out.writeInt(udEdgePermMove.size)
            out.writeInt(udEdgePermMove[0].size)
            for (row in udEdgePermMove) {
                for (value in row) {
                    out.writeInt(value)
                }
            }
            
            // Slice sorted move table [24][10]
            val sliceSortedMove = MoveTables.getSliceSortedMoveTable()
            out.writeInt(sliceSortedMove.size)
            out.writeInt(sliceSortedMove[0].size)
            for (row in sliceSortedMove) {
                for (value in row) {
                    out.writeShort(value)
                }
            }
            
            // === Pruning Tables ===
            
            // Phase 1 twist-flip pruning [2187 * 2048]
            val phase1TwistFlip = PruningTables.getPhase1TwistFlipPruning()
            out.writeInt(phase1TwistFlip.size)
            out.write(phase1TwistFlip)
            
            // Phase 1 slice-twist pruning [495 * 2187]
            val phase1SliceTwist = PruningTables.getPhase1SliceTwistPruning()
            out.writeInt(phase1SliceTwist.size)
            out.write(phase1SliceTwist)
            
            // Phase 2 corner-edge pruning [40320 * 8]
            val phase2CornerEdge = PruningTables.getPhase2CornerEdgePruning()
            out.writeInt(phase2CornerEdge.size)
            out.write(phase2CornerEdge)
            
            // Phase 2 corner-slice pruning [40320 * 24]
            val phase2CornerSlice = PruningTables.getPhase2CornerSlicePruning()
            out.writeInt(phase2CornerSlice.size)
            out.write(phase2CornerSlice)
        }
    }
}
