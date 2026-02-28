package com.example.sie.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            StatsUiState.Loading -> CircularProgressIndicator()
            StatsUiState.Empty -> Text(text = "No exam results yet.")
            is StatsUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = "Average Score",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ScoreChart(score = uiState.averageScore)
                    }

                    Text(
                        text = "Recent Results",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    LazyColumn {
                        items(uiState.recentResults) { result ->
                            ExamResultItem(result)
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
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20f)
            )
            drawArc(
                color = if (score >= 70) Color.Green else Color.Red,
                startAngle = -90f,
                sweepAngle = (score / 100) * 360f,
                useCenter = false,
                style = Stroke(width = 20f)
            )
        }
        Text(
            text = "${score.toInt()}%",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ExamResultItem(result: ExamResult) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    ListItem(
        headlineContent = { Text("Score: ${result.score}%") },
        supportingContent = { Text(dateFormat.format(Date(result.date))) },
        trailingContent = { 
            Text(
                text = "${result.correctCount}/${result.totalQuestions}",
                style = MaterialTheme.typography.bodySmall
            ) 
        }
    )
}
