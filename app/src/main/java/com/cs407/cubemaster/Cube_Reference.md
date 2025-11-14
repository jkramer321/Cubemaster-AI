# Quick Reference: Cube Structure

## Color Codes
```
0 = White   (Front - s1)
1 = Red     (Bottom - s3)
2 = Blue    (Right - s5)
3 = Orange  (Left - s4)
4 = Green   (Back - s6)
5 = Yellow  (Top - s2)
```

## Standard Rubik's Cube Orientation
```
         +-------+
         | YELLOW|  (s2 - Top)
         |   5   |
         +-------+
+-------++-------++-------++-------+
|ORANGE ||WHITE ||BLUE   ||GREEN  |
|   3   ||   0  ||   2   ||   4   |
| (s4)  || (s1) || (s5)  || (s6)  |
+-------++-------++-------++-------+
         +-------+
         |  RED  |  (s3 - Bottom)
         |   1   |
         +-------+
```

## Coordinate System
The 3D cube uses a standard coordinate system:
```
     +Y (Up)
      |
      |
      +---- +X (Right)
     /
    /
  +Z (Toward viewer)
```

## Cubie Positions
Each cubie has a position (x, y, z) where each coordinate is -1, 0, or 1:

### Corner Cubies (8 total)
- (-1, +1, +1) - Front Top Left
- (+1, +1, +1) - Front Top Right
- (-1, -1, +1) - Front Bottom Left
- (+1, -1, +1) - Front Bottom Right
- (-1, +1, -1) - Back Top Left
- (+1, +1, -1) - Back Top Right
- (-1, -1, -1) - Back Bottom Left
- (+1, -1, -1) - Back Bottom Right

### Edge Cubies (12 total)
- (0, +1, +1) - Front Top
- (0, -1, +1) - Front Bottom
- (-1, 0, +1) - Front Left
- (+1, 0, +1) - Front Right
- (0, +1, -1) - Back Top
- (0, -1, -1) - Back Bottom
- (-1, 0, -1) - Back Left
- (+1, 0, -1) - Back Right
- (-1, +1, 0) - Top Left
- (+1, +1, 0) - Top Right
- (-1, -1, 0) - Bottom Left
- (+1, -1, 0) - Bottom Right

### Center Cubies (6 total)
- (0, 0, +1) - Front Center
- (0, 0, -1) - Back Center
- (0, +1, 0) - Top Center
- (0, -1, 0) - Bottom Center
- (-1, 0, 0) - Left Center
- (+1, 0, 0) - Right Center

### Core Cubie (1 total)
- (0, 0, 0) - Center (not visible)

## Face-to-Coordinate Mapping

### Front Face (s1, z = +1)
```
Row 0: y = +1
Row 1: y =  0
Row 2: y = -1

Col 0: x = -1
Col 1: x =  0
Col 2: x = +1
```

### Top Face (s2, y = +1)
```
Row 0: z = -1
Row 1: z =  0
Row 2: z = +1

Col 0: x = -1
Col 1: x =  0
Col 2: x = +1
```

### Bottom Face (s3, y = -1)
```
Row 0: z = +1
Row 1: z =  0
Row 2: z = -1

Col 0: x = -1
Col 1: x =  0
Col 2: x = +1
```

### Left Face (s4, x = -1)
```
Row 0: y = +1
Row 1: y =  0
Row 2: y = -1

Col 0: z = +1
Col 1: z =  0
Col 2: z = -1
```

### Right Face (s5, x = +1)
```
Row 0: y = +1
Row 1: y =  0
Row 2: y = -1

Col 0: z = -1
Col 1: z =  0
Col 2: z = +1
```

### Back Face (s6, z = -1)
```
Row 0: y = +1
Row 1: y =  0
Row 2: y = -1

Col 0: x = +1 (mirrored)
Col 1: x =  0
Col 2: x = -1 (mirrored)
```

## Example: Finding a Cubie's Colors

For the corner cubie at position (+1, +1, +1):
- This is the **Front Top Right** corner
- It has 3 visible faces:
    - **Front face** (z = +1): Get color from s1[0][2]
    - **Top face** (y = +1): Get color from s2[2][2]
    - **Right face** (x = +1): Get color from s5[0][2]

## Rotation Reference

### Face Rotations (90° clockwise)
- **R** (Right): Rotate x = +1 slice clockwise
- **L** (Left): Rotate x = -1 slice clockwise
- **U** (Up): Rotate y = +1 slice clockwise
- **D** (Down): Rotate y = -1 slice clockwise
- **F** (Front): Rotate z = +1 slice clockwise
- **B** (Back): Rotate z = -1 slice clockwise

### Counter-clockwise (add prime ')
- **R'**: Right counter-clockwise
- **L'**: Left counter-clockwise
- **U'**: Up counter-clockwise
- **D'**: Down counter-clockwise
- **F'**: Front counter-clockwise
- **B'**: Back counter-clockwise

### Double rotations (180°)
- **R2**: Right 180°
- **L2**: Left 180°
- etc.