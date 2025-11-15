package com.cs407.cubemaster.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cs407.cubemaster.R
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.ui.theme.CubemasterTheme

@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    permissionGranted: Boolean,
    showPermissionDeniedMessage: Boolean,
    requestPermission: () -> Unit,
    openSettings: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.permission_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    permissionGranted -> stringResource(R.string.permission_granted)
                    showPermissionDeniedMessage -> stringResource(R.string.permission_denied_message)
                    else -> stringResource(R.string.permission_request_message)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            if (showPermissionDeniedMessage) {
                Button(onClick = openSettings) {
                    Text(text = stringResource(R.string.button_open_settings))
                }
            } else {
                Button(onClick = requestPermission, enabled = !permissionGranted) {
                    Text(text = stringResource(R.string.button_grant_permission))
                }
            }
        }
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text(text = stringResource(R.string.button_back))
        }
        if (permissionGranted) {
            Button(
                onClick = {
                    navController.navigate("scan") {
                        popUpTo("permission") { inclusive = true }
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Text(text = stringResource(R.string.button_next))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    CubemasterTheme {
        PermissionScreen(
            navController = rememberNavController(),
            permissionGranted = false,
            showPermissionDeniedMessage = false,
            requestPermission = {},
            openSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenGrantedPreview() {
    CubemasterTheme {
        PermissionScreen(
            navController = rememberNavController(),
            permissionGranted = true,
            showPermissionDeniedMessage = false,
            requestPermission = {},
            openSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenDeniedPreview() {
    CubemasterTheme {
        PermissionScreen(
            navController = rememberNavController(),
            permissionGranted = false,
            showPermissionDeniedMessage = true,
            requestPermission = {},
            openSettings = {}
        )
    }
}
