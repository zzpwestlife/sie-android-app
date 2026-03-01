package com.example.sie.feature.home

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.component.QuestionCard
import com.example.sie.core.designsystem.theme.AccentGradient
import com.example.sie.core.designsystem.theme.OnBackground
import com.example.sie.core.designsystem.theme.OnBackgroundSecondary
import com.example.sie.core.designsystem.theme.OnSurface
import com.example.sie.core.model.Question

@Composable
fun BookmarkedRoute(
    onBackClick: () -> Unit = {},
    viewModel: BookmarkedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val language by viewModel.language.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    BookmarkedScreen(
        uiState = uiState,
        language = language,
        snackbarHostState = snackbarHostState,
        onCategorySelected = viewModel::selectCategory,
        onRemoveBookmark = viewModel::toggleBookmark,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookmarkedScreen(
    uiState: BookmarkedUiState,
    language: String,
    snackbarHostState: SnackbarHostState,
    onCategorySelected: (String?) -> Unit,
    onRemoveBookmark: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.bookmarked_title),
                    gradient = AccentGradient,
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
                    BookmarkedUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    BookmarkedUiState.Empty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = OnBackgroundSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(CommonR.string.bookmarked_empty),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnBackgroundSecondary
                                )
                            }
                        }
                    }

                    is BookmarkedUiState.Success -> {
                        BookmarkedContent(
                            questions = uiState.questions,
                            categories = uiState.categories,
                            selectedCategory = uiState.selectedCategory,
                            language = language,
                            onCategorySelected = onCategorySelected,
                            onRemoveBookmark = onRemoveBookmark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkedContent(
    questions: List<Question>,
    categories: List<String>,
    selectedCategory: String?,
    language: String,
    onCategorySelected: (String?) -> Unit,
    onRemoveBookmark: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Badge showing bookmark count
        item {
            ModernGradientCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = AccentGradient
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = OnSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            CommonR.string.bookmarked_count,
                            questions.size
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            CategoryFilterChips(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        // Bookmarked Questions
        items(
            items = questions,
            key = { it.id }
        ) { question ->
            BookmarkedQuestionCard(
                question = question,
                language = language,
                onRemoveBookmark = { onRemoveBookmark(question.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFF11998E)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = OnBackgroundSecondary,
                    selectedBorderColor = Color(0xFF11998E),
                    enabled = true,
                    selected = selectedCategory == null,
                    borderWidth = 1.dp
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
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Color(0xFF11998E)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = OnBackgroundSecondary,
                    selectedBorderColor = Color(0xFF11998E),
                    enabled = true,
                    selected = selectedCategory == category,
                    borderWidth = 1.dp
                )
            )
        }
    }
}

@Composable
private fun BookmarkedQuestionCard(
    question: Question,
    language: String,
    onRemoveBookmark: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Header: Category + Remove button
        ModernGradientCard(
            modifier = Modifier.fillMaxWidth(),
            gradient = AccentGradient,
            onClick = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF11998E),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.getLocalizedContent(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onRemoveBookmark) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(CommonR.string.common_remove_bookmark),
                        tint = OnBackgroundSecondary
                    )
                }
            }
        }

        // Expanded: QuestionCard with options + explanation
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))

            QuestionCard(
                question = question,
                selectedOptionIndex = question.correctAnswerIndex,
                onOptionSelected = {},
                showFeedback = true,
                showExplanation = true,
                showQuestionText = false,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
