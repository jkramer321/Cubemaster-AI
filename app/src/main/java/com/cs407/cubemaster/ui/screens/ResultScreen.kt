package com.cs407.cubemaster.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.ui.components.Interactive3DCube
import com.cs407.cubemaster.ui.components.ModernBottomNavBar
import com.cs407.cubemaster.ui.components.createSolvedCube
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import java.io.File
import java.io.FileOutputStream

@Composable
fun ResultScreen(modifier: Modifier = Modifier, navController: NavController) {
    val gradientBrush = Brush.verticalGradient(colors = listOf(LightOrange, DarkOrange))
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Use scanned cube if available, otherwise show solved cube
            Interactive3DCube(cube = CubeHolder.scannedCube ?: createSolvedCube())
        }

        ModernBottomNavBar(
            navController = navController,
            currentScreen = "cube",
            onNavigate = { dest ->
                when (dest) {
                    "home" -> showDialog = true
                    "timer" -> navController.navigate("timer")
                    "cube" -> navController.navigate("solve")
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
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_generic),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_no_pdf_viewer),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
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