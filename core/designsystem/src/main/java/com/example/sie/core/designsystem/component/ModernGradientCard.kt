package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientCard - Primary container component
 *
 * Design: Pure white background with left gradient accent strip
 * Usage: 80% of UI components
 *
 * @param modifier Modifier for the card
 * @param gradient Left accent gradient (default: PrimaryGradient)
 * @param onClick Optional click handler (makes card clickable)
 * @param content Card content (ColumnScope)
 */
@Composable
fun ModernGradientCard(
    modifier: Modifier = Modifier,
    gradient: Brush = PrimaryGradient,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = Color.White // Pure white for readability
        ),
        shape = RoundedCornerShape(CornerRadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationLow
        )
    ) {
        Row {
            // Left gradient accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(gradient)
            )
            // Content area
            Column(
                modifier = Modifier.padding(
                    start = SpacingMedium,
                    top = SpacingMedium,
                    end = SpacingMedium,
                    bottom = SpacingMedium
                )
            ) {
                content()
            }
        }
    }
}
