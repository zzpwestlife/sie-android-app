package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientProgressBar - Progress indicator
 *
 * Design: Gradient fill with percentage label
 * Usage: Study progress, exam timer
 *
 * @param progress Progress value (0.0 to 1.0)
 * @param modifier Modifier for the progress bar
 * @param gradient Progress fill gradient (default: AccentGradient)
 * @param showLabel Show percentage label above bar
 */
@Composable
fun ModernGradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    gradient: Brush = AccentGradient,
    showLabel: Boolean = true
) {
    Column(modifier = modifier) {
        if (showLabel) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = OnBackground,
                modifier = Modifier.padding(bottom = SpacingSmall)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE0E0E0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(gradient)
            )
        }
    }
}
