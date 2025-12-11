package com.cs407.cubemaster.solver

import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import com.cs407.cubemaster.solver.TableAssetLoader

/**
 * Generates move and pruning tables using the current solver implementation.
 * Outputs raw .bin files (no extra compression) under app/src/main/assets/.
 */
class GenerateTablesTest {

    @Test
    fun generateBinaryTables() {
        // Build fresh tables from the in-app algorithms (ignore assets)
        MoveTables.forceInitialize()
        PruningTables.forceInitialize()

        val assetsDir = File("app/src/main/assets")
        assetsDir.mkdirs()

        FileOutputStream(File(assetsDir, TableAssetLoader.MOVE_ASSET)).use { out ->
            MoveTables.exportBinary(out, compress = true)
        }
        FileOutputStream(File(assetsDir, TableAssetLoader.PRUNE_ASSET)).use { out ->
            PruningTables.exportBinary(out, compress = true)
        }
    }
}

