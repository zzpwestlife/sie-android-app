package com.example.sie.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.example.sie.core.designsystem.theme.*

/**
 * ModernGradientTopAppBar - Screen header
 *
 * Design: Gradient background with white text + shadow
 * Usage: All screen headers
 *
 * @param title Screen title
 * @param modifier Modifier for the app bar
 * @param gradient Background gradient (default: PrimaryGradient)
 * @param onNavigationClick Back button click handler (null = no back button)
 * @param actions Top app bar actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernGradientTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    gradient: Brush = PrimaryGradient,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.3f),
                        offset = Offset(0f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier.background(gradient)
    )
}
