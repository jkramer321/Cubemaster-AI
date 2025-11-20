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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cs407.cubemaster.R
import com.cs407.cubemaster.ui.theme.DarkOrange
import com.cs407.cubemaster.ui.theme.LightOrange
import coil.compose.AsyncImage
import com.cs407.cubemaster.ui.theme.MediumOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE) }

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
                    .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .background(
                        color = MediumOrange, // Changed to MediumOrange
                        shape = MaterialTheme.shapes.medium
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Profile Picture Placeholder
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") } // Make it clickable
                            .background(Color.Gray)
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Profile Picture",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Use a default image if no image is selected
                            Image(
                                painter = painterResource(id = R.drawable.rubik_icon),
                                contentDescription = "Default Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Name and Quote
                    Column {
                        val userNameState = remember { mutableStateOf(prefs.getString("user_name", "Your Name") ?: "Your Name") }
                        OutlinedTextField(
                            value = userNameState.value,
                            onValueChange = { newValue ->
                                userNameState.value = newValue
                                prefs.edit().putString("user_name", newValue).apply()
                            },
                            label = { Text("Your Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dropdown for Favorite Quote
                        val allQuotes = listOf(
                            "The only way to do great work is to love what you do.",
                            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                            "The best way to predict the future is to create it.",
                            "Hard work beats talent when talent doesn't work hard."
                        )
                        // For now, assume all are unlocked.
                        val unlockedQuotes = allQuotes

                        var expanded by remember { mutableStateOf(false) }
                        val selectedQuoteState = remember { mutableStateOf(prefs.getString("selected_quote", allQuotes.firstOrNull() ?: "") ?: "") }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = selectedQuoteState.value,
                                onValueChange = {}, // Read-only
                                readOnly = true,
                                label = { Text("Favorite Quote") },
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Dropdown Arrow"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                unlockedQuotes.forEach { quote ->
                                    DropdownMenuItem(
                                        text = { Text(quote) },
                                        onClick = {
                                            selectedQuoteState.value = quote
                                            prefs.edit().putString("selected_quote", quote).apply()
                                            expanded = false
                                        }
                                    )
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
            Text("Back")
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}
