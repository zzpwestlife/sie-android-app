package com.example.sie.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GradientProgressIndicator(
    progress: Float,
    gradient: Brush,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.2f),
    animate: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) progress else progress,
        animationSpec = tween(durationMillis = 700),
        label = "progress_animation"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val cornerRadius = CornerRadius(height / 2, height / 2)

        // Background track
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset.Zero,
            size = Size(width, height),
            cornerRadius = cornerRadius
        )

        // Progress fill
        if (animatedProgress > 0f) {
            drawRoundRect(
                brush = gradient,
                topLeft = Offset.Zero,
                size = Size(width * animatedProgress.coerceIn(0f, 1f), height),
                cornerRadius = cornerRadius
            )
        }
    }
}
