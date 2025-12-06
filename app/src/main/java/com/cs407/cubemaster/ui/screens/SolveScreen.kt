package com.cs407.cubemaster.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.solver.KociembaSolver
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import kotlinx.coroutines.launch


@Composable
fun SolveScreen(navController: NavController) {
    val gradientBrush = Brush.verticalGradient(colors = listOf(LightOrange, DarkOrange))
    val context = LocalContext.current
    // Solver state
    val solver = remember { KociembaSolver() }
    var solutionSteps by remember { mutableStateOf((1..10).map { "Step $it" }) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showBackDialog by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(1) }

    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text(stringResource(R.string.timer_back_title)) },
            text = { Text(stringResource(R.string.timer_back_message)) },
            confirmButton = {
                Button(onClick = {
                    navController.popBackStack()
                    showBackDialog = false
                }) {
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                Button(onClick = { showBackDialog = false }) {
                    Text(stringResource(R.string.button_no))
                }
            }
        )
    }

    // Initialize solver and compute solution when screen loads
    LaunchedEffect(Unit) {
        // Disabled for now to work with placeholder data
        // scope.launch {
        //     try {
        //         isLoading = true
        //         solver.initialize()
        //
        //         val cube = CubeHolder.scannedCube
        //         if (cube != null) {
        //             // Validate cube first
        //             val validationResult = com.cs407.cubemaster.solver.CubeValidator.validate(cube)
        //             if (!validationResult.isValid) {
        //                 errorMessage = com.cs407.cubemaster.solver.CubeValidator.getErrorMessage(validationResult)
        //                 isLoading = false
        //                 return@launch
        //             }
        //
        //             // Check if already solved
        //             if (cube.isSolved()) {
        //                 solutionSteps = listOf(context.getString(R.string.cube_already_solved))
        //                 isLoading = false
        //                 return@launch
        //             }
        //
        //             // Solve the cube
        //             val solution = solver.solve(cube)
        //             if (solution != null) {
        //                 solutionSteps = solution
        //             } else {
        //                 errorMessage = context.getString(R.string.error_no_solution)
        //             }
        //         }
        //     } catch (t: Throwable) {
        //         errorMessage = "${context.getString(R.string.error_solving_cube)}: ${t.message}"
        //     } finally {
        //         isLoading = false
        //     }
        // }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        val duration = 700 // Animation duration in milliseconds
                        if (targetState > initialState) { // Moving to a higher step (e.g., 1 -> 2)
                            (slideInVertically(animationSpec = tween(duration)) { fullHeight -> -fullHeight } +
                                    fadeIn(animationSpec = tween(duration))) togetherWith
                                    (slideOutVertically(animationSpec = tween(duration)) { fullHeight -> fullHeight } +
                                            fadeOut(animationSpec = tween(duration)))
                        } else { // Moving to a lower step (e.g., 2 -> 1)
                            (slideInVertically(animationSpec = tween(duration)) { fullHeight -> fullHeight } +
                                    fadeIn(animationSpec = tween(duration))) togetherWith
                                    (slideOutVertically(animationSpec = tween(duration)) { fullHeight -> -fullHeight } +
                                            fadeOut(animationSpec = tween(duration)))
                        }
                    }, label = ""
                ) { targetStep ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Next step
                        if (targetStep < solutionSteps.size) {
                            StepBox(
                                step = targetStep + 1,
                                isCurrent = false,
                                onClick = { currentStep = targetStep + 1 }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Current step
                        StepBox(
                            step = targetStep,
                            isCurrent = true,
                            onClick = { /* Does nothing for now */ }
                        )

                        // Previous step
                        if (targetStep > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            StepBox(
                                step = targetStep - 1,
                                isCurrent = false,
                                onClick = { currentStep = targetStep - 1 }
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = { showBackDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
        ) {
            Text(text = stringResource(R.string.button_back))
        }
    }
}

@Composable
fun StepBox(step: Int, isCurrent: Boolean, onClick: () -> Unit) {
    val boxModifier = if (isCurrent) {
        Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(DarkOrange)
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LightOrange)
            .clickable(onClick = onClick)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Step $step",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (isCurrent) 30.sp else 20.sp
        )
    }
}