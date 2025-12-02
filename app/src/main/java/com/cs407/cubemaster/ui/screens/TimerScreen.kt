package com.cs407.cubemaster.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader

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

    val context = LocalContext.current
    val scrambles = remember { mutableStateOf<List<String>>(emptyList()) }
    val loadingText = stringResource(R.string.timer_loading_scramble)
    val errorText = stringResource(R.string.error_failed_scrambles)
    var currentScramble by remember { mutableStateOf(loadingText) }

    LaunchedEffect(Unit) {
        try {
            val inputStream = context.assets.open("random_scrambles.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            scrambles.value = reader.readLines()
            currentScramble = scrambles.value.random()
        } catch (e: Exception) {
            currentScramble = errorText
        }
    }


    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.timer_reset_title)) },
            text = { Text(stringResource(R.string.timer_reset_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        timeMillis = 0L
                        isActive = false
                        if (scrambles.value.isNotEmpty()) {
                            currentScramble = scrambles.value.random()
                        }
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                Button(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.button_no))
                }
            }
        )
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(stringResource(R.string.timer_delete_title)) },
            text = { Text(stringResource(R.string.timer_delete_message)) },
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
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmationDialog = false
                    timeToDelete = null
                }) {
                    Text(stringResource(R.string.button_no))
                }
            }
        )
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.timer_save_title)) },
            text = { Text(stringResource(R.string.timer_save_message)) },
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
                    Text(stringResource(R.string.button_yes))
                }
            },
            dismissButton = {
                Button(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.button_no))
                }
            }
        )
    }

    if (showTimesFullDialog) {
        AlertDialog(
            onDismissRequest = { showTimesFullDialog = false },
            title = { Text(stringResource(R.string.timer_saved_full_title)) },
            text = { Text(stringResource(R.string.timer_saved_full_message)) },
            confirmButton = {
                Button(onClick = { showTimesFullDialog = false }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
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
            Button(onClick = {
                if (scrambles.value.isNotEmpty()) {
                    currentScramble = scrambles.value.random()
                }
            }) {
                Text(text = stringResource(R.string.button_new_scramble))
            }
            Spacer(modifier = Modifier.height(16.dp)) // Add some space between the button and "Scramble"
            Text(
                text = stringResource(R.string.timer_scramble_label),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            AnimatedContent(
                targetState = currentScramble,
                transitionSpec = {
                    slideInVertically(
                        animationSpec = tween(500),
                        initialOffsetY = { fullHeight -> -fullHeight }
                    ) togetherWith slideOutVertically(
                        animationSpec = tween(500),
                        targetOffsetY = { fullHeight -> fullHeight }
                    )
                }, label = ""
            ) { scramble ->
                Text(
                    text = formatScramble(scramble),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2, // Allow text to wrap to 2 lines
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis // Add ellipsis if it overflows
                )
            }
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
                    Text(text = if (isActive) stringResource(R.string.button_stop) else stringResource(R.string.button_start))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = { showResetDialog = true }) {
                    Text(text = stringResource(R.string.button_reset))
                }
                Button(onClick = {
                    if (savedTimes.value.size >= 5) {
                        showTimesFullDialog = true
                    } else {
                        showSaveDialog = true
                    }
                }) {
                    Text(text = stringResource(R.string.button_save))
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
                    Text(stringResource(R.string.timer_stats_best), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(bestTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.timer_stats_worst), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(worstTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.timer_stats_average), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(averageTime), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.timer_stats_average_3_of_5), color = Color.White, fontWeight = FontWeight.Bold)
                    Text(formatTime(average3of5), color = Color.White)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.timer_saved_times), color = Color.White, fontWeight = FontWeight.Bold)
                    LazyColumn {
                        items(savedTimes.value, key = { it }) { time ->
                            AnimatedVisibility(
                                visible = true, // Always visible once added, animations handle entry/exit
                                enter = expandVertically(animationSpec = tween(500)),
                                exit = shrinkVertically(animationSpec = tween(500))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(formatTime(time), color = Color.White)
                                    IconButton(onClick = {
                                        timeToDelete = time
                                        showDeleteConfirmationDialog = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cd_remove_time),
                                            tint = Color.White
                                        )
                                    }
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
            Text(text = stringResource(R.string.button_back))
        }
    }
}

private fun formatScramble(scramble: String): String {
    val moves = scramble.split(" ")
    if (moves.size > 10) {
        val line1 = moves.subList(0, 10).joinToString(" ")
        val line2 = moves.subList(10, moves.size).joinToString(" ")
        return "$line1\n$line2"
    }
    return scramble
}

private fun formatTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = (timeMillis % 1000) / 10
    return "%02d:%02d:%02d".format(minutes, seconds, milliseconds)
}
