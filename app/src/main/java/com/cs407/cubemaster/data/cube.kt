package com.cs407.cubemaster.data

// Cube Class
// Contains the sides of the full cube
// These are represented by 6, 2D arrays each of size 3x3 (Rubik grid)
// NOTE: s1 should be considered the "front" for orientation purposes
//
// NOTE 2: We assume the cube remains in the same orientation in the user's perspective.
//         We do this because otherwise rotateRow and rotateCol would not function correctly.
//         When printing out the cube, always print starting with s1 as the "front"
//
// NOTE 3: Colors are associated with a number value.
//         From the machine's perspective, this doesn't matter, but for a human, we will want to translate the color into an appropriate number.
//         TODO: add map to data class for translation of number-to-color and vice versa?
//
// NOTE 4: All faces use the same coordinate system (row 0 = top, col 0 = left) when viewed directly.
//         For s6 (back face), this means col 0 is the right edge from the front perspective.


data class Cube(val s1: MutableList<MutableList<Int>>,
                val s2: MutableList<MutableList<Int>>,
                val s3: MutableList<MutableList<Int>>,
                val s4: MutableList<MutableList<Int>>,
                val s5: MutableList<MutableList<Int>>,
                val s6: MutableList<MutableList<Int>>,) {
    var sides = mutableMapOf<String, MutableList<MutableList<Int>>>(
        "s1" to s1,
        "s2" to s2,
        "s3" to s3,
        "s4" to s4,
        "s5" to s5,
        "s6" to s6
    )

    // Checks if the cube is solved
    fun isSolved(): Boolean {
        // Check each side
        for (side in sides) {
            // Grab the first corner color, this should be our color to compare against.
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
     */

    fun getCell(side: String, row: Int, col: Int): Int {
        return sides[side]?.get(row)?.get(col) ?: throw IllegalArgumentException("Invalid side, row, or col")
    }

    fun setCell(side: String, row: Int, col: Int, value: Int) {
        sides[side]?.get(row)?.set(col, value) ?: throw IllegalArgumentException("Invalid side, row, or col")
    }

    fun getRow(side: String, row: Int): List<Int> {
        if (row < 0 || row > 2) {
            throw IllegalArgumentException("Invalid row index")
        }
        return sides[side]?.get(row) ?: throw IllegalArgumentException("Invalid side or row")
    }

    fun setRow(side: String, row: Int, newRow: List<Int>) {
        if (row < 0 || row > 2) {
            throw IllegalArgumentException("Invalid row index")
        }
        // SAFETY CHECK: this requires the newRow to be exactly size 3. Turn this off for use with larger cubes.
        if (newRow.size != 3) throw IllegalArgumentException("Row must have size 3")
        val mutableRow = sides[side]?.get(row) ?: throw IllegalArgumentException("Invalid side or row")
        mutableRow.clear()
        mutableRow.addAll(newRow)
    }

    // Returns column back, read from top-to-bottom
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

    // Sets column, writes from top-to-bottom
    fun setCol(side: String, colIndex: Int, newCol: List<Int>) {
        // SAFETY CHECK: this requires the newRow to be exactly size 3. Turn this off for use with larger cubes.
        if (newCol.size != 3) throw IllegalArgumentException("Column must have size 3")
        if (colIndex < 0 || colIndex > 2) throw IllegalArgumentException("Invalid column index")
        // Iterate over the values on side and replace with the corresponding value in newCol.
        for (i in 0..2) {
            setCell(side, i, colIndex, newCol[i])
        }
    }

    // Rotates a face 90 degrees clockwise
    private fun rotateFaceClockwise(side: String) {
        val original = sides[side]!!
        val rotated = MutableList(3) { MutableList(3) { 0 } }
        for (i in 0..2) {
            for (j in 0..2) {
                rotated[j][2 - i] = original[i][j]
            }
        }
        sides[side] = rotated
    }

    // Rotates a face 90 degrees counterclockwise
    private fun rotateFaceCounterClockwise(side: String) {
        val original = sides[side]!!
        val rotated = MutableList(3) { MutableList(3) { 0 } }
        for (i in 0..2) {
            for (j in 0..2) {
                rotated[2 - j][i] = original[i][j]
            }
        }
        sides[side] = rotated
    }

    // Updates cube to be rotated along some col
    // colIndex 0 = left edge, colIndex 2 = right edge
    // Updates cube to be rotated along some col
    // colIndex 0 = left edge, colIndex 2 = right edge
    fun rotateCol(colIndex: Int, rotateUp: Boolean) {
        if (colIndex < 0 || colIndex > 2) {
            throw IllegalArgumentException("Invalid column index")
        }

        var sideKeys = listOf("s1", "s2", "s6", "s3")

        if (!rotateUp) {
            sideKeys = sideKeys.reversed()
        }

        // For s6 (back face), we need to use the opposite column index
        // because s6 is viewed from behind
        val s6ColIndex = 2 - colIndex

        // Get the column values, handling s6 specially
        fun getColValue(side: String, col: Int): List<Int> {
            return if (side == "s6") {
                getCol(side, s6ColIndex).reversed()
            } else {
                getCol(side, col)
            }
        }

        fun setColValue(side: String, col: Int, values: List<Int>) {
            if (side == "s6") {
                setCol(side, s6ColIndex, values.reversed())
            } else {
                setCol(side, col, values)
            }
        }

        // Init incoming change (last item will replace first in this instance)
        var incomingChange = getColValue(sideKeys.last(), colIndex)

        for (key in sideKeys) {
            val nextChange = getColValue(key, colIndex)
            setColValue(key, colIndex, incomingChange)
            incomingChange = nextChange
        }

        // Rotate the perpendicular face when rotating outer columns
        when (colIndex) {
            0 -> if (rotateUp) rotateFaceCounterClockwise("s4") else rotateFaceClockwise("s4")
            2 -> if (rotateUp) rotateFaceClockwise("s5") else rotateFaceCounterClockwise("s5")
        }
    }

    // Updates cube to be rotated along some row
    fun rotateRow(rowIndex: Int, rotateRight: Boolean) {
        var sideKeys = listOf("s1", "s5", "s6", "s4")

        if (!rotateRight) {
            sideKeys = sideKeys.reversed()
        }

        // Get row values, handling s6 specially
        fun getRowValue(side: String, row: Int): List<Int> {
            return if (side == "s6") {
                getRow(side, row).reversed()
            } else {
                getRow(side, row)
            }
        }

        fun setRowValue(side: String, row: Int, values: List<Int>) {
            if (side == "s6") {
                setRow(side, row, values.reversed())
            } else {
                setRow(side, row, values)
            }
        }

        var incomingChange = getRowValue(sideKeys.last(), rowIndex)

        for (key in sideKeys) {
            val nextChange = getRowValue(key, rowIndex)
            setRowValue(key, rowIndex, incomingChange)
            incomingChange = nextChange
        }

        // Rotate the perpendicular face when rotating outer rows
        when (rowIndex) {
            0 -> if (rotateRight) rotateFaceClockwise("s2") else rotateFaceCounterClockwise("s2")
            2 -> if (rotateRight) rotateFaceClockwise("s3") else rotateFaceCounterClockwise("s3")
        }
    }
    // PLEASE USE THIS METHOD!!!
    // For displaying or using the cube in a manner on the frontend!
    // This will prevent excessive memory usage and battery drain
    fun freeze(): Cube {
        // Create a deep copy of all sides by converting to immutable lists

        // Usage:
        // val workingCube = Cube(...)
        // ... perform rotations ...
        // val frozenState = workingCube.freeze()  -- Safe immutable copy for storage

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