package com.cs407.cubemaster.ui.theme




import androidx.compose.material3.MaterialTheme



import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable




@Composable
fun CubemasterTheme(

    // Dynamic color is available on Android 12+


    content: @Composable () -> Unit
) {
    val colorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40,
        background = DarkOrange,
        surface = DarkOrange
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}