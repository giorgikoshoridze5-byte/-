package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val NotoGeorgianFontFamily = FontFamily(
  Font(R.font.noto_sans_georgian, FontWeight.Normal),
  Font(R.font.noto_sans_georgian, FontWeight.Medium),
  Font(R.font.noto_sans_georgian, FontWeight.SemiBold),
  Font(R.font.noto_sans_georgian, FontWeight.Bold),
)

val OutfitDisplayFontFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Normal),
  Font(R.font.outfit, FontWeight.Medium),
  Font(R.font.outfit, FontWeight.SemiBold),
  Font(R.font.outfit, FontWeight.Bold),
)

val Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = OutfitDisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 64.sp,
        lineHeight = 70.sp,
        letterSpacing = (-1.5).sp,
      ),
    displayMedium =
      TextStyle(
        fontFamily = OutfitDisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
      ),
    headlineSmall =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = NotoGeorgianFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
      ),
  )
