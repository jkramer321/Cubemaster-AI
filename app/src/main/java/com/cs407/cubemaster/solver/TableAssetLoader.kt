package com.cs407.cubemaster.solver

import java.io.IOException
import java.io.InputStream
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

object TableAssetLoader {
    // Public constants for test/export usages (prefer compressed names)
    const val MOVE_ASSET = "move_tables.bin.gz"
    const val PRUNE_ASSET = "pruning_tables.bin.gz"
    private data class Candidate(val name: String, val compressed: Boolean)

    private val moveCandidates = listOf(
        Candidate("move_tables.bin.gz", true),
        Candidate("move_tables.bin", false)
    )
    private val pruneCandidates = listOf(
        Candidate("pruning_tables.bin.gz", true),
        Candidate("pruning_tables.bin", false)
    )

    fun loadAll(openAsset: (String) -> InputStream?): Boolean {
        val moveLoaded = loadWithFallback("move", openAsset, moveCandidates) { stream, compressed ->
            MoveTables.importBinary(stream, compressed = compressed)
        }
        val pruneLoaded = loadWithFallback("prune", openAsset, pruneCandidates) { stream, compressed ->
            PruningTables.importBinary(stream, compressed = compressed)
        }
        return moveLoaded && pruneLoaded
    }

    private fun loadWithFallback(
        label: String,
        openAsset: (String) -> InputStream?,
        candidates: List<Candidate>,
        import: (CheckedInputStream, Boolean) -> Boolean
    ): Boolean {
        for (candidate in candidates) {
            val name = candidate.name
            try {
                val estimatedSize = try {
                    openAsset(name)?.use { it.available() } ?: -1
                } catch (_: IOException) {
                    -1
                }
                SolverLog.d(
                    "TableAssetLoader",
                    "Loading $label candidate $name (compressed=${candidate.compressed}, available=$estimatedSize)"
                )
                val raw = openAsset(name)
                if (raw == null) {
                    SolverLog.e("TableAssetLoader", "Asset not found: $name")
                    continue
                }
                CheckedInputStream(raw, CRC32()).use { checked ->
                    val ok = import(checked, candidate.compressed)
                    SolverLog.d(
                        "TableAssetLoader",
                        "Loaded $name success=$ok crc=${checked.checksum.value}"
                    )
                    if (ok) return true
                    SolverLog.e("TableAssetLoader", "Import failed for $name")
                }
            } catch (e: Exception) {
                SolverLog.e("TableAssetLoader", "Failed to load $name: ${e.message}")
            }
        }
        SolverLog.e("TableAssetLoader", "All candidates failed for $label tables")
        return false
    }
}

