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

                SieButton(
                    onClick = onTopicSelectionClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Practice")
                }

                SieButton(
                    onClick = onMockExamClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mock Exam")
                }

                SieButton(
                    onClick = onStatsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Statistics")
                }

                SieButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings")
                }
            }
        }
    }
}
