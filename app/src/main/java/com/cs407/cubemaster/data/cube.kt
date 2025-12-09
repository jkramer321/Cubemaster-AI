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



        // For s6 (back), we need to SWAP the column index due to mirroring
        // s6 is stored mirrored: when viewing from FRONT, right side is col 0 from BEHIND
        // R move (col 2 from front) affects B face col 0 (from behind)
        // L move (col 0 from front) affects B face col 2 (from behind)
        val s6ColIndex = 2 - colIndex  // SWAP the index
        
        val temp1 = getCol("s1", colIndex).toList()
        val temp2 = getCol("s2", colIndex).toList()
        val temp3 = getCol("s3", colIndex).toList()
        val temp6 = getCol("s6", s6ColIndex).toList().reversed() // Read from s6 (swapped) and reverse

        if (rotateUp) {
            // R move: U -> B -> D -> F -> U
            setCol("s1", colIndex, temp3) // F gets D
            setCol("s2", colIndex, temp1) // U gets F
            setCol("s6", s6ColIndex, temp2.reversed()) // B gets U (reversed)
            setCol("s3", colIndex, temp6) // D gets B (already reversed from read)
        } else {
            // R' move: U -> F -> D -> B -> U
            setCol("s1", colIndex, temp2) // F gets U
            setCol("s2", colIndex, temp6) // U gets B (already reversed from read)
            setCol("s6", s6ColIndex, temp3.reversed()) // B gets D (reversed)
            setCol("s3", colIndex, temp1) // D gets F
        }

        // Rotate perpendicular faces
        when (colIndex) {
            0 -> if (rotateUp) rotateFaceCounterClockwise("s4") else rotateFaceClockwise("s4")
            2 -> if (rotateUp) rotateFaceClockwise("s5") else rotateFaceCounterClockwise("s5")
        }
    }

    fun rotateRow(rowIndex: Int, rotateRight: Boolean) {
        // Save current row values (must make copies!)
        val temp1 = getRow("s1", rowIndex).toList()  // Front
        val temp2 = getRow("s2", rowIndex).toList()  // Up  
        val temp3 = getRow("s3", rowIndex).toList()  // Down
        val temp4 = getRow("s4", rowIndex).toList()  // Left
        val temp5 = getRow("s5", rowIndex).toList()  // Right
        val temp6 = getRow("s6", rowIndex).toList()  // Back

        if (rotateRight) {
            // D move (row 2, clockwise from top): F -> R -> B -> L -> F
            // U' move (row 0, clockwise from top): F -> R -> B -> L -> F
            setRow("s5", rowIndex, temp1)  // Right <- Front
            setRow("s6", rowIndex, temp5)  // Back <- Right (straight, rows aren't mirrored)
            setRow("s4", rowIndex, temp6)  // Left <- Back (straight)
            setRow("s1", rowIndex, temp4)  // Front <- Left
        } else {
            // D' move (row 2, counter-clockwise): F -> L -> B -> R -> F
            // U move (row 0, counter-clockwise): F -> L -> B -> R -> F
            setRow("s4", rowIndex, temp1)  // Left <- Front
            setRow("s6", rowIndex, temp4)  // Back <- Left (straight)
            setRow("s5", rowIndex, temp6)  // Right <- Back (straight)
            setRow("s1", rowIndex, temp5)  // Front <- Right
        }

        // Rotate the perpendicular face
        // For U (row 0): rotateRight=false → U face rotates clockwise from above
        // For D (row 2): rotateRight=true → D face rotates clockwise from below
        when (rowIndex) {
            0 -> if (rotateRight) rotateFaceCounterClockwise("s2") else rotateFaceClockwise("s2")
            2 -> if (rotateRight) rotateFaceClockwise("s3") else rotateFaceCounterClockwise("s3")
        }
    }

    fun rotateDepth(depthIndex: Int, rotateClockwise: Boolean) {
        // Rotate a depth layer (for F/B moves)
        // depthIndex: 0 = Front (s1), 2 = Back (s6)
        
        // Save current values
        val temp2 = getRow("s2", 2).toList()  // U bottom row
        val temp3 = getRow("s3", 0).toList()  // D top row
        val temp4 = getCol("s4", 2).toList()  // L RIGHT column (col 2) - F affects this column
        val temp5 = getCol("s5", 0).toList()  // R LEFT column (col 0) - F affects this column

        if (depthIndex == 0) {
            // F move: U bottom -> R left (from R's view) -> D top (reversed) -> L right (from L's view) -> U bottom
            if (rotateClockwise) {
                // F move (clockwise)
                setCol("s5", 0, temp2)  // R left <- U bottom
                setRow("s3", 0, temp5.reversed())  // D top <- R left (reversed)
                setCol("s4", 2, temp3)  // L right <- D top
                setRow("s2", 2, temp4.reversed())  // U bottom <- L right (reversed)
                rotateFaceClockwise("s1")
            } else {
                // F' move (counter-clockwise)
                // Reverse cycle: U bottom -> L right (reversed) -> D top -> R left (reversed) -> U bottom
                setCol("s4", 2, temp2.reversed())  // L right <- U bottom (reversed)
                setRow("s3", 0, temp4)  // D top <- L right
                setCol("s5", 0, temp3.reversed())  // R left <- D top (reversed)
                setRow("s2", 2, temp5)  // U bottom <- R left
                rotateFaceCounterClockwise("s1")
            }
        } else {
            // B move: U top -> R right -> D bottom -> L left -> U top
            // (cycling in opposite direction from F because viewing from back)
            val tempU = getRow("s2", 0).toList()  // U top row
            val tempD = getRow("s3", 2).toList()  // D bottom row
            val tempL = getCol("s4", 0).toList()  // L left column
            val tempR = getCol("s5", 2).toList()  // R right column
            
            if (rotateClockwise) {
                // B move (clockwise when viewing from back)
                // U top -> L left -> D bottom -> R right -> U top
                setCol("s4", 0, tempU.reversed())  // L left <- U top (reversed)
                setRow("s3", 2, tempL)  // D bottom <- L left
                setCol("s5", 2, tempD.reversed())  // R right <- D bottom (reversed)
                setRow("s2", 0, tempR)  // U top <- R right
                rotateFaceClockwise("s6")
            } else {
                // B' move (counter-clockwise)
                // Reverse cycle: U top -> R right (rev) -> D bottom -> L left (rev) -> U top
                setCol("s5", 2, tempU.reversed())  // R right <- U top (reversed)
                setRow("s3", 2, tempR)  // D bottom <- R right
                setCol("s4", 0, tempD.reversed())  // L left <- D bottom (reversed)
                setRow("s2", 0, tempL)  // U top <- L left
                rotateFaceCounterClockwise("s6")
            }
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