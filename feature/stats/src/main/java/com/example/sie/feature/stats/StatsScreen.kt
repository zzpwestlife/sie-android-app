package com.example.sie.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
import com.example.sie.core.model.ExamResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    StatsScreen(uiState)
}

@Composable
internal fun StatsScreen(uiState: StatsUiState) {
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
            ),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            StatsUiState.Loading -> CircularProgressIndicator(color = Color.White)
            StatsUiState.Empty -> Text(
                text = "No exam results yet.",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            is StatsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Average Score",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = PrimaryGradient
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ScoreChart(score = uiState.averageScore)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Recent Results",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color.White
                    )

                    LazyColumn {
                        items(uiState.recentResults) { result ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                gradient = SecondaryGradient
                            ) {
                                ExamResultItemContent(result)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreChart(score: Float) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f)
            )
            drawArc(
                color = if (score >= 70) Color(0xFF4CAF50) else Color(0xFFF44336),
                startAngle = -90f,
                sweepAngle = (score / 100) * 360f,
                useCenter = false,
                style = Stroke(width = 20f)
            )
        }
        Text(
            text = "${score.toInt()}%",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}

@Composable
fun ExamResultItemContent(result: ExamResult) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Score: ${result.score}%",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateFormat.format(Date(result.date)),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = "${result.correctCount}/${result.totalQuestions}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}
