package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.example.sie.core.designsystem.theme.*

/**
 * AppBackground - Screen background layer
 *
 * Design: Subtle top gradient fading to solid color
 * Usage: Wrap all screen content
 *
 * @param modifier Modifier for the background
 * @param content Screen content
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppBackgroundGradientTop, // Light cyan (top)
                        AppBackground             // Light gray-white (bottom)
                    ),
                    startY = 0f,
                    endY = 800f // Gradient only in top 200dp
                )
            )
    ) {
        content()
    }
}
