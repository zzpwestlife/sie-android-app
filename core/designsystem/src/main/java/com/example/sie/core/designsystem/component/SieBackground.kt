package com.example.sie.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The main background for the app.
 * Uses [MaterialTheme.colorScheme.background] by default.
 *
 * @param modifier Modifier to be applied to the background.
 * @param content The content to render inside the background.
 */
@Composable
fun SieBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxSize(),
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}

/**
 * A gradient background for the app.
 *
 * @param modifier Modifier to be applied to the background.
 * @param topColor The top gradient color.
 * @param bottomColor The bottom gradient color.
 * @param content The content to render inside the background.
 */
@Composable
fun SieGradientBackground(
    modifier: Modifier = Modifier,
    topColor: Color = MaterialTheme.colorScheme.primaryContainer,
    bottomColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable () -> Unit
) {
    Surface(
        color = bottomColor,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxSize(),
    ) {
        // Implementation of gradient could be added here if needed,
        // for now just using surface with bottom color
         CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}
