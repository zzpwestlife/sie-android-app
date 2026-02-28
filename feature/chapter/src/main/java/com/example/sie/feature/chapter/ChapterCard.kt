package com.example.sie.feature.chapter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.model.Chapter
import com.example.sie.core.common.R as CommonR

@Composable
fun ChapterCard(
    chapter: Chapter,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        gradient = if (chapter.isSelected) PrimaryGradient else null,
        onClick = onToggleSelection
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chapter name
                Text(
                    text = chapter.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

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
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = stringResource(
                            CommonR.string.chapter_not_studied_format,
                            chapter.totalQuestions,
                            chapter.proportion * 100
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Checkbox
            Checkbox(
                checked = chapter.isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF667eea),
                    uncheckedColor = Color.White.copy(alpha = 0.5f),
                    checkmarkColor = Color.White
                )
            )
        }
    }
}
