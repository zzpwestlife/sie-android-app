package com.example.sie.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Create a new Typography with font sizes scaled by the given factor.
 * @param scale Font size scale (-2 to +2)
 *   -2 = 80% size
 *   -1 = 90% size
 *    0 = 100% size (default)
 *   +1 = 110% size
 *   +2 = 120% size
 */
fun Typography.withFontScale(scale: Int): Typography {
    val scaleFactor = when (scale) {
        -2 -> 0.8f
        -1 -> 0.9f
        0 -> 1.0f
        1 -> 1.1f
        2 -> 1.2f
        else -> 1.0f
    }

    return this.copy(
        displayLarge = this.displayLarge.copy(fontSize = this.displayLarge.fontSize * scaleFactor),
        displayMedium = this.displayMedium.copy(fontSize = this.displayMedium.fontSize * scaleFactor),
        displaySmall = this.displaySmall.copy(fontSize = this.displaySmall.fontSize * scaleFactor),
        headlineLarge = this.headlineLarge.copy(fontSize = this.headlineLarge.fontSize * scaleFactor),
        headlineMedium = this.headlineMedium.copy(fontSize = this.headlineMedium.fontSize * scaleFactor),
        headlineSmall = this.headlineSmall.copy(fontSize = this.headlineSmall.fontSize * scaleFactor),
        titleLarge = this.titleLarge.copy(fontSize = this.titleLarge.fontSize * scaleFactor),
        titleMedium = this.titleMedium.copy(fontSize = this.titleMedium.fontSize * scaleFactor),
        titleSmall = this.titleSmall.copy(fontSize = this.titleSmall.fontSize * scaleFactor),
        bodyLarge = this.bodyLarge.copy(fontSize = this.bodyLarge.fontSize * scaleFactor),
        bodyMedium = this.bodyMedium.copy(fontSize = this.bodyMedium.fontSize * scaleFactor),
        bodySmall = this.bodySmall.copy(fontSize = this.bodySmall.fontSize * scaleFactor),
        labelLarge = this.labelLarge.copy(fontSize = this.labelLarge.fontSize * scaleFactor),
        labelMedium = this.labelMedium.copy(fontSize = this.labelMedium.fontSize * scaleFactor),
        labelSmall = this.labelSmall.copy(fontSize = this.labelSmall.fontSize * scaleFactor),
    )
}
