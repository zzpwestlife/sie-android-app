package com.example.sie.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sie.core.model.Question

@Composable
fun QuestionCard(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showFeedback: Boolean = false,
    language: String = "en"
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = question.getContent(language),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            question.options.forEachIndexed { index, _ ->
                val isSelected = selectedOptionIndex == index
                val isCorrect = index == question.correctAnswerIndex
                
                val (borderColor, containerColor, contentColor) = when {
                    showFeedback && isCorrect -> Triple(
                        Color(0xFF4CAF50), // Green
                        Color(0xFFE8F5E9), // Light Green
                        Color(0xFF1B5E20)  // Dark Green
                    )
                    showFeedback && isSelected && !isCorrect -> Triple(
                        MaterialTheme.colorScheme.error,
                        MaterialTheme.colorScheme.errorContainer,
                        MaterialTheme.colorScheme.onErrorContainer
                    )
                    isSelected -> Triple(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    else -> Triple(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.onSurface
                    )
                }

                OptionRow(
                    text = question.getOption(index, language),
                    isSelected = isSelected,
                    borderColor = borderColor,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = { if (!showFeedback) onOptionSelected(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showFeedback) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Explanation:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = question.getExplanation(language),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun OptionRow(
    text: String,
    isSelected: Boolean,
    borderColor: Color,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by Surface
                colors = RadioButtonDefaults.colors(
                    selectedColor = contentColor,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
                color = contentColor
            )
        }
    }
}

@Preview
@Composable
fun QuestionCardPreview() {
    MaterialTheme {
        QuestionCard(
            question = Question(
                id = 1,
                content = "What is the capital of France?",
                options = listOf("London", "Paris", "Berlin", "Madrid"),
                correctAnswerIndex = 1,
                explanation = "Paris is the capital of France.",
                category = "Geography"
            ),
            selectedOptionIndex = 1,
            onOptionSelected = {}
        )
    }
}
