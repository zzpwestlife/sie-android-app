package com.example.sie.feature.exam

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.designsystem.component.QuestionCard
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.GradientProgressIndicator
import com.example.sie.core.designsystem.component.GradientButton
import com.example.sie.core.designsystem.component.AppBackground
import com.example.sie.core.designsystem.component.ModernGradientTopAppBar
import com.example.sie.core.designsystem.component.ModernGradientCard
import com.example.sie.core.designsystem.component.ModernGradientButton
import com.example.sie.core.designsystem.component.ModernGradientProgressBar
import com.example.sie.core.designsystem.theme.SuccessGradient
import com.example.sie.core.designsystem.theme.ErrorGradient
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
import com.example.sie.core.designsystem.theme.AccentGradient
import com.example.sie.core.designsystem.theme.WarningGradient
import com.example.sie.core.designsystem.theme.OnBackground
import com.example.sie.core.designsystem.theme.OnBackgroundSecondary
import com.example.sie.core.designsystem.theme.OnSurface
import com.example.sie.core.designsystem.theme.SpacingMedium
import com.example.sie.core.designsystem.theme.SpacingSmall
import com.example.sie.core.designsystem.theme.SpacingLarge
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ExamRoute(
    onBackClick: () -> Unit,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    ExamScreen(
        uiState = uiState,
        onStartExam = viewModel::startExam,
        onAnswerSelected = viewModel::onAnswerSelected,
        onToggleBookmark = viewModel::toggleBookmark,
        onSubmitExam = viewModel::submitExam,
        onBackClick = onBackClick,
        onQuestionSelected = viewModel::onQuestionSelected,
        onResetExam = viewModel::resetExam,
        language = language
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamScreen(
    uiState: ExamUiState,
    onStartExam: () -> Unit,
    onAnswerSelected: (Int, Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onBackClick: () -> Unit,
    onQuestionSelected: (Int) -> Unit,
    onResetExam: () -> Unit,
    language: String
) {
    AppBackground {
        when (uiState) {
            ExamUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            ExamUiState.Intro -> {
                ExamIntroContent(
                    onStartExam = onStartExam,
                    onBackClick = onBackClick,
                    language = language
                )
            }
            is ExamUiState.InProgress -> {
                ExamInProgressContent(
                    state = uiState,
                    onAnswerSelected = onAnswerSelected,
                    onToggleBookmark = onToggleBookmark,
                    onSubmitExam = onSubmitExam,
                    onQuestionSelected = onQuestionSelected,
                    language = language
                )
            }
            is ExamUiState.Finished -> {
                ExamResultContent(
                    state = uiState,
                    onToggleBookmark = onToggleBookmark,
                    onBackClick = onBackClick,
                    onResetExam = onResetExam,
                    language = language
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamIntroContent(
    onStartExam: () -> Unit,
    onBackClick: () -> Unit,
    language: String
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            ModernGradientTopAppBar(
                title = stringResource(CommonR.string.exam_title),
                gradient = SecondaryGradient,
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(SpacingMedium)
        ) {
            // Exam Rules Card
            ModernGradientCard(
                gradient = SecondaryGradient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                ) {
                    Text(
                        text = stringResource(CommonR.string.exam_rules_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )

                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.List,
                        title = stringResource(CommonR.string.exam_intro_questions),
                        subtitle = stringResource(CommonR.string.exam_intro_questions_subtitle)
                    )
                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.Notifications,
                        title = stringResource(CommonR.string.exam_intro_time),
                        subtitle = stringResource(CommonR.string.exam_intro_time_subtitle)
                    )
                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        title = stringResource(CommonR.string.exam_intro_pass),
                        subtitle = stringResource(CommonR.string.exam_intro_pass_subtitle)
                    )
                }
            }

            Spacer(modifier = Modifier.height(SpacingMedium))

            // Start Exam Button - Large and prominent
            ModernGradientButton(
                text = stringResource(CommonR.string.exam_button_start),
                onClick = onStartExam,
                gradient = SecondaryGradient,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            Spacer(modifier = Modifier.height(SpacingMedium))
        }
    }
}

@Composable
private fun IntroItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF1FA2FF), // SecondaryGradient start color
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(SpacingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackgroundSecondary,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ExamInProgressContent(
    state: ExamUiState.InProgress,
    onAnswerSelected: (Int, Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onQuestionSelected: (Int) -> Unit,
    language: String
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentQuestionIndex,
        pageCount = { state.questions.size }
    )
    val scope = rememberCoroutineScope()
    var showReviewDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // Pre-fetch strings for use in coroutine scope
    val bookmarkedText = stringResource(CommonR.string.study_bookmarked)
    val bookmarkRemovedText = stringResource(CommonR.string.study_bookmark_removed)

    LaunchedEffect(state.currentQuestionIndex) {
        if (pagerState.currentPage != state.currentQuestionIndex) {
            pagerState.animateScrollToPage(state.currentQuestionIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentQuestionIndex) {
            onQuestionSelected(pagerState.currentPage)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            ModernGradientTopAppBar(
                title = stringResource(CommonR.string.exam_title),
                gradient = SecondaryGradient,
                actions = {
                    // Timer Badge with gradient background
                    val timerGradient = if (state.timeLeftMillis < 10 * 60 * 1000) {
                        WarningGradient
                    } else {
                        SecondaryGradient
                    }

                    // Pulse animation when time is running out
                    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (state.timeLeftMillis < 10 * 60 * 1000) 1.08f else 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "timer_pulse_scale"
                    )

                    Box(
                        modifier = Modifier
                            .padding(end = SpacingSmall)
                            .background(
                                brush = timerGradient,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .scale(scale)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatTime(state.timeLeftMillis),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = if (state.timeLeftMillis < 10 * 60 * 1000) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    androidx.compose.material3.IconButton(onClick = { showReviewDialog = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(CommonR.string.exam_dialog_review_title),
                            tint = Color.White
                        )
                    }

                    TextButton(onClick = { showSubmitDialog = true }) {
                        Text(
                            text = stringResource(CommonR.string.exam_button_submit),
                            color = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(CommonR.string.exam_detail_question_index, pagerState.currentPage + 1, state.questions.size),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Big Action Button
                val isLastQuestion = pagerState.currentPage == state.questions.size - 1
                ModernGradientButton(
                    text = if (isLastQuestion) stringResource(CommonR.string.exam_button_submit_exam) else stringResource(CommonR.string.exam_button_next_question),
                    onClick = {
                        if (isLastQuestion) {
                            showSubmitDialog = true
                        } else {
                            scope.launch {
                                val nextPage = pagerState.currentPage + 1
                                if (nextPage < state.questions.size) {
                                    onQuestionSelected(nextPage)
                                }
                            }
                        }
                    },
                    gradient = SecondaryGradient,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            // Use key to prevent recomposition issues if list changes (though it shouldn't in exam)
            // But importantly, pass language to QuestionCard
            val question = state.questions[page]
            val isBookmarked = question.isBookmarked
            // Access the question by index from state to be safe
            
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                
                Column {
                    // Category Tag and Bookmark Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.SuggestionChip(
                            onClick = {},
                            label = { Text(question.category.substringBefore("/")) }, // Show English part mostly or simplify
                            colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )

                            androidx.compose.material3.IconButton(
                                onClick = {
                                    onToggleBookmark(question.id)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(
                                            if (isBookmarked) bookmarkRemovedText else bookmarkedText
                                        )
                                    }
                                }
                            ) {
                            androidx.compose.material3.Icon(
                                imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Filled.Star else androidx.compose.material.icons.Icons.Outlined.Star,
                                contentDescription = stringResource(CommonR.string.common_bookmark),
                                tint = if (isBookmarked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f), // Gold for active, semi-transparent white for inactive
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    QuestionCard(
                        question = question,
                        selectedOptionIndex = state.userAnswers[question.id],
                        onOptionSelected = { answerIndex ->
                            onAnswerSelected(question.id, answerIndex)
                        },
                        showFeedback = false,
                        language = language,
                        // Add flag button to card header if possible, or just overlay it
                    )
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text(stringResource(CommonR.string.exam_dialog_review_title)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(state.questions) { index, question ->
                        val isAnswered = state.userAnswers.containsKey(question.id)
                        val isCurrent = index == state.currentQuestionIndex
                        
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .width(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                             Button(
                                 onClick = {
                                     onQuestionSelected(index)
                                     showReviewDialog = false
                                 },
                                 contentPadding = PaddingValues(0.dp),
                                 colors = ButtonDefaults.buttonColors(
                                     containerColor = if (isCurrent) MaterialTheme.colorScheme.primary 
                                                      else if (isAnswered) MaterialTheme.colorScheme.secondaryContainer
                                                      else MaterialTheme.colorScheme.surfaceVariant,
                                     contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                                    else if (isAnswered) MaterialTheme.colorScheme.onSecondaryContainer
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             ) {
                                 Text((index + 1).toString())
                             }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(stringResource(CommonR.string.common_close))
                }
            }
        )
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(stringResource(CommonR.string.exam_dialog_submit_title)) },
            text = { Text(stringResource(CommonR.string.exam_dialog_submit_message, state.questions.size - state.userAnswers.size)) },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    onSubmitExam()
                }) {
                    Text(stringResource(CommonR.string.common_submit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text(stringResource(CommonR.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamResultContent(
    state: ExamUiState.Finished,
    onToggleBookmark: (Int) -> Unit,
    onBackClick: () -> Unit,
    onResetExam: () -> Unit,
    language: String
) {
    var isReviewing by remember { mutableStateOf(false) }

    if (isReviewing) {
        ExamReviewContent(
            state = state,
            onCloseReview = { isReviewing = false },
            onToggleBookmark = onToggleBookmark,
            language = language
        )
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                ModernGradientTopAppBar(
                    title = stringResource(CommonR.string.exam_result_title),
                    gradient = SecondaryGradient,
                    onNavigationClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(SpacingMedium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SpacingLarge)
            ) {
                // Score Circle and Status
                ModernGradientCard(
                    gradient = if (state.passed) AccentGradient else WarningGradient,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        // Pass/Fail Icon
                        androidx.compose.material3.Icon(
                            imageVector = if (state.passed) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = null,
                            tint = if (state.passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = if (state.passed) stringResource(CommonR.string.exam_result_passed) else stringResource(CommonR.string.exam_result_failed),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.passed) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )

                        // Animated Score Circle
                        AnimatedScoreCircle(
                            score = state.score,
                            passed = state.passed
                        )

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = stringResource(CommonR.string.exam_result_correct),
                                value = "${(state.score * state.totalQuestions) / 100}",
                                color = Color(0xFF4CAF50)
                            )
                            // Vertical Divider
                            androidx.compose.material3.VerticalDivider(modifier = Modifier.height(40.dp))
                            StatItem(
                                label = stringResource(CommonR.string.exam_result_incorrect),
                                value = "${state.totalQuestions - (state.score * state.totalQuestions) / 100}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(SpacingSmall)
                ) {
                    ModernGradientButton(
                        text = stringResource(CommonR.string.exam_result_review),
                        gradient = SecondaryGradient,
                        onClick = { isReviewing = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    ModernGradientButton(
                        text = stringResource(CommonR.string.exam_result_retake),
                        gradient = PrimaryGradient,
                        onClick = onResetExam,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(CommonR.string.common_back_to_home),
                            color = OnBackground
                        )
                    }
                }

                // Category Breakdown
                ModernGradientCard(
                    gradient = SecondaryGradient,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(SpacingMedium)
                    ) {
                        Text(
                            text = stringResource(CommonR.string.exam_result_category_breakdown),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        
                        // Calculate stats per category
                        val categoryStats = state.questions.groupBy { it.category }
                            .mapValues { (_, questions) ->
                                val correct = questions.count { q -> state.userAnswers[q.id] == q.correctAnswerIndex }
                                val total = questions.size
                                Pair(correct, total)
                            }
                        
                        categoryStats.forEach { (category, stats) ->
                            val (correct, total) = stats
                            val percentage = (correct.toFloat() / total * 100).toInt()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.substringBefore("/"), // Simplified name
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$percentage% ($correct/$total)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage >= 70) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                            ModernGradientProgressBar(
                                progress = correct.toFloat() / total,
                                gradient = if (percentage >= 70) AccentGradient else WarningGradient,
                                showLabel = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedScoreCircle(
    score: Int,
    passed: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "score_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .background(
                color = if (passed) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFF44336).copy(alpha = 0.2f),
                shape = androidx.compose.foundation.shape.CircleShape
            )
    ) {
        Text(
            text = "$animatedScore%",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ExamReviewContent(
    state: ExamUiState.Finished,
    onCloseReview: () -> Unit,
    onToggleBookmark: (Int) -> Unit,
    language: String
) {
    val pagerState = rememberPagerState(pageCount = { state.questions.size })
    var showReviewDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            ModernGradientTopAppBar(
                title = stringResource(CommonR.string.exam_review_title),
                gradient = SecondaryGradient,
                onNavigationClick = onCloseReview,
                actions = {
                    Text(
                        text = "${pagerState.currentPage + 1}/${state.questions.size}",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(end = SpacingSmall)
                    )
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text(stringResource(CommonR.string.exam_dialog_all_questions), color = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val prevPage = pagerState.currentPage - 1
                            if (prevPage >= 0) {
                                pagerState.animateScrollToPage(prevPage)
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.38f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (pagerState.currentPage > 0) Color.White else Color.White.copy(alpha = 0.12f)
                    )
                ) {
                    Text(stringResource(CommonR.string.common_previous))
                }

                Button(
                    onClick = {
                        scope.launch {
                            val nextPage = pagerState.currentPage + 1
                            if (nextPage < state.questions.size) {
                                pagerState.animateScrollToPage(nextPage)
                            }
                        }
                    },
                    enabled = pagerState.currentPage < state.questions.size - 1
                ) {
                    Text(stringResource(CommonR.string.common_next))
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            val question = state.questions[page]
            val isBookmarked = question.isBookmarked

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.SuggestionChip(
                            onClick = {},
                            label = { Text(question.category.substringBefore("/")) },
                            colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )

                        androidx.compose.material3.IconButton(
                            onClick = { onToggleBookmark(question.id) }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Filled.Star else androidx.compose.material.icons.Icons.Outlined.Star,
                                tint = if (isBookmarked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(28.dp),
                                contentDescription = stringResource(CommonR.string.common_bookmark)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    QuestionCard(
                        question = question,
                        selectedOptionIndex = state.userAnswers[question.id],
                        onOptionSelected = {},
                        showFeedback = true,
                        language = language
                    )
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text(stringResource(CommonR.string.exam_dialog_all_questions)) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(state.questions) { index, question ->
                        val userAnswer = state.userAnswers[question.id]
                        val isCorrect = userAnswer == question.correctAnswerIndex
                        val isCurrent = index == pagerState.currentPage
                        
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .width(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                             Button(
                                 onClick = {
                                     scope.launch {
                                         pagerState.animateScrollToPage(index)
                                     }
                                     showReviewDialog = false
                                 },
                                 contentPadding = PaddingValues(0.dp),
                                 colors = ButtonDefaults.buttonColors(
                                     containerColor = if (isCurrent) MaterialTheme.colorScheme.primary 
                                                      else if (isCorrect) MaterialTheme.colorScheme.primaryContainer
                                                      else MaterialTheme.colorScheme.errorContainer,
                                     contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                                    else if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onErrorContainer
                                 )
                             ) {
                                 Text((index + 1).toString())
                             }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(stringResource(CommonR.string.common_close))
                }
            }
        )
    }
}

@Composable
private fun ExamScoreContent(
    state: ExamUiState.Finished,
    onBackClick: () -> Unit,
    onResetExam: () -> Unit,
    onReviewClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onBackClick) {
                    Text(stringResource(CommonR.string.common_back_to_home))
                }
                TextButton(onClick = onReviewClick) {
                    Text(stringResource(CommonR.string.exam_result_review))
                }
                Button(onClick = onResetExam) {
                    Text(stringResource(CommonR.string.exam_result_retake))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (state.passed) stringResource(CommonR.string.exam_score_passed) else stringResource(CommonR.string.exam_score_failed),
                style = MaterialTheme.typography.displayMedium,
                color = if (state.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${state.score}%",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Score: ${state.score} / 100",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (state.passed) stringResource(CommonR.string.exam_score_congrats) else stringResource(CommonR.string.exam_score_keep_practicing),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ExamReviewContent(
    state: ExamUiState.Finished,
    onCloseReview: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.questions.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Question ${pagerState.currentPage + 1}/${state.questions.size}",
                        color = Color.White
                    )
                },
                actions = {
                    TextButton(onClick = onCloseReview) {
                        Text(stringResource(CommonR.string.common_close), color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val prevPage = pagerState.currentPage - 1
                            if (prevPage >= 0) {
                                pagerState.animateScrollToPage(prevPage)
                            }
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Text(stringResource(CommonR.string.common_previous))
                }

                Button(
                    onClick = {
                        scope.launch {
                            val nextPage = pagerState.currentPage + 1
                            if (nextPage < state.questions.size) {
                                pagerState.animateScrollToPage(nextPage)
                            }
                        }
                    },
                    enabled = pagerState.currentPage < state.questions.size - 1
                ) {
                    Text(stringResource(CommonR.string.common_next))
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            val question = state.questions[page]
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                QuestionCard(
                    question = question,
                    selectedOptionIndex = state.userAnswers[question.id],
                    onOptionSelected = { },
                    showFeedback = true
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
