package com.example.sie.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sie.core.designsystem.component.*
import com.example.sie.core.designsystem.theme.*

@Composable
fun HomeRoute(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    HomeScreen(
        onTopicSelectionClick = onTopicSelectionClick,
        onMockExamClick = onMockExamClick,
        onStatsClick = onStatsClick,
        onBookmarkedClick = onBookmarkedClick,
        onWrongQuestionsClick = onWrongQuestionsClick,
        onFlashcardsClick = onFlashcardsClick,
        onSettingsClick = onSettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onFlashcardsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    AppBackground {
        Scaffold(
            topBar = {
                ModernGradientTopAppBar(
                    title = "SIE Exam Prep",
                    gradient = PrimaryGradient,
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                )
            },
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(SpacingMedium),
                contentPadding = PaddingValues(vertical = SpacingMedium)
            ) {
                // Hero Card
                item {
                    ModernGradientCard(
                        gradient = PrimaryGradient,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        Text(
                            text = "Continue your SIE exam preparation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnBackgroundSecondary
                        )
                        Spacer(modifier = Modifier.height(SpacingMedium))

                        // Progress Section
                        Text(
                            text = "Study Progress",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(SpacingSmall))
                        ModernGradientProgressBar(
                            progress = 0.45f,
                            gradient = AccentGradient,
                            showLabel = true
                        )
                    }
                }

                // Statistics Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        ModernGradientCard(
                            modifier = Modifier.weight(1f),
                            gradient = TertiaryGradient
                        ) {
                            Text(
                                text = "245",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Questions\nStudied",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackgroundSecondary,
                                fontSize = 12.sp
                            )
                        }

                        ModernGradientCard(
                            modifier = Modifier.weight(1f),
                            gradient = AccentGradient
                        ) {
                            Text(
                                text = "78%",
                                style = MaterialTheme.typography.headlineMedium,
                                color = OnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Correct\nRate",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackgroundSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Feature Buttons Grid
                item {
                    Text(
                        text = "Study Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = SpacingSmall)
                    )
                    Spacer(modifier = Modifier.height(SpacingSmall))
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Study Mode",
                                onClick = onTopicSelectionClick,
                                gradient = PrimaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Mock Exam",
                                onClick = onMockExamClick,
                                gradient = SecondaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Flashcards",
                                onClick = onFlashcardsClick,
                                gradient = TertiaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Bookmarks",
                                onClick = onBookmarkedClick,
                                gradient = TertiaryGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(SpacingMedium)
                        ) {
                            ModernGradientButton(
                                text = "Wrong Questions",
                                onClick = onWrongQuestionsClick,
                                gradient = WarningGradient,
                                modifier = Modifier.weight(1f)
                            )
                            ModernGradientButton(
                                text = "Statistics",
                                onClick = onStatsClick,
                                gradient = AccentGradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
