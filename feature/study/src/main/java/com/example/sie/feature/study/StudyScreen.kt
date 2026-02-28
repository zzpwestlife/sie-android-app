package com.example.sie.feature.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.designsystem.component.QuestionCard

@Composable
fun StudyRoute(
    viewModel: StudyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    StudyScreen(
        uiState = uiState,
        onOptionSelected = viewModel::selectOption,
        onNextQuestion = viewModel::loadNewQuestion
    )
}

@Composable
internal fun StudyScreen(
    uiState: StudyUiState,
    onOptionSelected: (Int) -> Unit,
    onNextQuestion: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is StudyUiState.Loading -> {
                CircularProgressIndicator()
            }
            is StudyUiState.Error -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is StudyUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    QuestionCard(
                        question = uiState.currentQuestion,
                        selectedOptionIndex = uiState.selectedOptionIndex,
                        onOptionSelected = onOptionSelected,
                        showFeedback = uiState.isAnswerRevealed,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (uiState.isAnswerRevealed) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (uiState.currentQuestion.explanation.isNotEmpty()) {
                             Text(
                                text = "Explanation: ${uiState.currentQuestion.explanation}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 16.dp)
                             )
                        }

                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Next Question")
                        }
                    }
                }
            }
        }
    }
}
