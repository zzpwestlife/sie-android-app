package com.example.sie.feature.exam

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.designsystem.component.QuestionCard
import com.example.sie.core.designsystem.component.GradientProgressIndicator
import com.example.sie.core.designsystem.component.GradientButton
import com.example.sie.core.designsystem.theme.SuccessGradient
import com.example.sie.core.designsystem.theme.ErrorGradient
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
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
        onFlagQuestion = viewModel::onFlagQuestion,
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
    onFlagQuestion: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onSubmitExam: () -> Unit,
    onBackClick: () -> Unit,
    onQuestionSelected: (Int) -> Unit,
    onResetExam: () -> Unit,
    language: String
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
                    onFlagQuestion = onFlagQuestion,
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
            TopAppBar(
                title = {
                    Text(
                        text = if (language == "zh") "模拟考试" else "Mock Exam",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Exam Rules Card
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (language == "zh") "考试规则" else "Exam Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.List,
                        title = if (language == "zh") "32 道题" else "32 Questions",
                        subtitle = if (language == "zh") "精选高频考题" else "Selected high-frequency questions"
                    )
                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.Notifications,
                        title = if (language == "zh") "30 分钟" else "30 Minutes",
                        subtitle = if (language == "zh") "共 0.5 小时" else "Total 0.5 hours"
                    )
                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        title = if (language == "zh") "答对 70% 及格" else "70% to Pass",
                        subtitle = if (language == "zh") "基于 32 道计分题目" else "Based on 32 scored questions"
                    )
                    IntroItem(
                        icon = androidx.compose.material.icons.Icons.Default.Star,
                        title = if (language == "zh") "标记功能" else "Flagging",
                        subtitle = if (language == "zh") "可标记不确定的题目，方便回顾检查" else "Flag uncertain questions for review"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (language == "zh") "开始考试" else "Start Exam",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun IntroItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.Top) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ExamInProgressContent(
    state: ExamUiState.InProgress,
    onAnswerSelected: (Int, Int) -> Unit,
    onFlagQuestion: (Int) -> Unit,
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
        topBar = {
            TopAppBar(
                navigationIcon = {
                    TextButton(onClick = { showReviewDialog = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "Review",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                         val timeColor = when {
                            state.timeLeftMillis < 1 * 60 * 1000 -> Color(0xFFF44336)
                            state.timeLeftMillis < 5 * 60 * 1000 -> Color(0xFFFFA500)
                            else -> Color.White
                        }

                        // Pulse animation when time is running out
                        val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = if (state.timeLeftMillis < 5 * 60 * 1000) 1.08f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "timer_pulse_scale"
                        )

                        Text(
                            text = formatTime(state.timeLeftMillis),
                            style = MaterialTheme.typography.titleMedium,
                            color = timeColor,
                            fontWeight = if (state.timeLeftMillis < 5 * 60 * 1000) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.scale(scale)
                        )
                    }
                },
                actions = {
                    val currentQuestion = state.questions.getOrNull(pagerState.currentPage)
                    if (currentQuestion != null) {
                        // Flag is now inside the card, but we can keep a submit button here or just time
                        // Keeping Submit here for accessibility
                        TextButton(onClick = { showSubmitDialog = true }) {
                            Text(
                                text = if (language == "zh") "提交" else "Submit",
                                color = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            scope.launch {
                                val prevPage = pagerState.currentPage - 1
                                if (prevPage >= 0) {
                                    onQuestionSelected(prevPage)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text("< " + (if (language == "zh") "上一题" else "Prev"))
                    }

                    Text(
                        text = "${pagerState.currentPage + 1} / ${state.questions.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    androidx.compose.material3.TextButton(
                        onClick = {
                            scope.launch {
                                val nextPage = pagerState.currentPage + 1
                                if (nextPage < state.questions.size) {
                                    onQuestionSelected(nextPage)
                                }
                            }
                        },
                        enabled = pagerState.currentPage < state.questions.size - 1
                    ) {
                        Text((if (language == "zh") "下一题" else "Next") + " >")
                    }
                }
                
                // Big Action Button
                val isLastQuestion = pagerState.currentPage == state.questions.size - 1
                Button(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isLastQuestion) (if (language == "zh") "提交试卷" else "Submit Exam") else (if (language == "zh") "下一题" else "Next Question"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
            // Use key to prevent recomposition issues if list changes (though it shouldn't in exam)
            // But importantly, pass language to QuestionCard
            val question = state.questions[page]
            // Access the question by index from state to be safe
            
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                
                Column {
                    // Category Tag
                    androidx.compose.material3.SuggestionChip(
                        onClick = {},
                        label = { Text(question.category.substringBefore("/")) }, // Show English part mostly or simplify
                        colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = null
                    )
                    
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
                
                // Overlay Bookmark button on top right of the card area
                val isBookmarked = question.isBookmarked
                androidx.compose.material3.IconButton(
                    onClick = {
                        onToggleBookmark(question.id)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 24.dp) // Adjust based on layout
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (isBookmarked) androidx.compose.material.icons.Icons.Filled.Star else androidx.compose.material.icons.Icons.Outlined.Star,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Review Answers") },
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
                        val isFlagged = state.flaggedQuestions.contains(question.id)
                        
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
                                                      else if (isFlagged) MaterialTheme.colorScheme.tertiaryContainer // Flagged
                                                      else if (isAnswered) MaterialTheme.colorScheme.secondaryContainer
                                                      else MaterialTheme.colorScheme.surfaceVariant,
                                     contentColor = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                                    else if (isFlagged) MaterialTheme.colorScheme.onTertiaryContainer
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
                    Text("Close")
                }
            }
        )
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Submit Exam?") },
            text = { Text("Are you sure you want to submit? You have ${state.questions.size - state.userAnswers.size} unanswered questions.") },
            confirmButton = {
                Button(onClick = {
                    showSubmitDialog = false
                    onSubmitExam()
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) {
                    Text("Cancel")
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
            language = language
        )
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (language == "zh") "考试结果" else "Exam Result",
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        androidx.compose.material3.IconButton(onClick = onBackClick) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Score Circle and Status
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pass/Fail Icon
                        androidx.compose.material3.Icon(
                            imageVector = if (state.passed) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = null,
                            tint = if (state.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = if (state.passed) (if (language == "zh") "恭喜通过！" else "Passed!") else (if (language == "zh") "继续加油！" else "Keep Trying!"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
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
                                label = if (language == "zh") "正确" else "Correct",
                                value = "${(state.score * state.totalQuestions) / 100}",
                                color = Color(0xFF4CAF50)
                            )
                            // Vertical Divider
                            androidx.compose.material3.Divider(modifier = Modifier.height(40.dp).width(1.dp))
                            StatItem(
                                label = if (language == "zh") "错误" else "Incorrect",
                                value = "${state.totalQuestions - (state.score * state.totalQuestions) / 100}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GradientButton(
                        text = if (language == "zh") "查看答案解析" else "Review Answers",
                        gradient = PrimaryGradient,
                        onClick = { isReviewing = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    GradientButton(
                        text = if (language == "zh") "重新考试" else "Retake Exam",
                        gradient = SecondaryGradient,
                        onClick = onResetExam,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (language == "zh") "返回首页" else "Back to Home",
                            color = Color.White
                        )
                    }
                }

                // Category Breakdown
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (language == "zh") "板块正确率" else "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$percentage% ($correct/$total)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (percentage >= 70) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                )
                            }
                            GradientProgressIndicator(
                                progress = correct.toFloat() / total,
                                gradient = if (percentage >= 70) SuccessGradient else ErrorGradient,
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = Color.White.copy(alpha = 0.2f),
                                animate = true
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
    language: String
) {
    val pagerState = rememberPagerState(pageCount = { state.questions.size })
    var showReviewDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Review: Question ${pagerState.currentPage + 1}/${state.questions.size}",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onCloseReview) {
                        Text("Close", color = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text("All Questions", color = Color.White)
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
                    Text("Previous")
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
                    Text("Next")
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
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
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

    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("All Questions") },
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
                    Text("Close")
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
                    Text("Back to Home")
                }
                TextButton(onClick = onReviewClick) {
                    Text("Review Answers")
                }
                Button(onClick = onResetExam) {
                    Text("Retake Exam")
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
                text = if (state.passed) "Passed!" else "Failed",
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
                text = if (state.passed) "Congratulations! You are ready for the real exam." else "Keep practicing. You need 70% to pass.",
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
                        Text("Close", color = Color.White)
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
                    Text("Previous")
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
                    Text("Next")
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
