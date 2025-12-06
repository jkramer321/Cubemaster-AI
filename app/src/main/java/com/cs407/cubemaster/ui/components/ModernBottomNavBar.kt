package com.cs407.cubemaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs407.cubemaster.R

@Composable
fun ModernBottomNavBar(navController: NavController, currentScreen: String, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.primary)
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