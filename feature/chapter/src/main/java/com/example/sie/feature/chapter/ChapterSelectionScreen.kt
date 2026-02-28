package com.example.sie.feature.chapter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.common.R as CommonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectionScreen(
    onBackClick: () -> Unit,
    onStartStudy: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    // Calculate hasSelected based on current UI state
    val hasSelectedChapters = when (val state = uiState) {
        is ChapterSelectionUiState.Success -> state.chapters.any { it.isSelected }
        else -> false
    }

    LaunchedEffect(language) {
        viewModel.setLanguage(language)
    }

    Box(
        modifier = modifier
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
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(CommonR.string.chapter_title),
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(CommonR.string.common_back),
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                val selected = viewModel.getSelectedChapters()
                                android.util.Log.d("ChapterSelection", "Start button clicked. Selected: $selected")
                                if (selected.isNotEmpty()) {
                                    android.util.Log.d("ChapterSelection", "Calling onStartStudy with $selected")
                                    onStartStudy(selected)
                                } else {
                                    android.util.Log.w("ChapterSelection", "No chapters selected!")
                                }
                            },
                            enabled = hasSelectedChapters
                        ) {
                            Text(
                                stringResource(CommonR.string.chapter_start),
                                color = if (hasSelectedChapters)
                                    Color.White
                                else
                                    Color.White.copy(alpha = 0.4f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is ChapterSelectionUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is ChapterSelectionUiState.Success -> {
                    if (state.chapters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(CommonR.string.chapter_empty),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.chapters,
                                key = { it.name }
                            ) { chapter ->
                                ChapterCard(
                                    chapter = chapter,
                                    onToggleSelection = {
                                        viewModel.toggleChapterSelection(chapter.name)
                                    }
                                )
                            }
                        }
                    }
                }

                is ChapterSelectionUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = stringResource(CommonR.string.chapter_error),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Button(
                                onClick = { /* TODO: Implement retry */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF667eea)
                                )
                            ) {
                                Text(stringResource(CommonR.string.chapter_retry))
                            }
                        }
                    }
                }
            }
        }
    }
}
