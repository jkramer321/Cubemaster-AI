package com.cs407.cubemaster.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
fun ResultScreen(modifier: Modifier = Modifier, navController: NavController) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            LightOrange,
            DarkOrange
        )
    )
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showAnalysis by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Returning home will cause you to lose your progress.") },
            confirmButton = {
                Button(
                    onClick = {
                        navController.navigate("start")
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // Top box for the steps
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showAnalysis) {
                AnalysisView()
            } else {
                BoxWithConstraints {
                    val boxWidth = this.maxWidth
                    var currentStep by remember { mutableStateOf(1) }
                    val steps = (1..10).map { "Step $it: Perform this action." }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                    ) {
                        if (currentStep < steps.size) {
                            StepCard(
                                step = currentStep + 1,
                                isExpanded = false,
                                content = steps[currentStep],
                                onClick = { currentStep++ }
                            )
                        }

                        StepCard(
                            step = currentStep,
                            isExpanded = true,
                            content = steps[currentStep - 1],
                            onClick = { /* Already expanded */ },
                            width = boxWidth
                        )

                        if (currentStep > 1) {
                            StepCard(
                                step = currentStep - 1,
                                isExpanded = false,
                                content = steps[currentStep - 2],
                                onClick = { currentStep-- }
                            )
                        }
                    }
                }
            }
        }

        // Bottom navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f) // Adjusted weight for a bottom-bar feel
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(DarkOrange),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                onClick = { showDialog = true }
            )
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.List,
                label = "Steps",
                onClick = { showAnalysis = false }
            )
            BottomNavItem(
                icon = Icons.Default.Analytics,
                label = "Analysis",
                onClick = { showAnalysis = true }
            )
            BottomNavItem(
                icon = Icons.Default.School,
                label = "Guide",
                onClick = {
                    val assetManager = context.assets
                    val pdfName = "official_guide.pdf"
                    val file = File(context.cacheDir, pdfName)
                    if (!file.exists()) {
                        try {
                            assetManager.open(pdfName).use { inputStream ->
                                FileOutputStream(file).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Error copying file", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val uri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_LONG).show()
                    }
                }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun AnalysisView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Total Steps: 10",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Composable
fun StepCard(step: Int, isExpanded: Boolean, content: String, onClick: () -> Unit, width: Dp? = null) {
    val height by animateDpAsState(
        targetValue = if (isExpanded) width ?: 120.dp else 50.dp,
        animationSpec = tween(300)
    )

    val cardModifier = (if (isExpanded) Modifier.width(width ?: 120.dp) else Modifier.fillMaxWidth())
        .height(height)
        .clickable(onClick = onClick)

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) LightOrange else DarkOrange.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isExpanded) {
                Text(
                    text = content,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                Text(
                    text = "Step $step",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    CubemasterTheme {
        ResultScreen(navController = rememberNavController())
    }
}