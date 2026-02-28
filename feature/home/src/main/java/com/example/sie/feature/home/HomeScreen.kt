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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
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
    onSettingsClick: () -> Unit,
) {
    HomeScreen(
        onTopicSelectionClick = onTopicSelectionClick,
        onMockExamClick = onMockExamClick,
        onStatsClick = onStatsClick,
        onBookmarkedClick = onBookmarkedClick,
        onWrongQuestionsClick = onWrongQuestionsClick,
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
                    title = "Dashboard",
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
                    text = "Welcome to Entry Test Prep",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 0,
                    gradient = PrimaryGradient,
                    onClick = onTopicSelectionClick,
                    title = "🎯 Start Practice",
                    subtitle = "Study questions by topic"
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 100,
                    gradient = SecondaryGradient,
                    onClick = onMockExamClick,
                    title = "📝 Mock Exam",
                    subtitle = "Take a full practice test"
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 200,
                    gradient = TertiaryGradient,
                    onClick = onStatsClick,
                    title = "📊 Statistics",
                    subtitle = "View your performance"
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 300,
                    gradient = AccentGradient,
                    onClick = onBookmarkedClick,
                    title = "⭐ Bookmarked",
                    subtitle = "Review your saved questions"
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 400,
                    gradient = ErrorGradient,
                    onClick = onWrongQuestionsClick,
                    title = "❌ Wrong Questions",
                    subtitle = "Practice questions you got wrong"
                )

                AnimatedGlassCard(
                    visible = cardsVisible,
                    delay = 500,
                    gradient = SuccessGradient,
                    onClick = onSettingsClick,
                    title = "⚙️ Settings",
                    subtitle = "Configure app preferences"
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
    subtitle: String
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
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = gradient,
            onClick = onClick
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
