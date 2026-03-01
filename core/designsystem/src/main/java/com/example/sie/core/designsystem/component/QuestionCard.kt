package com.example.sie.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sie.core.model.Question
import com.example.sie.core.designsystem.theme.*
import com.example.sie.core.common.R as CommonR

@Composable
fun QuestionCard(
    question: Question,
    selectedOptionIndex: Int?,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showFeedback: Boolean = false,
    showExplanation: Boolean = true,
    showQuestionText: Boolean = true,
    language: String = "en"
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = ElevationLow
        )
    ) {
        Column(
            modifier = Modifier.padding(SpacingMedium)
        ) {
            // Question Text
            if (showQuestionText) {
                Text(
                    text = question.getContent(language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = QuestionText,
                    fontWeight = FontWeight.Medium,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )

                Spacer(modifier = Modifier.height(SpacingMedium))
            }

            // Options
            question.options.forEachIndexed { index, _ ->
                val isSelected = selectedOptionIndex == index
                val isCorrect = index == question.correctAnswerIndex

                OptionRow(
                    text = question.getOption(index, language),
                    isSelected = isSelected,
                    isCorrect = isCorrect,
                    showFeedback = showFeedback,
                    onClick = { if (!showFeedback) onOptionSelected(index) }
                )

                if (index < question.options.size - 1) {
                    Spacer(modifier = Modifier.height(SpacingSmall))
                }
            }

            // Explanation
            if (showExplanation && showFeedback && question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(SpacingMedium))

                Text(
                    text = stringResource(CommonR.string.common_explanation),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = question.getExplanation(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = QuestionText.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
}

@Composable
fun OptionRow(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showFeedback: Boolean,
    onClick: () -> Unit
) {
    // Color logic based on state
    val (borderColor, containerColor, contentColor, showIcon) = when {
        // Feedback mode: Show correct answer in green
        showFeedback && isCorrect -> {
            Quadruple(
                Color(0xFF4CAF50),      // Green border
                Color(0xFFE8F5E9),      // Light green background
                Color(0xFF1B5E20),      // Dark green text
                Icons.Filled.CheckCircle
            )
        }
        // Feedback mode: Show wrong selection in red
        showFeedback && isSelected && !isCorrect -> {
            Quadruple(
                Color(0xFFF44336),      // Red border
                Color(0xFFFFEBEE),      // Light red background
                Color(0xFFC62828),      // Dark red text
                Icons.Filled.Close
            )
        }
        // Selected but no feedback yet: Use primary gradient color
        isSelected -> {
            Quadruple(
                Color(0xFF11998E),      // Teal border (from PrimaryGradient)
                Color(0xFFE0F2F1),      // Light teal background
                Color(0xFF0F172A),      // OnBackground text
                null
            )
        }
        // Default unselected state
        else -> {
            Quadruple(
                OnBackgroundSecondary.copy(alpha = 0.3f), // Light gray border
                Color.White,                              // White background
                OnBackground,                             // Dark text
                null
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadiusMedium))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CornerRadiusMedium),
        border = BorderStroke(
            width = if (isSelected || (showFeedback && isCorrect)) 2.dp else 1.dp,
            color = borderColor
        ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showFeedback && showIcon != null) {
                Icon(
                    imageVector = showIcon,
                    contentDescription = if (isCorrect) "Correct" else "Incorrect",
                    tint = contentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = SpacingSmall)
                )
            } else {
                RadioButton(
                    selected = isSelected,
                    onClick = null, // Handled by Surface
                    colors = RadioButtonDefaults.colors(
                        selectedColor = contentColor,
                        unselectedColor = contentColor.copy(alpha = 0.6f)
                    )
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = SpacingSmall)
                    .weight(1f),
                color = contentColor,
                fontWeight = if (isSelected || (showFeedback && isCorrect))
                    FontWeight.Medium
                else
                    FontWeight.Normal
            )
        }
    }
}

// Helper class for 4-tuple (Kotlin doesn't have built-in Quadruple)
private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D?
)

@Preview
@Composable
fun QuestionCardPreview() {
    MaterialTheme {
        QuestionCard(
            question = Question(
                id = 1,
                content_en = "What is the capital of France?",
                content_zh = "法国的首都是哪里？",
                options_en = listOf("London", "Paris", "Berlin", "Madrid"),
                options_zh = listOf("伦敦", "巴黎", "柏林", "马德里"),
                correctAnswerIndex = 1,
                explanation_en = "Paris is the capital of France.",
                explanation_zh = "巴黎是法国的首都。",
                category_en = "Geography",
                category_zh = "地理",
                category_short = "Geography"
            ),
            selectedOptionIndex = 1,
            onOptionSelected = {}
        )
    }
}
