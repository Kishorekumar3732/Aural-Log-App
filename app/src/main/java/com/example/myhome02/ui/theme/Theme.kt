package com.example.myhome02.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myhome02.R

// Custom font families
private val AdventurerFontFamily = FontFamily(
    Font(R.font.adventurerdemoregular)  // Custom font for "Aural Log"
)

private val AnirmFontFamily = FontFamily(
    Font(R.font.anirm)  // Custom font for "Welcome, user"
)

// Custom typography
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = AdventurerFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AnirmFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    )
)

// Define color schemes for light and dark modes
val LightColorPalette = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    background = Color.Transparent, // Make the background transparent
    surface = Color.Transparent,    // Make surfaces (e.g., cards) transparent
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun MyhomeTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorPalette // Always use light theme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
