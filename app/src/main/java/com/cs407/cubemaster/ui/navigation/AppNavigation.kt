package com.cs407.cubemaster.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.ui.screens.PermissionScreen
import com.cs407.cubemaster.ui.screens.ResultScreen
import com.cs407.cubemaster.ui.screens.ScanScreen
import com.cs407.cubemaster.ui.screens.StartScreen
import com.cs407.cubemaster.ui.screens.TimerScreen
import com.cs407.cubemaster.ui.screens.ValidationScreen

@Composable
fun AppNavigation(
    permissionGranted: Boolean,
    showPermissionDeniedMessage: Boolean,
    requestPermission: () -> Unit,
    openSettings: () -> Unit
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable(
            "start",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) },
            popEnterTransition = { fadeIn(animationSpec = tween(700)) },
            popExitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            StartScreen(navController = navController)
        }
        composable(
            "permission",
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) }
        ) {
            PermissionScreen(
                navController = navController,
                permissionGranted = permissionGranted,
                showPermissionDeniedMessage = showPermissionDeniedMessage,
                requestPermission = requestPermission,
                openSettings = openSettings
            )
        }
        composable(
            "scan",
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) }
        ) {
            ScanScreen(navController = navController)
        }
        composable(
            "validation",
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) }
        ) {
            ValidationScreen(navController = navController)
        }
        composable(
            "result",
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(700)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(700)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) }
        ) {
            ResultScreen(navController = navController)
        }
        composable(
            "timer",
            enterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(700)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(700)) }
        ) {
            TimerScreen(navController = navController)
        }
    }
}
