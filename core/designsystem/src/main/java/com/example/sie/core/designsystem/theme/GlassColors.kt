package com.example.sie.core.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Glass Surface Colors
val GlassSurface = Color.White.copy(alpha = 0.15f)
val GlassBorder = Color.White.copy(alpha = 0.3f)
val GlassShadow = Color.Black.copy(alpha = 0.1f)

// Gradient Brushes
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
)

val SecondaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFf093fb), Color(0xFFf5576c))
)

val TertiaryGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4facfe), Color(0xFF00f2fe))
)

val AccentGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFfa709a), Color(0xFFfee140))
)

// Success/Error Gradients
val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
)

val ErrorGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF44336), Color(0xFFEF5350))
)
