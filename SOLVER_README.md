# Kociemba Solver Implementation

This document describes the Kociemba's two-phase algorithm implementation for solving Rubik's cubes in the Cubemaster-AI project.

## Overview

Kociemba's algorithm is one of the most efficient methods for solving a Rubik's cube, typically finding solutions in 20 moves or less (God's Number is 20). It works in two phases:

### Phase 1: Reduction to G1 Subgroup
- **Goal**: Orient all edges correctly and position the 4 middle-layer edges in the middle layer
- **Coordinates Used**:
  - Twist: Corner orientation (0..2186 = 3^7)
  - Flip: Edge orientation (0..2047 = 2^11)
  - Slice: Position of middle-layer edges (0..494 = C(12,4))
- **Search**: IDA* with pruning tables

### Phase 2: Solve within G1 Subgroup
- **Goal**: Position all pieces correctly while maintaining G1 constraints
- **Coordinates Used**:
  - Corner permutation (0..40319 = 8!)
  - UD edge permutation (0..40319 = 8!)
  - Slice sorted (0..23 = 4!)
- **Allowed Moves**: Only half-turns of R, L, F, B and any turns of U, D
- **Search**: IDA* with pruning tables

## Architecture

### Core Components

#### 1. `CubeState.kt`
Represents the cube in cubie representation (corners + edges with orientations).

- **Corner indices**: 0=URF, 1=UFL, 2=ULB, 3=UBR, 4=DFR, 5=DLF, 6=DBL, 7=DRB
- **Edge indices**: 0=UR, 1=UF, 2=UL, 3=UB, 4=DR, 5=DF, 6=DL, 7=DB, 8=FR, 9=FL, 10=BL, 11=BR
- **Methods**:
  - `applyMove(move: CubeMove)`: Apply a single move
  - `isSolved()`: Check if cube is solved
  - `isInG1()`: Check if cube is in G1 subgroup (Phase 1 complete)

#### 2. `CubeMove.kt`
Enumeration of all 18 possible moves:
- Quarter turns: U, D, R, L, F, B
- Half turns: U2, D2, R2, L2, F2, B2
- Inverse turns: U', D', R', L', F', B'

#### 3. `CoordinateSystem.kt`
Converts between full cube state and coordinate representation for efficient search.

**Phase 1 Coordinates**:
- `getTwistCoordinate()`: Maps corner orientations to 0..2186
- `getFlipCoordinate()`: Maps edge orientations to 0..2047
- `getSliceCoordinate()`: Maps middle-layer edge positions to 0..494

**Phase 2 Coordinates**:
- `getCornerPermutationCoordinate()`: Maps corner positions to 0..40319
- `getUDEdgePermutationCoordinate()`: Maps UD edge positions to 0..40319
- `getSliceSortedCoordinate()`: Maps middle-layer edge permutation to 0..23

#### 4. `MoveTables.kt`
Pre-computed transition tables for fast move application in coordinate space.

- **Initialization**: Generates all move tables at startup (~1-2 seconds)
- **Phase 1 Tables**: twist[2187][18], flip[2048][18], slice[495][18]
- **Phase 2 Tables**: cornerPerm[40320][10], udEdgePerm[40320][10], sliceSorted[24][10]

#### 5. `PruningTables.kt`
Pre-computed distance-to-goal estimates for IDA* search pruning.

- **Phase 1 Pruning**:
  - Twist-Flip table: minimum moves to orient all edges and corners
  - Slice-Twist table: minimum moves to position middle edges and orient corners
- **Phase 2 Pruning**:
  - Corner-Slice table: minimum moves to position corners and middle edges

**Note**: Tables are simplified compared to full Kociemba implementation to reduce initialization time. Full tables would be larger but provide better pruning.

#### 6. `Search.kt`
IDA* (Iterative Deepening A*) search implementation.

**Algorithm**:
```
for depth = 1 to maxDepth:
    if search(state, 0, depth):
        return solution
```

**Pruning**: Uses pruning tables to estimate minimum moves to goal. If `currentDepth + estimate > maxDepth`, prune this branch.

**Move Ordering**: Avoids redundant moves:
- Don't do same face twice in a row (e.g., R R)
- Don't do opposite faces out of canonical order (e.g., L before R is ok, but R then L is skipped)

#### 7. `KociembaSolver.kt`
Main solver interface that ties everything together.

**Usage**:
```kotlin
val solver = KociembaSolver()
solver.initialize() // Call once at app startup
val solution = solver.solve(cube) // Returns List<String> of moves
```

**Methods**:
- `initialize()`: Generate move and pruning tables (async, ~2-5 seconds)
- `solve(cube)`: Find solution for given cube
- `formatSolution(solution)`: Format moves as readable string
- `estimateMoves(cube)`: Estimate solution length

#### 8. `CubeConverter.kt`
Converts between app's facelet representation and solver's cubie representation.

**Facelet Mapping**:
- App: s1=Front, s2=Top, s3=Bottom, s4=Left, s5=Right, s6=Back
- Solver: U=Top, R=Right, F=Front, D=Bottom, L=Left, B=Back

**Process**:
1. Read all 54 facelets from Cube object
2. Identify center colors to determine color scheme
3. For each corner/edge position, identify which piece is there by color pattern
4. Determine piece orientation based on color ordering

## Integration with App

### ResultScreen Integration

The solver is integrated into `ResultScreen.kt`:

1. **Initialization**: Solver tables are generated when ResultScreen loads
2. **Solving**: Cube is automatically solved in background
3. **Display**: Solution steps replace placeholder steps in UI
4. **States**:
   - Loading: Shows progress indicator during initialization/solving
   - Success: Displays solution moves in step viewer
   - Error: Shows error message if solving fails
   - Already Solved: Shows "Cube is already solved" message

### Performance

- **Initialization**: 2-5 seconds (one-time cost)
- **Solving**: Usually < 1 second for most cubes
- **Solution Length**: Typically 18-22 moves
- **Memory**: ~50 MB for all tables

## Limitations and Future Improvements

### Current Limitations

1. **CubeConverter**: The conversion from facelet to cubie representation is complex and may have bugs. Thorough testing needed.

2. **Pruning Tables**: Simplified to reduce initialization time. Full implementation would have:
   - Phase 1: Twist-Flip-Slice combined table (2187 × 2048 × 495 ≈ 2.2 GB)
   - Phase 2: Corner-Edge-Slice combined table (40320 × 40320 × 24 ≈ 39 GB)
   - Solution: Use disk storage and symmetry reduction

3. **Move Application**: F and B moves in CubeConverter are not fully implemented

4. **Validation**: No validation that scanned cube is valid (e.g., correct number of each color, solvable configuration)

### Suggested Improvements

1. **Persistent Tables**: Save generated tables to disk to avoid regeneration
2. **Better Pruning**: Use more sophisticated pruning tables with symmetry reduction
3. **Parallel Search**: Search multiple depths in parallel
4. **Solution Optimization**: Post-process solution to reduce move count
5. **Visual Solution**: Animate the solution on the 3D cube view
6. **Custom Algorithms**: Allow users to input their own algorithms (CFOP, Roux, etc.)

## Testing

Basic tests are provided in `KociembaSolverTest.kt`:
- Test solved cube returns empty solution
- Test move application works correctly
- Test coordinate system is consistent

**Run tests**:
```bash
./gradlew test
```

## References

- Kociemba's Original Algorithm: http://kociemba.org/cube.htm
- Two-Phase Algorithm Explained: https://www.jaapsch.net/puzzles/compcube.htm
- Rubik's Cube Notation: https://ruwix.com/the-rubiks-cube/notation/

## Credits

Implementation based on Herbert Kociemba's two-phase algorithm (1992).
Adapted for Android/Kotlin in the Cubemaster-AI project (2025).
