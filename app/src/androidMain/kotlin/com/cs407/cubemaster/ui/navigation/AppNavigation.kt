package com.cs407.cubemaster.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.ui.screens.PermissionScreen
import com.cs407.cubemaster.ui.screens.ScanScreen
import com.cs407.cubemaster.ui.screens.StartScreen

@Composable
fun AppNavigation(
    permissionGranted: Boolean,
    showPermissionDeniedMessage: Boolean,
    requestPermission: () -> Unit,
    openSettings: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable("start") {
            StartScreen(navController = navController)
        }
        composable("permission") {
            PermissionScreen(
                navController = navController,
                permissionGranted = permissionGranted,
                showPermissionDeniedMessage = showPermissionDeniedMessage,
                requestPermission = requestPermission,
                openSettings = openSettings
            )
        }
        composable("scan") {
            ScanScreen(navController = navController)
        }
    }
}
