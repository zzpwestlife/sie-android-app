package com.example.sie.feature.study

import androidx.compose.foundation.background
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.QuestionCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun StudyRoute(
    onBackClick: () -> Unit,
    viewModel: StudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    StudyScreen(
        uiState = uiState,
        onOptionSelected = viewModel::selectOption,
        onNextQuestion = viewModel::loadNextQuestion,
        onPreviousQuestion = viewModel::loadPreviousQuestion,
        onToggleBookmark = viewModel::toggleBookmark,
        onBackClick = onBackClick
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
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Practice Mode", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        if (uiState is StudyUiState.Success) {
                            val isBookmarked = uiState.currentQuestion.isBookmarked
                            IconButton(
                                onClick = {
                                    onToggleBookmark(uiState.currentQuestion.id)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            if (isBookmarked) "Bookmark removed"
                                            else "Bookmarked"
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Star
                                        else Icons.Outlined.Star,
                                    contentDescription = if (isBookmarked) "Remove bookmark"
                                        else "Add bookmark",
                                    tint = if (isBookmarked) Color(0xFFfee140)
                                        else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
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
                                Text("Go Back")
                            }
                        }
                    }
                    is StudyUiState.Success -> {
                        StudyContent(
                            state = uiState,
                            onOptionSelected = onOptionSelected,
                            onNextQuestion = onNextQuestion,
                            onPreviousQuestion = onPreviousQuestion
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
    onPreviousQuestion: () -> Unit
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "Time", value = formatTime(elapsedTime))
            StatItem(label = "Count", value = "${state.stats.totalAnswered}")
            val accuracy = if (state.stats.totalAnswered > 0) {
                (state.stats.correctCount.toFloat() / state.stats.totalAnswered * 100).toInt()
            } else 0
            StatItem(label = "Accuracy", value = "$accuracy%")
        }

        // Question Card
        QuestionCard(
            question = state.currentQuestion,
            selectedOptionIndex = state.selectedOptionIndex,
            onOptionSelected = onOptionSelected,
            showFeedback = state.isAnswerRevealed,
            showExplanation = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onPreviousQuestion,
                enabled = state.hasPrevious,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.38f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Text("Previous")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNextQuestion,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea),
                    contentColor = Color.White
                )
            ) {
                Text(if (state.isAnswerRevealed) "Next" else "Skip")
            }
        }

        // Explanation Section
        if (state.isAnswerRevealed) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Explanation:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4facfe),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.currentQuestion.getExplanation("en"),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color.White.copy(alpha = 0.9f)
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
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
