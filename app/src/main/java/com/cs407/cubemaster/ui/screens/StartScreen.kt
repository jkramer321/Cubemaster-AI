package com.cs407.cubemaster.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.Cube
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.ui.theme.Blue
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkYellow

@Composable
fun StartScreen(modifier: Modifier = Modifier, navController: NavController) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(elevation = 16.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(DarkYellow),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rubik_icon),
                contentDescription = stringResource(R.string.cd_rubik_icon),
                modifier = Modifier.size(150.dp)
            )
        }
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Box {
                Text(
                    text = stringResource(R.string.app_title),
                    style = TextStyle(
                        fontSize = 48.sp,
                        color = Blue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
                Text(
                    text = stringResource(R.string.app_title),
                    style = TextStyle(
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_message),
                style = TextStyle(
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navController.navigate("permission") },
                    modifier = Modifier
                        .size(width = 200.dp, height = 80.dp)
                ) {
                    Text(text = stringResource(R.string.button_start), fontSize = 24.sp)
                }

            }
            // Debug button to test face mapping with a known cube pattern
//            Button(
//                onClick = {
//                    CubeHolder.scannedCube = createTestCube()
//                    navController.navigate("validation")
//                },
//                modifier = Modifier.padding(bottom = 16.dp)
//            ) {
//                Text(text = stringResource(R.string.test_face_mapping), fontSize = 12.sp)
//            }
        }
    }
}

/**
 * Creates a test cube with distinctive corner markers for each face.
 * Each face has a unique color in each corner to verify orientation:
 * - [0][0] = top-left, [0][2] = top-right
 * - [2][0] = bottom-left, [2][2] = bottom-right
 * 
 * Color codes: 0=White, 1=Red, 2=Blue, 3=Orange, 4=Green, 5=Yellow
 * 
 * Expected appearance when viewed in 3D:
 * - Front (s1/White): Red top-left, Blue top-right, Orange bottom-left, Green bottom-right
 * - Top (s2/Yellow): Red top-left (back-left), Blue top-right (back-right)
 * - Bottom (s3/Red): Green top-left (front-left), Orange top-right (front-right)
 */
private fun createTestCube(): Cube {
    // Front face (s1) - White center with colored corners
    val s1 = mutableListOf(
        mutableListOf(1, 0, 2),  // Row 0: Red, White, Blue
        mutableListOf(0, 0, 0),  // Row 1: White, White, White
        mutableListOf(3, 0, 4)   // Row 2: Orange, White, Green
    )
    
    // Top face (s2) - Yellow center with colored corners
    // Row 0 = back of cube, Row 2 = front of cube
    val s2 = mutableListOf(
        mutableListOf(1, 5, 2),  // Row 0 (back): Red, Yellow, Blue
        mutableListOf(5, 5, 5),  // Row 1: Yellow, Yellow, Yellow
        mutableListOf(3, 5, 4)   // Row 2 (front): Orange, Yellow, Green
    )
    
    // Bottom face (s3) - Red center with colored corners
    // Row 0 = front of cube, Row 2 = back of cube
    val s3 = mutableListOf(
        mutableListOf(4, 1, 3),  // Row 0 (front): Green, Red, Orange
        mutableListOf(1, 1, 1),  // Row 1: Red, Red, Red
        mutableListOf(2, 1, 0)   // Row 2 (back): Blue, Red, White
    )
    
    // Left face (s4) - Orange center with colored corners
    val s4 = mutableListOf(
        mutableListOf(5, 3, 1),  // Row 0: Yellow, Orange, Red
        mutableListOf(3, 3, 3),  // Row 1: Orange, Orange, Orange
        mutableListOf(4, 3, 2)   // Row 2: Green, Orange, Blue
    )
    
    // Right face (s5) - Blue center with colored corners
    val s5 = mutableListOf(
        mutableListOf(1, 2, 5),  // Row 0: Red, Blue, Yellow
        mutableListOf(2, 2, 2),  // Row 1: Blue, Blue, Blue
        mutableListOf(4, 2, 0)   // Row 2: Green, Blue, White
    )
    
    // Back face (s6) - Green center with colored corners
    // Note: Columns are mirrored when viewed from behind
    val s6 = mutableListOf(
        mutableListOf(2, 4, 1),  // Row 0: Blue, Green, Red (appears as Red, Green, Blue from behind)
        mutableListOf(4, 4, 4),  // Row 1: Green, Green, Green
        mutableListOf(0, 4, 5)   // Row 2: White, Green, Yellow (appears as Yellow, Green, White from behind)
    )
    
    return Cube(s1, s2, s3, s4, s5, s6)
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    CubemasterTheme {
        // In preview, we can't use a real NavController, so we pass a dummy one.
        StartScreen(navController = rememberNavController())
    }
}
