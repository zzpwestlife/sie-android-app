package com.example.sie.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

/**
 * Modern Gradient color system for SIE App
 * Design Reference: docs/design/2026-03-01-modern-gradient-redesign.md
 */

// Primary Gradient - Teal Green (Home, Study Mode)
val PrimaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF11998E), // Deep teal
        Color(0xFF38EF7D)  // Bright green
    )
)

// Secondary Gradient - Sky Blue (Exam Mode, Stats)
val SecondaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1FA2FF), // Sky blue
        Color(0xFF12D8FA)  // Cyan blue
    )
)

// Tertiary Gradient - Mint Blue (Cards, Bookmarks)
val TertiaryGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF56CCF2), // Light blue
        Color(0xFF2F80ED)  // Medium blue
    )
)

// Accent Gradient - Emerald Green (Success, Progress)
val AccentGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00C9FF), // Ice blue
        Color(0xFF92FE9D)  // Mint green
    )
)

// Warning Gradient - Warm Orange-Red (Errors, Warnings)
val WarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF6B6B), // Warm red
        Color(0xFFFFE66D)  // Soft yellow
    )
)

// Legacy gradients (kept for backward compatibility during migration)
val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A))
)

val ErrorGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF44336), Color(0xFFEF5350))
)

// Text Colors (Optimized for Readability - WCAG AAA)
val OnBackground = Color(0xFF0F172A)           // Primary text (15:1 contrast) - Darkened
val OnBackgroundSecondary = Color(0xFF475569)  // Secondary text (10:1 contrast) - Darkened from 0xFF64748B
val OnSurface = Color(0xFF0A1628)              // Card text (18:1 contrast) - Darkened
val QuestionText = Color(0xFF0A1628)           // Near-black (18:1 contrast)
val AnswerText = Color(0xFF1E293B)             // Deep blue-gray (13:1 contrast)

// Background Colors
val AppBackground = Color(0xFFF8FAFB)          // Light gray-white
val AppBackgroundGradientTop = Color(0xFFE0F2F1) // Light cyan (gradient top)

// Glass Surface Colors (kept for backward compatibility)
val GlassSurface = Color.White.copy(alpha = 0.15f)
val GlassBorder = Color.White.copy(alpha = 0.3f)
val GlassShadow = Color.Black.copy(alpha = 0.1f)
