package com.cs407.cubemaster.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.ui.components.Interactive3DCube
import com.cs407.cubemaster.ui.components.createSolvedCube
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

@Composable
fun ResultScreen(modifier: Modifier = Modifier, navController: NavController) {
    val gradientBrush = Brush.verticalGradient(colors = listOf(LightOrange, DarkOrange))
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showAnalysis by remember { mutableStateOf(true) }  // Changed to true - default to CUBE

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.dialog_are_you_sure)) },
            text = { Text(stringResource(R.string.dialog_lose_progress)) },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("start")
                    showDialog = false
                }) {
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.button_no))
                }
            }
        )
    }

    Column(modifier = modifier.fillMaxSize().background(gradientBrush)) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showAnalysis) {
                // Use scanned cube if available, otherwise show solved cube
                Interactive3DCube(cube = CubeHolder.scannedCube ?: createSolvedCube())
            } else {
                BoxWithConstraints {
                    val boxWidth = this.maxWidth
                    var currentStep by remember { mutableStateOf(1) }
                    val steps = (1..10).map { stringResource(R.string.step_template, it) }

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInVertically(animationSpec = tween(600)) { -it } togetherWith slideOutVertically(animationSpec = tween(600)) { it }
                            } else {
                                slideInVertically(animationSpec = tween(600)) { it } togetherWith slideOutVertically(animationSpec = tween(600)) { -it }
                            }
                        },
                        label = "step"
                    ) { targetStep ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                        ) {
                            if (targetStep < steps.size) {
                                StepCard(targetStep + 1, false, steps[targetStep], { currentStep++ })
                            }
                            StepCard(targetStep, true, steps[targetStep - 1], {}, boxWidth)
                            if (targetStep > 1) {
                                StepCard(targetStep - 1, false, steps[targetStep - 2], { currentStep-- })
                            }
                        }
                    }
                }
            }
        }

        ModernBottomNavBar(
            navController = navController,
            currentScreen = if (showAnalysis) "cube" else "steps",
            onNavigate = { dest ->
                when (dest) {
                    "home" -> showDialog = true
                    "timer" -> navController.navigate("timer")
                    "cube" -> showAnalysis = true
                    "profile" -> navController.navigate("profile")
                    "guide" -> {
                        val file = File(context.cacheDir, "official_guide.pdf")
                        if (!file.exists()) {
                            try {
                                context.assets.open("official_guide.pdf").use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, context.getString(R.string.error_generic), Toast.LENGTH_SHORT).show()
                            }
                        }
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, context.getString(R.string.error_no_pdf_viewer), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ModernBottomNavBar(navController: NavController, currentScreen: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp).background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(Icons.Default.Home, stringResource(R.string.nav_home), false) { onNavigate("home") }
        NavBarItem(Icons.Default.Timer, stringResource(R.string.nav_timer), false) { onNavigate("timer") }
        CubeNavBarItem(currentScreen == "cube") { onNavigate("cube") }
        NavBarItem(Icons.Default.Person, stringResource(R.string.nav_profile), false) { onNavigate("profile") }
        NavBarItem(Icons.Default.School, stringResource(R.string.nav_guide), false) { onNavigate("guide") }
    }
}

@Composable
fun NavBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp).fillMaxHeight().clickable(onClick = onClick).padding(4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFFA726) else Color.White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            color = if (isSelected) Color(0xFFFFA726) else Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun CubeNavBarItem(isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(64.dp).clip(CircleShape)
            .background(if (isSelected) Color(0xFF4CAF50) else Color(0xFF2E7D32))
            .clickable(onClick = onClick)
            .border(3.dp, if (isSelected) Color(0xFF81C784) else Color(0xFF4CAF50), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = stringResource(R.string.cd_cube),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Text(text = stringResource(R.string.nav_cube), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StepCard(step: Int, isExpanded: Boolean, content: String, onClick: () -> Unit, width: Dp? = null) {
    val height by animateDpAsState(
        targetValue = if (isExpanded) width ?: 120.dp else 50.dp,
        animationSpec = tween(300),
        label = "height"
    )

    Card(
        modifier = (if (isExpanded) Modifier.width(width ?: 120.dp) else Modifier.fillMaxWidth())
            .height(height).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) LightOrange else DarkOrange.copy(alpha = 0.6f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    text = stringResource(R.string.step_label, step),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenPreview() {
    CubemasterTheme {
        ResultScreen(navController = rememberNavController())
    }
}