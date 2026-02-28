package com.example.sie.feature.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.component.SieTopAppBar
import com.example.sie.core.designsystem.theme.ErrorGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
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
                SieTopAppBar(
                    title = stringResource(CommonR.string.wrong_questions_title),
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconContentDescription = stringResource(CommonR.string.common_back),
                    onNavigationClick = onBackClick,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
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
                        CircularProgressIndicator(color = Color.White)
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
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(CommonR.string.wrong_questions_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WrongQuestionsStatsCard(
    stats: WrongQuestionsStats,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        gradient = SecondaryGradient
    ) {
        Text(
            text = stringResource(CommonR.string.wrong_questions_stats_title),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
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
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = {
                    Text(
                        text = stringResource(CommonR.string.common_filter_all),
                        color = Color.White
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    selectedContainerColor = Color(0xFF667eea)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.3f),
                    selectedBorderColor = Color(0xFF667eea),
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
                        color = Color.White
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    selectedContainerColor = Color(0xFF667eea)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.3f),
                    selectedBorderColor = Color(0xFF667eea),
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

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        gradient = ErrorGradient
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = question.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\u00d7${question.wrongCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFff6b6b),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = stringResource(CommonR.string.common_bookmark),
                            tint = if (question.isBookmarked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = { showRemoveDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(CommonR.string.wrong_questions_remove_from_list),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // Options
                question.options.forEachIndexed { index, option ->
                    val isCorrect = index == question.correctAnswerIndex
                    val backgroundColor = if (isCorrect) Color(0xFF4caf50).copy(alpha = 0.2f) else Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(backgroundColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${(65 + index).toChar()}. $option",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCorrect) Color(0xFF4caf50) else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Explanation
                Text(
                    text = stringResource(CommonR.string.common_explanation),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
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
