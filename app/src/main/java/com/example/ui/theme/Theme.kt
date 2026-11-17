package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = SkyCyan,
    onPrimary = Color(0xFF042238),
    primaryContainer = SkyCyanContainer,
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = SunAmber,
    onSecondary = Color(0xFF332000),
    secondaryContainer = SunAmberContainer,
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = MistTeal,
    onTertiary = Color(0xFF032D27),
    background = NightSkyDeep,
    onBackground = Color(0xFFF1F5F9),
    surface = NightSkySurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = NightSkySurfaceHigh,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = NightSkyOutline,
    outlineVariant = Color(0xFF1E354F),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DayPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = DayPrimaryContainer,
    onPrimaryContainer = Color(0xFF082F49),
    secondary = DaySecondaryAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF451A03),
    tertiary = Color(0xFF0D9488),
    onTertiary = Color.White,
    background = DaySkyBg,
    onBackground = DayTextPrimary,
    surface = DaySkySurface,
    onSurface = DayTextPrimary,
    surfaceVariant = DaySkySurfaceVariant,
    onSurfaceVariant = DayTextSecondary,
    outline = DaySkyOutline,
    outlineVariant = Color(0xFFDCE8F5),
  )

private val AppShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
  )

@Composable
fun AmindiTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
