package com.example.sie.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.SieButton
import com.example.sie.core.designsystem.component.SieTopAppBar
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.*

@Composable
fun HomeRoute(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    HomeScreen(
        onTopicSelectionClick = onTopicSelectionClick,
        onMockExamClick = onMockExamClick,
        onStatsClick = onStatsClick,
        onBookmarkedClick = onBookmarkedClick,
        onWrongQuestionsClick = onWrongQuestionsClick,
        onFlashcardsClick = onFlashcardsClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var cardsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cardsVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SieTopAppBar(
                    title = stringResource(CommonR.string.home_title),
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(CommonR.string.home_welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 0,
                    gradient = PrimaryGradient,
                    onClick = onTopicSelectionClick,
                    title = stringResource(CommonR.string.home_start_practice),
                    subtitle = stringResource(CommonR.string.home_start_practice_subtitle)
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 100,
                    gradient = SecondaryGradient,
                    onClick = onMockExamClick,
                    title = stringResource(CommonR.string.home_mock_exam),
                    subtitle = stringResource(CommonR.string.home_mock_exam_subtitle)
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 200,
                    gradient = TertiaryGradient,
                    onClick = onStatsClick,
                    title = stringResource(CommonR.string.home_statistics),
                    subtitle = stringResource(CommonR.string.home_statistics_subtitle)
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 300,
                    gradient = AccentGradient,
                    onClick = onBookmarkedClick,
                    title = stringResource(CommonR.string.home_bookmarked),
                    subtitle = stringResource(CommonR.string.home_bookmarked_subtitle)
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 400,
                    gradient = ErrorGradient,
                    onClick = onWrongQuestionsClick,
                    title = stringResource(CommonR.string.home_wrong_questions),
                    subtitle = stringResource(CommonR.string.home_wrong_questions_subtitle)
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 500,
                    gradient = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0984e3),
                            Color(0xFF74b9ff)
                        )
                    ),
                    onClick = onFlashcardsClick,
                    title = stringResource(CommonR.string.home_flashcards),
                    subtitle = stringResource(CommonR.string.home_flashcards_subtitle),
                    enabled = false
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 600,
                    gradient = SuccessGradient,
                    onClick = onSettingsClick,
                    title = stringResource(CommonR.string.home_settings),
                    subtitle = stringResource(CommonR.string.home_settings_subtitle)
                )
            }
        }
    }
}

@Composable
private fun AnimatedGlassCard(
    visible: Boolean,
    delay: Int,
    gradient: Brush,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
    enabled: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 500, delayMillis = delay)
        ) + slideInVertically(
            animationSpec = tween(durationMillis = 500, delayMillis = delay),
            initialOffsetY = { it / 4 }
        )
    ) {
        val displayGradient = if (enabled) gradient else Brush.linearGradient(
            colors = listOf(
                Color.Gray,
                Color.LightGray
            )
        )
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = displayGradient,
            onClick = if (enabled) onClick else null
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
