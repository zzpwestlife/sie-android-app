package com.example.sie.feature.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientButton
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientProgressBar
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.component.QuestionCard
import com.example.sie.core.designsystem.theme.AccentGradient
import com.example.sie.core.designsystem.theme.OnBackground
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SpacingMedium
import com.example.sie.core.designsystem.theme.SpacingSmall
import com.example.sie.core.designsystem.theme.WarningGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun StudyRoute(
    onBackClick: () -> Unit,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    
    StudyScreen(
        uiState = uiState,
        onOptionSelected = viewModel::selectOption,
        onNextQuestion = viewModel::loadNextQuestion,
        onPreviousQuestion = viewModel::loadPreviousQuestion,
        onToggleBookmark = viewModel::toggleBookmark,
        onBackClick = onBackClick,
        language = language
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StudyScreen(
    uiState: StudyUiState,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onBackClick: () -> Unit,
    language: String
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Pre-fetch strings for use in coroutine scope
    val bookmarkRemovedText = stringResource(CommonR.string.study_bookmark_removed)
    val bookmarkedText = stringResource(CommonR.string.study_bookmarked)

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.study_title),
                    gradient = PrimaryGradient,
                    onNavigationClick = onBackClick,
                    actions = {
                        if (uiState is StudyUiState.Success) {
                            val isBookmarked = uiState.currentQuestion.isBookmarked
                            IconButton(
                                onClick = {
                                    onToggleBookmark(uiState.currentQuestion.id)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            if (isBookmarked) bookmarkRemovedText
                                            else bookmarkedText
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Star
                                        else Icons.Outlined.Star,
                                    contentDescription = if (isBookmarked) stringResource(CommonR.string.common_remove_bookmark)
                                        else stringResource(CommonR.string.common_add_bookmark),
                                    tint = if (isBookmarked) Color(0xFFfee140)
                                        else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                when (uiState) {
                    is StudyUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is StudyUiState.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onBackClick) {
                                Text(stringResource(CommonR.string.common_go_back))
                            }
                        }
                    }
                    is StudyUiState.Success -> {
                        StudyContent(
                            state = uiState,
                            onOptionSelected = onOptionSelected,
                            onNextQuestion = onNextQuestion,
                            onPreviousQuestion = onPreviousQuestion,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyContent(
    state: StudyUiState.Success,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    language: String
) {
    var elapsedTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(state.stats.startTime) {
        while (isActive) {
            elapsedTime = System.currentTimeMillis() - state.stats.startTime
            delay(1000)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        // Progress Bar at Top
        val progress = if (state.stats.totalAnswered > 0) {
            state.stats.correctCount.toFloat() / state.stats.totalAnswered
        } else 0f
        ModernGradientProgressBar(
            progress = progress,
            gradient = AccentGradient,
            showLabel = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Stats Header in Card
        ModernGradientCard(
            gradient = PrimaryGradient,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = stringResource(CommonR.string.study_stat_time), value = formatTime(elapsedTime))
                StatItem(label = stringResource(CommonR.string.study_stat_count), value = "${state.stats.totalAnswered}")
                val accuracy = if (state.stats.totalAnswered > 0) {
                    (state.stats.correctCount.toFloat() / state.stats.totalAnswered * 100).toInt()
                } else 0
                StatItem(label = stringResource(CommonR.string.study_stat_accuracy), value = "$accuracy%")
            }
        }

        // Question Card
        QuestionCard(
            question = state.currentQuestion,
            selectedOptionIndex = state.selectedOptionIndex,
            onOptionSelected = onOptionSelected,
            showFeedback = state.isAnswerRevealed,
            showExplanation = false,
            modifier = Modifier.fillMaxWidth(),
            language = language
        )

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
        ) {
            // Previous Button - outlined style
            OutlinedButton(
                onClick = onPreviousQuestion,
                enabled = state.hasPrevious,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnBackground,
                    disabledContentColor = OnBackground.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(CommonR.string.common_previous))
            }

            // Next/Skip Button - gradient style
            ModernGradientButton(
                text = if (state.isAnswerRevealed) stringResource(CommonR.string.common_next) else stringResource(CommonR.string.study_skip),
                onClick = onNextQuestion,
                gradient = PrimaryGradient,
                modifier = Modifier.weight(1f)
            )
        }

        // Explanation Section in Card
        if (state.isAnswerRevealed) {
            ModernGradientCard(
                gradient = if (state.selectedOptionIndex == state.currentQuestion.correctAnswerIndex)
                    AccentGradient
                else
                    WarningGradient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(CommonR.string.common_explanation),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = state.currentQuestion.getExplanation(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnBackground.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackground.copy(alpha = 0.7f)
        )
    }
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
