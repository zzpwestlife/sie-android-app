package com.example.sie.feature.exam

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.*
import com.example.sie.core.model.ExamResult
import com.example.sie.core.model.Question
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExamDetailRoute(
    onBackClick: () -> Unit,
    viewModel: ExamDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    ExamDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleFilter = viewModel::toggleFilter,
        language = language
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamDetailScreen(
    uiState: ExamDetailUiState,
    onBackClick: () -> Unit,
    onToggleFilter: () -> Unit,
    language: String
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.exam_detail_title),
                    gradient = SecondaryGradient,
                    onNavigationClick = onBackClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState) {
                    ExamDetailUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    ExamDetailUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(CommonR.string.common_error),
                                color = OnBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    is ExamDetailUiState.LegacyExam -> {
                        LegacyExamContent(examResult = uiState.examResult)
                    }
                    is ExamDetailUiState.Success -> {
                        ExamDetailContent(
                            state = uiState,
                            onToggleFilter = onToggleFilter,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegacyExamContent(examResult: ExamResult) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ModernGradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = SecondaryGradient
        ) {
            Text(
                text = dateFormat.format(Date(examResult.date)),
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${examResult.score}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${examResult.correctCount}/${examResult.totalQuestions}",
                style = MaterialTheme.typography.titleLarge,
                color = OnBackgroundSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = OnBackgroundSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(CommonR.string.exam_detail_legacy_exam),
            style = MaterialTheme.typography.titleMedium,
            color = OnBackground,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(CommonR.string.exam_detail_legacy_exam_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackgroundSecondary,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExamDetailContent(
    state: ExamDetailUiState.Success,
    onToggleFilter: () -> Unit,
    language: String
) {
    val filteredQuestions = state.filteredQuestions
    val filteredAnswers = state.filteredAnswers
    val answersMap = filteredAnswers.associateBy { it.questionId }

    if (filteredQuestions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(CommonR.string.exam_history_empty),
                color = OnBackground,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { filteredQuestions.size })
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        // Header info card
        ModernGradientCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            gradient = SecondaryGradient
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFormat.format(Date(state.examResult.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackgroundSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.examResult.score}% · ${state.examResult.correctCount}/${state.examResult.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !state.filterWrongOnly,
                        onClick = { if (state.filterWrongOnly) onToggleFilter() },
                        label = {
                            Text(
                                text = stringResource(CommonR.string.exam_detail_filter_all),
                                color = if (!state.filterWrongOnly) Color.White else OnSurface
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1FA2FF),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = state.filterWrongOnly,
                        onClick = { if (!state.filterWrongOnly) onToggleFilter() },
                        label = {
                            Text(
                                text = stringResource(CommonR.string.exam_detail_filter_wrong),
                                color = if (state.filterWrongOnly) Color.White else OnSurface
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF6B6B),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Question counter
        Text(
            text = stringResource(
                CommonR.string.exam_detail_question_index,
                pagerState.currentPage + 1,
                filteredQuestions.size
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = OnBackgroundSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Question pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val question = filteredQuestions[page]
            val answer = answersMap[question.id]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column {
                    // Status indicators
                    if (answer != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!answer.isAnswered) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(CommonR.string.exam_detail_not_answered),
                                        color = Color(0xFF9E9E9E),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (answer.isCorrect) Color(0xFF00C9FF) else Color(0xFFFF6B6B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (answer.isCorrect) stringResource(CommonR.string.exam_result_correct)
                                               else stringResource(CommonR.string.exam_result_incorrect),
                                        color = if (answer.isCorrect) Color(0xFF00C9FF) else Color(0xFFFF6B6B),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (answer.isFlagged) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFFA500),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = stringResource(CommonR.string.exam_detail_flagged),
                                        color = Color(0xFFFFA500),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Question card with Modern Gradient design
                    ModernGradientQuestionCard(
                        question = question,
                        selectedOptionIndex = answer?.selectedOptionIndex,
                        isCorrect = answer?.isCorrect ?: false,
                        isAnswered = answer?.isAnswered ?: false,
                        language = language
                    )
                }
            }
        }

        // Bottom navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = pagerState.currentPage > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OnSurface
                ),
                border = BorderStroke(1.dp, OnBackgroundSecondary.copy(alpha = 0.5f))
            ) {
                Text(stringResource(CommonR.string.common_previous))
            }

            Button(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage < filteredQuestions.size - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = pagerState.currentPage < filteredQuestions.size - 1
            ) {
                Text(stringResource(CommonR.string.common_next))
            }
        }
    }
}

@Composable
private fun ModernGradientQuestionCard(
    question: Question,
    selectedOptionIndex: Int?,
    isCorrect: Boolean,
    isAnswered: Boolean,
    language: String,
    modifier: Modifier = Modifier
) {
    // Determine left gradient based on answer correctness
    val cardGradient = when {
        !isAnswered -> PrimaryGradient
        isCorrect -> AccentGradient
        else -> WarningGradient
    }

    var isExplanationExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExplanationExpanded) 180f else 0f,
        label = "explanation_arrow_rotation"
    )

    Column(modifier = modifier) {
        // Question content card
        ModernGradientCard(
            gradient = cardGradient
        ) {
            // Question text
            Text(
                text = question.getLocalizedContent(language),
                style = MaterialTheme.typography.bodyLarge,
                color = QuestionText,
                fontWeight = FontWeight.Medium,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(SpacingMedium))

            // Options
            question.options.forEachIndexed { index, _ ->
                val optionText = question.getOption(index, language)
                val isSelected = selectedOptionIndex == index
                val isThisCorrect = index == question.correctAnswerIndex

                val (borderColor, containerColor, textColor) = when {
                    isAnswered && isThisCorrect -> Triple(
                        Color(0xFF4CAF50),
                        Color(0xFFE8F5E9),
                        Color(0xFF1B5E20)
                    )
                    isAnswered && isSelected && !isThisCorrect -> Triple(
                        Color(0xFFF44336),
                        Color(0xFFFFEBEE),
                        Color(0xFFC62828)
                    )
                    isSelected -> Triple(
                        Color(0xFF11998E),
                        Color(0xFFE0F2F1),
                        Color(0xFF0F172A)
                    )
                    else -> Triple(
                        OnBackgroundSecondary.copy(alpha = 0.3f),
                        Color.White,
                        OnBackground
                    )
                }

                ModernOptionRow(
                    text = optionText,
                    isSelected = isSelected,
                    borderColor = borderColor,
                    containerColor = containerColor,
                    textColor = textColor
                )

                if (index < question.options.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Expandable explanation card
        if (isAnswered) {
            Spacer(modifier = Modifier.height(12.dp))

            ModernGradientCard(
                gradient = if (isCorrect) AccentGradient else WarningGradient,
                onClick = { isExplanationExpanded = !isExplanationExpanded }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(CommonR.string.common_explanation),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF006064) else Color(0xFFC62828)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExplanationExpanded) "Collapse" else "Expand",
                        tint = if (isCorrect) Color(0xFF006064) else Color(0xFFC62828),
                        modifier = Modifier.rotate(rotationAngle)
                    )
                }

                AnimatedVisibility(
                    visible = isExplanationExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        Text(
                            text = question.getExplanation(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackground,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernOptionRow(
    text: String,
    isSelected: Boolean,
    borderColor: Color,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadiusMedium),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = textColor,
                    unselectedColor = textColor.copy(alpha = 0.6f)
                )
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = SpacingSmall)
                    .weight(1f),
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
