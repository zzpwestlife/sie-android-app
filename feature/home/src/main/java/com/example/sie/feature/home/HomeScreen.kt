package com.example.sie.feature.home

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
    onSettingsClick: () -> Unit,
) {
    HomeScreen(
        onTopicSelectionClick = onTopicSelectionClick,
        onMockExamClick = onMockExamClick,
        onStatsClick = onStatsClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
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

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = PrimaryGradient,
                    onClick = onTopicSelectionClick
                ) {
                    Text(
                        text = "🎯 Start Practice",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Study questions by topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = SecondaryGradient,
                    onClick = onMockExamClick
                ) {
                    Text(
                        text = "📝 Mock Exam",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Take a full practice test",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = TertiaryGradient,
                    onClick = onStatsClick
                ) {
                    Text(
                        text = "📊 Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View your performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = AccentGradient,
                    onClick = onSettingsClick
                ) {
                    Text(
                        text = "⚙️ Settings",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure app preferences",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
