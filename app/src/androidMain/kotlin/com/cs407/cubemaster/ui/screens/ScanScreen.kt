package com.cs407.cubemaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange

@Composable
fun ScanScreen(modifier: Modifier = Modifier, navController: NavController) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            LightOrange,
            DarkOrange
        )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp)
                    .border(4.dp, LightOrange)
            ) {
                // This is the top half for the camera view
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp)
                    .background(LightOrange)
            ) {
                // This is the bottom half for the controls
            }
        }
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text(text = "Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    CubemasterTheme {
        ScanScreen(navController = rememberNavController())
    }
}
