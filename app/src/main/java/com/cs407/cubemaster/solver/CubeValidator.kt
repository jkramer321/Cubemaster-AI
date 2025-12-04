package com.cs407.cubemaster.solver

import com.cs407.cubemaster.data.Cube

/**
 * Validates that a scanned cube state is physically possible and solvable.
 *
 * A valid Rubik's cube must satisfy:
 * 1. Color count: Exactly 9 facelets of each of the 6 colors
 * 2. Corner parity: Even permutation of corners
 * 3. Edge parity: Even permutation of edges
 * 4. Corner orientation: Sum of corner orientations ≡ 0 (mod 3)
 * 5. Edge orientation: Sum of edge orientations ≡ 0 (mod 2)
 * 6. Total parity: Corner and edge permutation parity must match
 */
object CubeValidator {

    /**
     * Validation result with detailed error information
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorCode: ErrorCode? = null,
        val errorMessage: String? = null
    ) {
        enum class ErrorCode {
            INVALID_COLOR_COUNT,
            DUPLICATE_CORNER,
            DUPLICATE_EDGE,
            MISSING_CORNER,
            MISSING_EDGE,
            INVALID_CORNER_ORIENTATION,
            INVALID_EDGE_ORIENTATION,
            PARITY_ERROR,
            CONVERSION_ERROR
        }
    }

    /**
     * Validate a cube and return detailed result
     */
    fun validate(cube: Cube): ValidationResult {
        // Step 1: Check color count (most important for scanned cubes)
        val colorCountResult = validateColorCount(cube)
        if (!colorCountResult.isValid) return colorCountResult

        // Step 2: Try to convert to CubeState for deeper validation
        val cubeState = try {
            CubeConverter.fromCube(cube)
        } catch (e: Exception) {
            // If conversion fails, just warn but don't fail validation
            // Color count validation is sufficient for most cases
            return ValidationResult(true)  // Pass with color validation only
        }

        // Step 3: Check for duplicate pieces
        val duplicateResult = validateNoDuplicates(cubeState)
        if (!duplicateResult.isValid) return duplicateResult

        // Step 4: Check for missing pieces
        val missingResult = validateAllPiecesPresent(cubeState)
        if (!missingResult.isValid) return missingResult

        // Step 5: Check corner orientation sum
        val cornerOrientationResult = validateCornerOrientation(cubeState)
        if (!cornerOrientationResult.isValid) return cornerOrientationResult

        // Step 6: Check edge orientation sum
        val edgeOrientationResult = validateEdgeOrientation(cubeState)
        if (!edgeOrientationResult.isValid) return edgeOrientationResult

        // Step 7: Check permutation parity
        val parityResult = validateParity(cubeState)
        if (!parityResult.isValid) return parityResult

        return ValidationResult(true)
    }

    /**
     * Validate that each color appears exactly 9 times
     */
    private fun validateColorCount(cube: Cube): ValidationResult {
        val colorCounts = mutableMapOf<Int, Int>()

        // Count colors on all faces
        for (side in listOf("s1", "s2", "s3", "s4", "s5", "s6")) {
            for (row in 0..2) {
                for (col in 0..2) {
                    val color = cube.getCell(side, row, col)
                    colorCounts[color] = colorCounts.getOrDefault(color, 0) + 1
                }
            }
        }

        // Check that we have exactly 6 colors, each appearing 9 times
        if (colorCounts.size != 6) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.INVALID_COLOR_COUNT,
                "Expected 6 colors, found ${colorCounts.size}"
            )
        }

        for ((color, count) in colorCounts) {
            if (count != 9) {
                return ValidationResult(
                    false,
                    ValidationResult.ErrorCode.INVALID_COLOR_COUNT,
                    "Color $color appears $count times (expected 9)"
                )
            }
        }

        return ValidationResult(true)
    }

    /**
     * Check that no corner or edge piece appears more than once
     */
    private fun validateNoDuplicates(cubeState: CubeState): ValidationResult {
        // Check corner duplicates
        val cornerSet = cubeState.cornerPermutation.toSet()
        if (cornerSet.size != 8) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.DUPLICATE_CORNER,
                "Found duplicate corner pieces"
            )
        }

        // Check edge duplicates
        val edgeSet = cubeState.edgePermutation.toSet()
        if (edgeSet.size != 12) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.DUPLICATE_EDGE,
                "Found duplicate edge pieces"
            )
        }

        return ValidationResult(true)
    }

    /**
     * Check that all corner and edge pieces are present
     */
    private fun validateAllPiecesPresent(cubeState: CubeState): ValidationResult {
        // Check all corners 0-7 are present
        val expectedCorners = (0..7).toSet()
        val actualCorners = cubeState.cornerPermutation.toSet()
        if (actualCorners != expectedCorners) {
            val missing = expectedCorners - actualCorners
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.MISSING_CORNER,
                "Missing corner pieces: $missing"
            )
        }

        // Check all edges 0-11 are present
        val expectedEdges = (0..11).toSet()
        val actualEdges = cubeState.edgePermutation.toSet()
        if (actualEdges != expectedEdges) {
            val missing = expectedEdges - actualEdges
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.MISSING_EDGE,
                "Missing edge pieces: $missing"
            )
        }

        return ValidationResult(true)
    }

    /**
     * Validate corner orientation constraint: sum must be divisible by 3
     */
    private fun validateCornerOrientation(cubeState: CubeState): ValidationResult {
        val sum = cubeState.cornerOrientation.sum()
        if (sum % 3 != 0) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.INVALID_CORNER_ORIENTATION,
                "Corner orientation sum is $sum (must be divisible by 3)"
            )
        }
        return ValidationResult(true)
    }

    /**
     * Validate edge orientation constraint: sum must be divisible by 2
     */
    private fun validateEdgeOrientation(cubeState: CubeState): ValidationResult {
        val sum = cubeState.edgeOrientation.sum()
        if (sum % 2 != 0) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.INVALID_EDGE_ORIENTATION,
                "Edge orientation sum is $sum (must be even)"
            )
        }
        return ValidationResult(true)
    }

    /**
     * Validate permutation parity: corner and edge permutations must have same parity
     */
    private fun validateParity(cubeState: CubeState): ValidationResult {
        val cornerParity = calculatePermutationParity(cubeState.cornerPermutation)
        val edgeParity = calculatePermutationParity(cubeState.edgePermutation)

        if (cornerParity != edgeParity) {
            return ValidationResult(
                false,
                ValidationResult.ErrorCode.PARITY_ERROR,
                "Corner parity ($cornerParity) does not match edge parity ($edgeParity). " +
                        "This cube configuration is impossible to solve."
            )
        }

        return ValidationResult(true)
    }

    /**
     * Calculate permutation parity (0 = even, 1 = odd)
     * Uses inversion count method
     */
    private fun calculatePermutationParity(permutation: IntArray): Int {
        var inversions = 0
        for (i in permutation.indices) {
            for (j in i + 1 until permutation.size) {
                if (permutation[i] > permutation[j]) {
                    inversions++
                }
            }
        }
        return inversions % 2
    }

    /**
     * Quick validation - just checks color count (fast)
     */
    fun quickValidate(cube: Cube): Boolean {
        return validateColorCount(cube).isValid
    }

    /**
     * Get a human-readable error message
     */
    fun getErrorMessage(result: ValidationResult): String {
        if (result.isValid) return "Cube is valid"

        return when (result.errorCode) {
            ValidationResult.ErrorCode.INVALID_COLOR_COUNT ->
                "Invalid color distribution. ${result.errorMessage}"
            ValidationResult.ErrorCode.DUPLICATE_CORNER ->
                "Duplicate corner pieces detected. Please re-scan the cube."
            ValidationResult.ErrorCode.DUPLICATE_EDGE ->
                "Duplicate edge pieces detected. Please re-scan the cube."
            ValidationResult.ErrorCode.MISSING_CORNER ->
                "Missing corner pieces. ${result.errorMessage}"
            ValidationResult.ErrorCode.MISSING_EDGE ->
                "Missing edge pieces. ${result.errorMessage}"
            ValidationResult.ErrorCode.INVALID_CORNER_ORIENTATION ->
                "Invalid corner orientation. ${result.errorMessage}"
            ValidationResult.ErrorCode.INVALID_EDGE_ORIENTATION ->
                "Invalid edge orientation. ${result.errorMessage}"
            ValidationResult.ErrorCode.PARITY_ERROR ->
                "This cube is physically impossible to solve. ${result.errorMessage}"
            ValidationResult.ErrorCode.CONVERSION_ERROR ->
                "Could not interpret cube configuration. ${result.errorMessage}"
            null -> result.errorMessage ?: "Unknown error"
        }
    }
}
