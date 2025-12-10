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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.cs407.cubemaster.data.Cube
import com.cs407.cubemaster.data.CubeHolder
import com.cs407.cubemaster.solver.KociembaSolver
import com.cs407.cubemaster.solver.SolverLog
import com.cs407.cubemaster.ui.components.Interactive3DCube
import com.cs407.cubemaster.ui.components.performMove
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import kotlinx.coroutines.launch


@Composable
fun SolveScreen(navController: NavController) {
    val gradientBrush = Brush.verticalGradient(colors = listOf(LightOrange, DarkOrange))
    val context = LocalContext.current
    // Solver state
    val solver = remember { KociembaSolver() }
    var solutionSteps by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var showBackDialog by remember { mutableStateOf(false) }
    var expandedStep by remember { mutableStateOf<Int?>(null) }

    // Compute cube states for each step
    val cubeStates = remember(solutionSteps) {
        if (solutionSteps.isEmpty()) {
            emptyList()
        } else {
            val initialCube = CubeHolder.scannedCube ?: com.cs407.cubemaster.ui.components.createSolvedCube()
            val states = mutableListOf(initialCube)
            var currentCube = initialCube
            solutionSteps.forEach { move ->
                currentCube = performMove(currentCube, move)
                states.add(currentCube)
            }
            states
        }
    }

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
        scope.launch {
            try {
                isLoading = true
                solver.initialize { name -> runCatching { context.assets.open(name) }.getOrNull() }
                SolverLog.d("SolveScreen", "Solver init source=${solver.lastInitSource()}")

                val cube = CubeHolder.scannedCube
                if (cube != null) {
                    // Validate cube first
                    val validationResult = com.cs407.cubemaster.solver.CubeValidator.validate(cube)
                    if (!validationResult.isValid) {
                        errorMessage = com.cs407.cubemaster.solver.CubeValidator.getErrorMessage(context, validationResult)
                        isLoading = false
                        return@launch
                    }

                    // Check if already solved
                    if (cube.isSolved()) {
                        errorMessage = context.getString(R.string.cube_already_solved)
                        isLoading = false
                        return@launch
                    }

                    // Solve the cube
                    val solution = solver.solve(cube)
                    if (solution != null) {
                        solutionSteps = solution
                    } else {
                        errorMessage = context.getString(R.string.error_no_solution)
                    }
                } else {
                    errorMessage = "No cube has been scanned. Please scan a cube first."
                }
            } catch (t: Throwable) {
                errorMessage = "${context.getString(R.string.error_solving_cube)}: ${t.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(solutionSteps) { index, move ->
                        StepBox(
                            stepNumber = index + 1,
                            move = move,
                            cubeState = cubeStates[index + 1],
                            isExpanded = expandedStep == index,
                            onClick = {
                                expandedStep = if (expandedStep == index) null else index
                            }
                        )
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
fun StepBox(
    stepNumber: Int,
    move: String,
    cubeState: Cube,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val boxModifier = if (isExpanded) {
        Modifier
            .fillMaxWidth()
            .height(500.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(DarkOrange)
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LightOrange)
            .clickable(onClick = onClick)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.step_format, stepNumber, move),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Interactive3DCube(
                        modifier = Modifier.fillMaxSize(),
                        cube = cubeState,
                        showControls = false
                    )
                }
            }
        } else {
            Text(
                text = stringResource(R.string.step_format, stepNumber, move),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}