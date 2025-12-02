package com.cs407.cubemaster.solver

enum class CubeMove(val notation: String) {
    U("U"),   // Up face clockwise
    U2("U2"), // Up face 180
    UP("U'"), // Up face counter-clockwise
    D("D"),   // Down face clockwise
    D2("D2"), // Down face 180
    DP("D'"), // Down face counter-clockwise
    R("R"),   // Right face clockwise
    R2("R2"), // Right face 180
    RP("R'"), // Right face counter-clockwise
    L("L"),   // Left face clockwise
    L2("L2"), // Left face 180
    LP("L'"), // Left face counter-clockwise
    F("F"),   // Front face clockwise
    F2("F2"), // Front face 180
    FP("F'"), // Front face counter-clockwise
    B("B"),   // Back face clockwise
    B2("B2"), // Back face 180
    BP("B'"); // Back face counter-clockwise

    companion object {
        // Phase 2 moves (only 180-degree turns and quarter turns of U, D)
        val PHASE2_MOVES = listOf(U, U2, UP, D, D2, DP, R2, L2, F2, B2)
    }
}
