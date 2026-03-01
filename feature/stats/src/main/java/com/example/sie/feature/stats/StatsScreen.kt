package com.example.sie.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.AccentGradient
import com.example.sie.core.designsystem.theme.OnBackground
import com.example.sie.core.designsystem.theme.OnBackgroundSecondary
import com.example.sie.core.designsystem.theme.OnSurface
import com.example.sie.core.designsystem.theme.SecondaryGradient
import com.example.sie.core.designsystem.theme.SpacingMedium
import com.example.sie.core.designsystem.theme.SpacingSmall
import com.example.sie.core.designsystem.theme.TertiaryGradient
import com.example.sie.core.model.ExamResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsRoute(
    onExamHistoryClick: () -> Unit = {},
    onExamResultClick: (Int) -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    StatsScreen(
        uiState = uiState,
        onExamHistoryClick = onExamHistoryClick,
        onExamResultClick = onExamResultClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatsScreen(
    uiState: StatsUiState,
    onExamHistoryClick: () -> Unit = {},
    onExamResultClick: (Int) -> Unit = {}
) {
    AppBackground {
        Scaffold(
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.stats_title),
                    gradient = TertiaryGradient
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            when (uiState) {
                StatsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                StatsUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(CommonR.string.stats_empty),
                            color = OnBackgroundSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is StatsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = SpacingMedium),
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium),
                        contentPadding = PaddingValues(vertical = SpacingMedium)
                    ) {
                        // Average Score Overview Card
                        item {
                            ModernGradientCard(
                                gradient = SecondaryGradient,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(CommonR.string.stats_average_score),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(SpacingMedium))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ScoreChart(score = uiState.averageScore)
                                }
                            }
                        }

                        // View All History Button
                        item {
                            ModernGradientCard(
                                gradient = TertiaryGradient,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onExamHistoryClick
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(CommonR.string.exam_history_view_all),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = OnBackgroundSecondary
                                    )
                                }
                            }
                        }

                        // Recent Results Section Header
                        item {
                            Text(
                                text = stringResource(CommonR.string.stats_recent_results),
                                style = MaterialTheme.typography.titleMedium,
                                color = OnBackground,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = SpacingSmall)
                            )
                        }

                        // Recent Results List
                        items(uiState.recentResults) { result ->
                            ModernGradientCard(
                                gradient = TertiaryGradient,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onExamResultClick(result.id) }
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
        Canvas(modifier = Modifier.size(140.dp)) {
            // Background circle (light gray)
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )

            // Progress arc with AccentGradient colors
            drawArc(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00C9FF), // Ice blue
                        Color(0xFF92FE9D)  // Mint green
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                startAngle = -90f,
                sweepAngle = (score / 100) * 360f,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${score.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Average Score",
                style = MaterialTheme.typography.bodySmall,
                color = OnBackgroundSecondary
            )
        }
    }
}

@Composable
fun ExamResultItemContent(result: ExamResult) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(CommonR.string.stats_score_label, result.score),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(SpacingSmall))
            Text(
                text = dateFormat.format(Date(result.date)),
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundSecondary
            )
            Text(
                text = "${result.correctCount}/${result.totalQuestions} questions",
                style = MaterialTheme.typography.bodySmall,
                color = OnBackgroundSecondary
            )
        }

        // Score Badge
        Box(
            modifier = Modifier
                .size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                drawCircle(
                    color = Color(0xFFF0F4F8),
                    radius = size.minDimension / 2
                )
            }
            Text(
                text = "${result.score}%",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
