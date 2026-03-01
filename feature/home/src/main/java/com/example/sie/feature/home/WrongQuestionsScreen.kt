package com.example.sie.feature.home

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.theme.WarningGradient
import com.example.sie.core.designsystem.theme.OnBackground
import com.example.sie.core.designsystem.theme.SpacingMedium
import com.example.sie.core.designsystem.theme.SpacingSmall
import com.example.sie.core.model.Question

@Composable
fun WrongQuestionsRoute(
    onBackClick: () -> Unit,
    viewModel: WrongQuestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    WrongQuestionsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onCategorySelected = viewModel::selectCategory,
        onRemoveFromWrong = viewModel::removeFromWrong,
        onToggleBookmark = viewModel::toggleBookmark
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WrongQuestionsScreen(
    uiState: WrongQuestionsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRemoveFromWrong: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.wrong_questions_title),
                    gradient = WarningGradient,
                    onNavigationClick = onBackClick
                )
            }
        ) { paddingValues ->
            when (uiState) {
                is WrongQuestionsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is WrongQuestionsUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OnBackground.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(CommonR.string.wrong_questions_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                is WrongQuestionsUiState.Success -> {
                    WrongQuestionsContent(
                        state = uiState,
                        modifier = Modifier.padding(paddingValues),
                        onCategorySelected = onCategorySelected,
                        onRemoveFromWrong = onRemoveFromWrong,
                        onToggleBookmark = onToggleBookmark
                    )
                }
            }
        }
    }
}

@Composable
private fun WrongQuestionsContent(
    state: WrongQuestionsUiState.Success,
    modifier: Modifier = Modifier,
    onCategorySelected: (String?) -> Unit,
    onRemoveFromWrong: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = SpacingMedium),
        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
    ) {
        item {
            Spacer(modifier = Modifier.height(SpacingSmall))
            WrongQuestionsStatsCard(stats = state.stats)
        }

        item {
            CategoryFilterChips(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        items(
            items = state.questions,
            key = { it.id }
        ) { question ->
            WrongQuestionCard(
                question = question,
                onRemoveClick = { onRemoveFromWrong(question.id) },
                onToggleBookmark = { onToggleBookmark(question.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(SpacingMedium))
        }
    }
}

@Composable
private fun WrongQuestionsStatsCard(
    stats: WrongQuestionsStats,
    modifier: Modifier = Modifier
) {
    ModernGradientCard(
        modifier = modifier.fillMaxWidth(),
        gradient = WarningGradient
    ) {
        // Header Badge
        Box(
            modifier = Modifier
                .background(
                    brush = WarningGradient,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Text(
                    text = stringResource(CommonR.string.wrong_questions_stats_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(SpacingMedium))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = stats.totalCount.toString(),
                label = stringResource(CommonR.string.wrong_questions_stat_total)
            )
            StatItem(
                value = stats.totalWrongCount.toString(),
                label = stringResource(CommonR.string.wrong_questions_stat_wrong)
            )
            StatItem(
                value = String.format("%.1f", stats.avgWrongCount),
                label = stringResource(CommonR.string.wrong_questions_stat_avg)
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = OnBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(SpacingSmall))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OnBackground.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingSmall)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = stringResource(CommonR.string.common_filter_all),
                        color = if (selectedCategory == null) Color.White else OnBackground
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFFFF6B6B),
                    labelColor = OnBackground,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = OnBackground.copy(alpha = 0.2f),
                    selectedBorderColor = Color.Transparent,
                    enabled = true,
                    selected = selectedCategory == null
                )
            )
        }
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        color = if (selectedCategory == category) Color.White else OnBackground
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFFFF6B6B),
                    labelColor = OnBackground,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = OnBackground.copy(alpha = 0.2f),
                    selectedBorderColor = Color.Transparent,
                    enabled = true,
                    selected = selectedCategory == category
                )
            )
        }
    }
}

@Composable
private fun WrongQuestionCard(
    question: Question,
    onRemoveClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    ModernGradientCard(
        modifier = modifier.fillMaxWidth(),
        gradient = WarningGradient,
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnBackground,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(SpacingSmall))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = question.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = OnBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(SpacingSmall))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFF6B6B),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "\u00d7${question.wrongCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(CommonR.string.common_bookmark),
                            tint = if (question.isBookmarked) Color(0xFFFFD700) else OnBackground.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = { showRemoveDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(CommonR.string.wrong_questions_remove_from_list),
                            tint = OnBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(SpacingMedium))

                // Options
                question.options.forEachIndexed { index, option ->
                    val isCorrect = index == question.correctAnswerIndex

                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isCorrect) Color(0xFF00C9FF).copy(alpha = 0.1f) else Color.Transparent,
                            contentColor = if (isCorrect) Color(0xFF00C9FF) else OnBackground,
                            disabledContainerColor = if (isCorrect) Color(0xFF00C9FF).copy(alpha = 0.1f) else Color.Transparent,
                            disabledContentColor = if (isCorrect) Color(0xFF00C9FF) else OnBackground.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${(65 + index).toChar()}. $option",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SpacingMedium))

                // Explanation
                Text(
                    text = stringResource(CommonR.string.common_explanation),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(SpacingSmall))
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = {
                Text(text = stringResource(CommonR.string.wrong_questions_remove_title))
            },
            text = {
                Text(text = stringResource(CommonR.string.wrong_questions_remove_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onRemoveClick()
                    }
                ) {
                    Text(text = stringResource(CommonR.string.common_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(text = stringResource(CommonR.string.common_cancel))
                }
            }
        )
    }
}
