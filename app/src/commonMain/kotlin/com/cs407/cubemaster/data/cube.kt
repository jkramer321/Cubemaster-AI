package com.cs407.cubemaster.data

data class Cube(val s1: MutableList<MutableList<Int>>,
                val s2: MutableList<MutableList<Int>>,
                val s3: MutableList<MutableList<Int>>,
                val s4: MutableList<MutableList<Int>>,
                val s5: MutableList<MutableList<Int>>,
                val s6: MutableList<MutableList<Int>>,) {
    private var sides = mutableMapOf<String, MutableList<MutableList<Int>>>(
        "s1" to s1,
        "s2" to s2,
        "s3" to s3,
        "s4" to s4,
        "s5" to s5,
        "s6" to s6
    )

    fun isSolved(): Boolean {
        for (side in sides) {
            val mainColor = getCell(side.key, 0, 0)
            for (row in 0..2) {
                for (col in 0..2) {
                    if (getCell(side.key, row, col) != mainColor) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /**
     * 1: Front
     * 2: Top
     * 3: Bottom
     * 4: Left side
     * 5: Right side
     * 6: Back
     *         +-----+
     *         |  2  |
     *         +-----+
     *
     * +-----+ +-----+ +-----+ +-----+
     * |  4  | |  1  | |  5  | |  6  |
     * +-----+ +-----+ +-----+ +-----+
     *
     *         +-----+
     *         |  3  |
     *         +-----+
     *
     * NOTE: s6 (back) uses same coordinate system when viewed from behind.
     *       For COLUMNS: left edge of cube = s6 col 2, right edge = s6 col 0
     *       For ROWS: rows remain the same (no reversal needed)
     */

    fun getCell(side: String, row: Int, col: Int): Int {
        return sides[side]?.get(row)?.get(col) ?: throw IllegalArgumentException("Invalid side, row, or col")
    }

    private fun setCell(side: String, row: Int, col: Int, value: Int) {
        sides[side]?.get(row)?.set(col, value) ?: throw IllegalArgumentException("Invalid side, row, or col")
    }

    fun getRow(side: String, row: Int): List<Int> {
        if (row < 0 || row > 2) {
            throw IllegalArgumentException("Invalid row index")
        }
        return sides[side]?.get(row) ?: throw IllegalArgumentException("Invalid side or row")
    }

    private fun setRow(side: String, row: Int, newRow: List<Int>) {
        if (row < 0 || row > 2) {
            throw IllegalArgumentException("Invalid row index")
        }
        if (newRow.size != 3) throw IllegalArgumentException("Row must have size 3")
        val mutableRow = sides[side]?.get(row) ?: throw IllegalArgumentException("Invalid side or row")
        mutableRow.clear()
        mutableRow.addAll(newRow)
    }

    fun getCol(side: String, col: Int): List<Int> {
        if (col < 0 || col > 2) {
            throw IllegalArgumentException("Invalid column index")
        }
        if (sides[side] == null) {
            throw IllegalArgumentException("Invalid side")
        }
        val values = mutableListOf<Int>()
        for (row in 0..2) {
            val cell = getCell(side, row, col)
            values.add(cell)
        }
        return values
    }

    private fun setCol(side: String, colIndex: Int, newCol: List<Int>) {
        if (newCol.size != 3) throw IllegalArgumentException("Column must have size 3")
        if (colIndex < 0 || colIndex > 2) throw IllegalArgumentException("Invalid column index")
        for (i in 0..2) {
            setCell(side, i, colIndex, newCol[i])
        }
    }

    private fun rotateFaceClockwise(side: String) {
        val original = sides[side]!!
        val rotated = MutableList(3) { MutableList(3) { 0 } }
        for (i in 0..2) {
            for (j in 0..2) {
                rotated[j][2 - i] = original[i][j]
            }
        }
        // Update in place instead of replacing the reference
        for (i in 0..2) {
            for (j in 0..2) {
                original[i][j] = rotated[i][j]
            }
        }
    }

    private fun rotateFaceCounterClockwise(side: String) {
        val original = sides[side]!!
        val rotated = MutableList(3) { MutableList(3) { 0 } }
        for (i in 0..2) {
            for (j in 0..2) {
                rotated[2 - j][i] = original[i][j]
            }
        }
        // Update in place instead of replacing the reference
        for (i in 0..2) {
            for (j in 0..2) {
                original[i][j] = rotated[i][j]
            }
        }
    }

    fun rotateCol(colIndex: Int, rotateUp: Boolean) {
        if (colIndex < 0 || colIndex > 2) {
            throw IllegalArgumentException("Invalid column index")
        }

        // For s6, use opposite column (mirror effect)
        val s6ColIndex = 2 - colIndex

        // Simplified: just read/write with column mirroring for s6
        val temp1 = getCol("s1", colIndex)
        val temp2 = getCol("s2", colIndex)
        val temp6 = getCol("s6", s6ColIndex).reversed() // s6 stored reversed
        val temp3 = getCol("s3", colIndex)

        if (rotateUp) {
            // s1 ← s3, s2 ← s1, s6 ← s2, s3 ← s6
            setCol("s1", colIndex, temp3)
            setCol("s2", colIndex, temp1)
            setCol("s6", s6ColIndex, temp2.reversed()) // Store reversed in s6
            setCol("s3", colIndex, temp6)
        } else {
            // s1 ← s2, s2 ← s6, s6 ← s3, s3 ← s1
            setCol("s1", colIndex, temp2)
            setCol("s2", colIndex, temp6)
            setCol("s6", s6ColIndex, temp3.reversed()) // Store reversed in s6
            setCol("s3", colIndex, temp1)
        }

        // Rotate perpendicular faces
        when (colIndex) {
            0 -> if (rotateUp) rotateFaceCounterClockwise("s4") else rotateFaceClockwise("s4")
            2 -> if (rotateUp) rotateFaceClockwise("s5") else rotateFaceCounterClockwise("s5")
        }
    }

    fun rotateRow(rowIndex: Int, rotateRight: Boolean) {
        // Simplified: just read/write with reversal for s6
        // IMPORTANT: getRow returns a reference to the actual list, so we must make copies!
        val temp1 = getRow("s1", rowIndex).toList()
        val temp5 = getRow("s5", rowIndex).toList()
        val temp6 = getRow("s6", rowIndex).reversed() // s6 stored reversed (reversed() already makes a copy)
        val temp4 = getRow("s4", rowIndex).toList()

        if (rotateRight) {
            // s1 ← s4, s5 ← s1, s6 ← s5, s4 ← s6
            setRow("s1", rowIndex, temp4)
            setRow("s5", rowIndex, temp1)
            setRow("s6", rowIndex, temp5.reversed()) // Store reversed in s6
            setRow("s4", rowIndex, temp6)
        } else {
            // s1 ← s5, s5 ← s6, s6 ← s4, s4 ← s1
            setRow("s1", rowIndex, temp5)
            setRow("s5", rowIndex, temp6)
            setRow("s6", rowIndex, temp4.reversed()) // Store reversed in s6
            setRow("s4", rowIndex, temp1)
        }

        // Rotate perpendicular faces
        when (rowIndex) {
            0 -> if (rotateRight) rotateFaceClockwise("s2") else rotateFaceCounterClockwise("s2")
            2 -> if (rotateRight) rotateFaceClockwise("s3") else rotateFaceCounterClockwise("s3")
        }
    }

    fun freeze(): Cube {
        return Cube(
            s1 = s1.map { it.toMutableList() }.toMutableList(),
            s2 = s2.map { it.toMutableList() }.toMutableList(),
            s3 = s3.map { it.toMutableList() }.toMutableList(),
            s4 = s4.map { it.toMutableList() }.toMutableList(),
            s5 = s5.map { it.toMutableList() }.toMutableList(),
            s6 = s6.map { it.toMutableList() }.toMutableList()
        )
    }
}