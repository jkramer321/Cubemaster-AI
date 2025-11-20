package com.cs407.cubemaster

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.cs407.cubemaster.ui.navigation.AppNavigation
import com.cs407.cubemaster.ui.theme.CubemasterTheme
import com.cs407.cubemaster.ui.theme.DarkOrange
import androidx.compose.runtime.remember
import com.cs407.cubemaster.ui.theme.DarkBlue
import com.cs407.cubemaster.ui.theme.DarkGreen
import com.cs407.cubemaster.ui.theme.LightBlue
import com.cs407.cubemaster.ui.theme.LightGreen
import com.cs407.cubemaster.ui.theme.LightOrange

class MainActivity : ComponentActivity() {

    private var permissionGranted by mutableStateOf(false)
    private var showPermissionDeniedMessage by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (!isGranted) {
            showPermissionDeniedMessage = !ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var theme by remember { mutableStateOf("Orange") } // Theme state

            CubemasterTheme(theme = theme) {
                val gradientBrush = Brush.verticalGradient(
                    colors = when (theme) {
                        "Blue" -> listOf(LightBlue, DarkBlue)
                        "Green" -> listOf(LightGreen, DarkGreen)
                        else -> listOf(LightOrange, DarkOrange)
                    }
                )
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush)
                    ) {
                        AppNavigation(
                            permissionGranted = permissionGranted,
                            showPermissionDeniedMessage = showPermissionDeniedMessage,
                            requestPermission = {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            openSettings = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            },
                            setTheme = { newTheme -> theme = newTheme } // Pass lambda to update theme
                        )
                    }
                }
            }
        }
    }
}