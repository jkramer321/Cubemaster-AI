package com.cs407.cubemaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    .weight(2f)
                    .padding(32.dp)
                    .border(4.dp, LightOrange, RoundedCornerShape(16.dp))
            ) {
                // This is the top half for the camera view
                Button(
                    onClick = { navController.navigate("validation") },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(text = "Next")
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkOrange),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please keep a single side\nfacing the camera",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }
            }
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
