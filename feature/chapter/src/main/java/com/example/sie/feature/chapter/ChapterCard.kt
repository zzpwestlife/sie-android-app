package com.example.sie.feature.chapter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientProgressBar
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.AccentGradient
import com.example.sie.core.designsystem.theme.OnSurface
import com.example.sie.core.designsystem.theme.OnBackgroundSecondary
import com.example.sie.core.designsystem.theme.SpacingSmall
import com.example.sie.core.model.Chapter
import com.example.sie.core.common.R as CommonR

@Composable
fun ChapterCard(
    chapter: Chapter,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine gradient based on chapter completion and selection
    val gradient = when {
        chapter.studiedQuestions == chapter.totalQuestions -> AccentGradient // Completed
        chapter.isSelected -> PrimaryGradient // Selected
        else -> PrimaryGradient // Default
    }

    ModernGradientCard(
        modifier = modifier.fillMaxWidth(),
        gradient = gradient,
        onClick = onToggleSelection
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Chapter name
                Text(
                    text = chapter.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(SpacingSmall))

                // Progress info
                if (chapter.studiedQuestions > 0) {
                    Text(
                        text = stringResource(
                            CommonR.string.chapter_progress_format,
                            chapter.totalQuestions,
                            chapter.studiedQuestions,
                            chapter.accuracyRate,
                            chapter.proportion * 100
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackgroundSecondary
                    )

                    Spacer(modifier = Modifier.height(SpacingSmall))

                    // Progress bar
                    ModernGradientProgressBar(
                        progress = if (chapter.totalQuestions > 0) {
                            chapter.studiedQuestions.toFloat() / chapter.totalQuestions.toFloat()
                        } else {
                            0f
                        },
                        gradient = if (chapter.studiedQuestions == chapter.totalQuestions) {
                            AccentGradient
                        } else {
                            PrimaryGradient
                        },
                        showLabel = false
                    )
                } else {
                    Text(
                        text = stringResource(
                            CommonR.string.chapter_not_studied_format,
                            chapter.totalQuestions,
                            chapter.proportion * 100
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackgroundSecondary
                    )
                }
            }

            // Checkbox
            Checkbox(
                checked = chapter.isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF11998E),
                    uncheckedColor = OnBackgroundSecondary,
                    checkmarkColor = Color.White
                )
            )
        }
    }
}
