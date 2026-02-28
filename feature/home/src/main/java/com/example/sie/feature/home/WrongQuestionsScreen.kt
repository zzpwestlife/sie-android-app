package com.example.sie.feature.home

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Delete
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
        onRemoveFromWrong = viewModel::removeFromWrong
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WrongQuestionsScreen(
    uiState: WrongQuestionsUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRemoveFromWrong: (Int) -> Unit
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
                    title = "Wrong Questions",
                    navigationIcon = Icons.Filled.ArrowBack,
                    navigationIconContentDescription = "Back",
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
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No wrong questions yet",
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
                        onRemoveFromWrong = onRemoveFromWrong
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
    onRemoveFromWrong: (Int) -> Unit
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
                onRemoveClick = { onRemoveFromWrong(question.id) }
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
            text = "Statistics",
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
                label = "Total"
            )
            StatItem(
                value = stats.totalWrongCount.toString(),
                label = "Wrong"
            )
            StatItem(
                value = String.format("%.1f", stats.avgWrongCount),
                label = "Avg"
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
                        text = "All",
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
    modifier: Modifier = Modifier
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        gradient = ErrorGradient
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
                    color = Color.White,
                    maxLines = 3,
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
            IconButton(onClick = { showRemoveDialog = true }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove from wrong list",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = {
                Text(text = "Remove Question")
            },
            text = {
                Text(text = "Are you sure you want to remove this question from the wrong list?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRemoveDialog = false
                        onRemoveClick()
                    }
                ) {
                    Text(text = "Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
