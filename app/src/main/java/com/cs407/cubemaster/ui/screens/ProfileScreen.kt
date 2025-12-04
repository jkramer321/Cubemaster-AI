package com.cs407.cubemaster.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.cs407.cubemaster.R
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import com.cs407.cubemaster.ui.theme.MediumOrange

data class Achievement(
    val name: String,
    val description: String,
    val unlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }

    val achievements = listOf(
        Achievement(
            name = stringResource(R.string.achievement_speed_solver),
            description = stringResource(R.string.achievement_speed_solver_desc),
            unlocked = prefs.getBoolean("speed_solver_unlocked", false)
        ),
        Achievement(
            name = stringResource(R.string.achievement_scramble_master),
            description = stringResource(R.string.achievement_scramble_master_desc),
            unlocked = prefs.getBoolean("scramble_master_unlocked", false)
        ),
        Achievement(
            name = stringResource(R.string.achievement_world_record),
            description = stringResource(R.string.achievement_world_record_desc),
            unlocked = prefs.getBoolean("average_viewer_unlocked", false)
        )
    )

    var selectedImageUri by remember {
        val savedUri = prefs.getString("profile_image_uri", null)?.let { Uri.parse(it) }
        mutableStateOf(savedUri)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            prefs.edit().putString("profile_image_uri", it.toString()).apply()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) { // Outer Column that fills the screen
        // Main Content Area (including profile box)
        Column(
            modifier = Modifier
                .weight(1f) // Takes all available vertical space except for the back button
                .fillMaxWidth()
        ) {
            // Profile Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Make this box fill all available space in this Column
                    .padding(top = 32.dp, start = 32.dp, end = 32.dp, bottom = 24.dp) // Increased padding
                    .background(
                        color = MediumOrange, // Changed to MediumOrange
                        shape = MaterialTheme.shapes.medium
                    ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize() // This column will manage the space inside the orange box
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), // Existing Row
                        verticalAlignment = Alignment.Top
                    ) {
                        // Profile Picture Placeholder
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Made smaller
                                .clip(CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") } // Make it clickable
                                .background(Color.Gray)
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = stringResource(R.string.cd_profile_picture),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Use a default image if no image is selected
                                Image(
                                    painter = painterResource(id = R.drawable.rubik_icon),
                                    contentDescription = stringResource(R.string.cd_default_profile),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp)) // Increased spacer width

                        // Name and Quote
                        Column {
                            val defaultName = stringResource(R.string.default_user_name)
                            val userNameState = remember { mutableStateOf(prefs.getString("user_name", defaultName) ?: defaultName) }
                            OutlinedTextField(
                                value = userNameState.value,
                                onValueChange = { newValue ->
                                    userNameState.value = newValue
                                    prefs.edit().putString("user_name", newValue).apply()
                                },
                                label = { Text(stringResource(R.string.profile_name_label)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Increased spacer height

                            // Dropdown for Favorite Quote
                            val unlockedQuotes = achievements.filter { it.unlocked }.map { it.name to it.name }

                            var expanded by remember { mutableStateOf(false) }
                            val selectedQuoteState = remember { mutableStateOf(prefs.getString("selected_quote", "") ?: "") }

                            // If the currently selected quote is from an achievement that is now locked, reset it.
                            if (selectedQuoteState.value.isNotEmpty() && unlockedQuotes.none { it.second == selectedQuoteState.value }) {
                                selectedQuoteState.value = ""
                                prefs.edit().putString("selected_quote", "").apply()
                            }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                TextField(
                                    value = selectedQuoteState.value,
                                    onValueChange = {}, // Read-only
                                    readOnly = true,
                                    label = { Text(stringResource(R.string.profile_quote_label)) },
                                    trailingIcon = {
                                        IconButton(onClick = { expanded = !expanded }) {
                                            Icon(
                                                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                                contentDescription = stringResource(R.string.cd_dropdown_arrow)
                                            )
                                        }
                                    },
                                    modifier = Modifier.menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    if (unlockedQuotes.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.achievement_no_unlocked_message)) },
                                            onClick = { expanded = false },
                                            enabled = false
                                        )
                                    } else {
                                        unlockedQuotes.forEach { (label, quoteValue) ->
                                            DropdownMenuItem(
                                                text = { Text(label) },
                                                onClick = {
                                                    selectedQuoteState.value = quoteValue
                                                    prefs.edit().putString("selected_quote", quoteValue).apply()
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // New Rectangle for Achievements
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Takes remaining vertical space
                            .padding(16.dp) // Add some padding inside this box
                            .background(
                                color = DarkOrange, // Darker color
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.profile_achievements),
                                color = Color.White, // Ensure text is visible on dark background
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Achievements List
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                achievements.forEach { achievement ->
                                    AchievementItem(achievement = achievement)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Back button (outside the weighted column, so it's always at the bottom)
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth() // Fill width so it doesn't look weird when content above fills width
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.button_back))
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    val alpha = if (achievement.unlocked) 1f else 0.5f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .background(
                color = LightOrange,
                shape = MaterialTheme.shapes.small
            )
            .padding(8.dp)
    ) {
        val context = LocalContext.current
        Icon(
            painter = painterResource(id = R.drawable.rubik_icon),
            contentDescription = if (achievement.unlocked)
                context.getString(R.string.achievement_unlocked_state)
            else
                context.getString(R.string.achievement_locked_state),
            modifier = Modifier.size(32.dp),
            tint = if (achievement.unlocked) Color.Unspecified else Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = achievement.name,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = achievement.description,
                color = Color.DarkGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}