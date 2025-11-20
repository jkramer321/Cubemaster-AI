package com.cs407.cubemaster.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(navController: NavController) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            LightOrange,
            DarkOrange
        )
    )
    var timeMillis by remember { mutableStateOf(0L) }
    var isActive by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showTimesFullDialog by remember { mutableStateOf(false) }
    var showBackDialog by remember { mutableStateOf(false) }
    val savedTimes = remember { mutableStateOf(listOf<Long>()) }
    var bestTime by remember { mutableStateOf(0L) }
    var worstTime by remember { mutableStateOf(0L) }
    var averageTime by remember { mutableStateOf(0L) }
    var average3of5 by remember { mutableStateOf(0L) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var timeToDelete by remember { mutableStateOf<Long?>(null) }


    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Timer") },
            text = { Text("Are you sure you want to reset the timer?") },
            confirmButton = {
                Button(
                    onClick = {
                        timeMillis = 0L
                        isActive = false
                        showResetDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showResetDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Time") },
            text = { Text("Are you sure you want to delete this time?") },
            confirmButton = {
                Button(
                    onClick = {
                        timeToDelete?.let { time ->
                            val newTimes = savedTimes.value.toMutableList()
                            newTimes.remove(time)
                            savedTimes.value = newTimes

                            bestTime = newTimes.minOrNull() ?: 0L
                            worstTime = newTimes.maxOrNull() ?: 0L
                            averageTime = if (newTimes.isNotEmpty()) newTimes.average().toLong() else 0L

                            if (newTimes.size == 5) {
                                val sortedTimes = newTimes.sorted()
                                val middleThree = sortedTimes.subList(1, 4)
                                average3of5 = middleThree.average().toLong()
                            } else {
                                average3of5 = 0L
                            }
                        }
                        showDeleteConfirmationDialog = false
                        timeToDelete = null
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmationDialog = false
                    timeToDelete = null
                }) {
                    Text("No")
                }
            }
        )
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Time") },
            text = { Text("Are you sure you want to save this time?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (savedTimes.value.size < 5) {
                            val newTimes = savedTimes.value + timeMillis
                            savedTimes.value = newTimes

                            bestTime = newTimes.minOrNull() ?: 0L
                            worstTime = newTimes.maxOrNull() ?: 0L
                            averageTime = if (newTimes.isNotEmpty()) newTimes.average().toLong() else 0L

                            if (newTimes.size == 5) {
                                val sortedTimes = newTimes.sorted()
                                val middleThree = sortedTimes.subList(1, 4)
                                average3of5 = middleThree.average().toLong()
                            }
                        }
                        showSaveDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showSaveDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showTimesFullDialog) {
        AlertDialog(
            onDismissRequest = { showTimesFullDialog = false },
            title = { Text("Saved Times Full") },
            text = { Text("Remove one to save current time.") },
            confirmButton = {
                Button(onClick = { showTimesFullDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Are you sure you want to go back?") },
            text = { Text("Any unsaved times will be lost.") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("result")
                    showBackDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showBackDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            while (true) {
                delay(10) // Update every 10 milliseconds
                timeMillis += 10
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Scramble",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "R' U' F R U' R' U' F' U2 R U' R' U F U F'",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = formatTime(timeMillis),
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { isActive = !isActive }) {
                    Text(text = if (isActive) "Stop" else "Start")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { showResetDialog = true }) {
                    Text(text = "Reset")
                }
                Button(onClick = {
                    if (savedTimes.value.size >= 5) {
                        showTimesFullDialog = true
                    } else {
                        showSaveDialog = true
                    }
                }) {
                    Text(text = "Save")
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(horizontal = 16.dp) // Added horizontal padding
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(DarkOrange)
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Best", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(bestTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Worst", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(worstTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Average", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(averageTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Average 3 of 5", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(average3of5), color = Color.White)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Saved Times", color = Color.White, fontWeight = FontWeight.Bold)
                    LazyColumn {
                        items(savedTimes.value) { time ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(formatTime(time), color = Color.White)
                                IconButton(onClick = {
                                    timeToDelete = time
                                    showDeleteConfirmationDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove Time",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showBackDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp) // Adjusted padding
        ) {
            Text(text = "Back")
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = (timeMillis % 1000) / 10
    return "%02d:%02d:%02d".format(minutes, seconds, milliseconds)
}
