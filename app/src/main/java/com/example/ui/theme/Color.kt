package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Atmospheric Nordic / Caucasus Weather Atlas Palette
val NightSkyDeep = Color(0xFF0A1624)
val NightSkySurface = Color(0xFF122337)
val NightSkySurfaceHigh = Color(0xFF1A314B)
val NightSkyOutline = Color(0xFF2B4666)

val SkyCyan = Color(0xFF38BDF8)
val SkyCyanContainer = Color(0xFF0C4A6E)
val SunAmber = Color(0xFFFBBF24)
val SunAmberContainer = Color(0xFF452A06)
val MistTeal = Color(0xFF2DD4BF)

val DaySkyBg = Color(0xFFF0F6FC)
val DaySkySurface = Color(0xFFFFFFFF)
val DaySkySurfaceVariant = Color(0xFFE1ECF7)
val DaySkyOutline = Color(0xFFC2D6EA)
val DayPrimaryBlue = Color(0xFF0284C7)
val DayPrimaryContainer = Color(0xFFD0ECFD)
val DaySecondaryAmber = Color(0xFFD97706)
val DayTextPrimary = Color(0xFF0F1E2E)
val DayTextSecondary = Color(0xFF47607A)

// Temperature Spectrum Colors
val TempFreezing = Color(0xFF60A5FA)
val TempCool = Color(0xFF2DD4BF)
val TempMild = Color(0xFFFBBF24)
val TempWarm = Color(0xFFFB923C)
val TempHot = Color(0xFFF87171)

fun temperatureColor(tempCelsius: Double): Color =
  when {
    tempCelsius <= 2.0 -> TempFreezing
    tempCelsius <= 12.0 -> TempCool
    tempCelsius <= 21.0 -> TempMild
    tempCelsius <= 28.0 -> TempWarm
    else -> TempHot
  }
