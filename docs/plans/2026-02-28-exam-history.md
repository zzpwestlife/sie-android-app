# Exam History Feature Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Persist per-question exam answers and build exam history browsing + detail review UI integrated into the Stats screen.

**Architecture:** New `exam_answers` table with foreign key to `exam_results`. Extend ExamResultDao, ExamRepository, ExamViewModel for answer persistence. Two new screens (ExamHistoryScreen, ExamDetailScreen) in the existing `feature/exam` module. Stats screen gains a clickable entry point.

**Tech Stack:** Room (migration 13→14), Hilt DI, Jetpack Compose, Kotlin Coroutines/Flow, MVVM

---

## Phase 1: Data Layer

### Task 1.1: Create ExamAnswer domain model

**Files:**
- Create: `core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt`

**Step 1: Create ExamAnswer model**

```kotlin
package com.example.sie.core.model

data class ExamAnswer(
    val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)
```

**Step 2: Commit**

```bash
git add core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt
git commit -m "feat(model): add ExamAnswer domain model"
```

---

### Task 1.2: Create ExamAnswerEntity with Room mapping

**Files:**
- Create: `core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt`

**Step 1: Create ExamAnswerEntity**

```kotlin
package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sie.core.model.ExamAnswer

@Entity(
    tableName = "exam_answers",
    foreignKeys = [ForeignKey(
        entity = ExamResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["examResultId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["examResultId"])]
)
data class ExamAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)

fun ExamAnswerEntity.asExternalModel() = ExamAnswer(
    id = id,
    examResultId = examResultId,
    questionId = questionId,
    selectedOptionIndex = selectedOptionIndex,
    isCorrect = isCorrect,
    isFlagged = isFlagged
)

fun ExamAnswer.asEntity() = ExamAnswerEntity(
    id = id,
    examResultId = examResultId,
    questionId = questionId,
    selectedOptionIndex = selectedOptionIndex,
    isCorrect = isCorrect,
    isFlagged = isFlagged
)
```

**Step 2: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt
git commit -m "feat(database): add ExamAnswerEntity with Room mapping"
```

---

### Task 1.3: Create database migration 13→14

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt`
- Modify: `core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt`

**Step 1: Add MIGRATION_13_14 to AppDatabase.kt**

Add the following after `MIGRATION_12_13`:

```kotlin
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exam_answers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `examResultId` INTEGER NOT NULL,
                `questionId` INTEGER NOT NULL,
                `selectedOptionIndex` INTEGER NOT NULL,
                `isCorrect` INTEGER NOT NULL DEFAULT 0,
                `isFlagged` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`examResultId`) REFERENCES `exam_results`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_exam_answers_result` ON `exam_answers`(`examResultId`)"
        )
    }
}
```

Update `@Database` annotation: bump version from 13 to 14, add `ExamAnswerEntity::class` to entities array.

**Step 2: Register migration in DatabaseModule.kt**

Add import: `import com.example.sie.core.database.MIGRATION_13_14`

Change `.addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)` to:
`.addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)`

**Step 3: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt
git commit -m "feat(database): add migration 13→14 for exam_answers table"
```

---

### Task 1.4: Extend ExamResultDao with new queries

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/dao/ExamResultDao.kt`

**Step 1: Add new DAO methods**

Add the following to `ExamResultDao`:

```kotlin
import androidx.room.Transaction
import com.example.sie.core.database.model.ExamAnswerEntity

@Insert
suspend fun insertExamResultAndGetId(examResult: ExamResultEntity): Long

@Insert
suspend fun insertExamAnswers(answers: List<ExamAnswerEntity>)

@Query("SELECT * FROM exam_answers WHERE examResultId = :examResultId")
fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswerEntity>>

@Query("SELECT * FROM exam_results WHERE id = :id")
fun getExamResultById(id: Int): Flow<ExamResultEntity?>
```

**Step 2: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/dao/ExamResultDao.kt
git commit -m "feat(database): extend ExamResultDao with answer persistence queries"
```

---

### Task 1.5: Extend ExamRepository interface and implementation

**Files:**
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/ExamRepository.kt`
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/OfflineExamRepository.kt`

**Step 1: Add new interface methods to ExamRepository.kt**

```kotlin
import com.example.sie.core.model.ExamAnswer

// Add to interface:
suspend fun saveExamResultWithAnswers(examResult: ExamResult, answers: List<ExamAnswer>)
fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswer>>
fun getExamResultById(id: Int): Flow<ExamResult?>
```

**Step 2: Implement in OfflineExamRepository.kt**

```kotlin
import com.example.sie.core.database.model.ExamAnswerEntity
import com.example.sie.core.database.model.asExternalModel as answerAsExternalModel
import com.example.sie.core.model.ExamAnswer

override suspend fun saveExamResultWithAnswers(examResult: ExamResult, answers: List<ExamAnswer>) {
    val resultId = examResultDao.insertExamResultAndGetId(
        ExamResultEntity(
            date = examResult.date,
            score = examResult.score,
            totalQuestions = examResult.totalQuestions,
            correctCount = examResult.correctCount
        )
    )
    val answerEntities = answers.map { answer ->
        ExamAnswerEntity(
            examResultId = resultId.toInt(),
            questionId = answer.questionId,
            selectedOptionIndex = answer.selectedOptionIndex,
            isCorrect = answer.isCorrect,
            isFlagged = answer.isFlagged
        )
    }
    examResultDao.insertExamAnswers(answerEntities)
}

override fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswer>> =
    examResultDao.getExamAnswers(examResultId).map { entities ->
        entities.map { it.asExternalModel() }
    }

override fun getExamResultById(id: Int): Flow<ExamResult?> =
    examResultDao.getExamResultById(id).map { entity ->
        entity?.asExternalModel()
    }
```

Note: The import for `asExternalModel` on `ExamAnswerEntity` needs to be aliased since `ExamResultEntity.asExternalModel` is already imported. Use:
```kotlin
import com.example.sie.core.database.model.asExternalModel as answerAsExternalModel
```
Then use `it.answerAsExternalModel()` or restructure imports. Alternatively, just use inline conversion:
```kotlin
entities.map { ExamAnswer(it.id, it.examResultId, it.questionId, it.selectedOptionIndex, it.isCorrect, it.isFlagged) }
```

**Step 3: Commit**

```bash
git add core/data/src/main/java/com/example/sie/core/data/repository/ExamRepository.kt core/data/src/main/java/com/example/sie/core/data/repository/OfflineExamRepository.kt
git commit -m "feat(data): extend ExamRepository with answer persistence methods"
```

---

### Task 1.6: Modify ExamViewModel to save answers on submit

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamViewModel.kt`

**Step 1: Replace `saveResult()` call in `submitExam()`**

Currently `submitExam()` at line 226 calls `saveResult(score, state.questions.size, correctCount)`. Replace the `saveResult` method and its call:

Replace the `saveResult` method (lines 242-254) with:

```kotlin
private fun saveResultWithAnswers(
    score: Int,
    totalQuestions: Int,
    correctCount: Int,
    questions: List<Question>,
    userAnswers: Map<Int, Int>,
    flaggedQuestions: Set<Int>
) {
    viewModelScope.launch {
        val answers = questions.map { question ->
            ExamAnswer(
                examResultId = 0, // will be set by repository
                questionId = question.id,
                selectedOptionIndex = userAnswers[question.id] ?: -1,
                isCorrect = userAnswers[question.id] == question.correctAnswerIndex,
                isFlagged = question.id in flaggedQuestions
            )
        }
        examRepository.saveExamResultWithAnswers(
            ExamResult(
                id = 0,
                date = System.currentTimeMillis(),
                score = score,
                totalQuestions = totalQuestions,
                correctCount = correctCount
            ),
            answers
        )
    }
}
```

In `submitExam()`, replace `saveResult(score, state.questions.size, correctCount)` with:
```kotlin
saveResultWithAnswers(score, state.questions.size, correctCount, state.questions, state.userAnswers, state.flaggedQuestions)
```

Add import: `import com.example.sie.core.model.ExamAnswer`

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamViewModel.kt
git commit -m "feat(exam): save per-question answers on exam submission"
```

---

## Phase 2: String Resources

### Task 2.1: Add exam history string resources

**Files:**
- Modify: `core/common/src/main/res/values/strings.xml`
- Modify: `core/common/src/main/res/values-zh/strings.xml`

**Step 1: Add English strings**

Add after the `<!-- Stats Screen -->` section (before `<!-- Settings Screen -->`):

```xml
<!-- Exam History -->
<string name="exam_history_title">Exam History</string>
<string name="exam_history_empty">No exam records yet</string>
<string name="exam_history_view_all">View All History</string>
<string name="exam_history_total_exams">Total Exams: %d</string>
<string name="exam_history_best_score">Best: %d%%</string>
<string name="exam_history_pass_rate">Pass Rate: %d%%</string>
<string name="exam_detail_title">Exam Detail</string>
<string name="exam_detail_filter_all">All</string>
<string name="exam_detail_filter_wrong">Wrong Only</string>
<string name="exam_detail_question_index">Question %1$d / %2$d</string>
<string name="exam_detail_your_answer">Your Answer</string>
<string name="exam_detail_correct_answer">Correct Answer</string>
<string name="exam_detail_flagged">Flagged</string>
<string name="exam_detail_not_answered">Not Answered</string>
```

**Step 2: Add Chinese strings**

Add corresponding section in values-zh/strings.xml:

```xml
<!-- Exam History -->
<string name="exam_history_title">考试历史</string>
<string name="exam_history_empty">暂无考试记录</string>
<string name="exam_history_view_all">查看全部历史</string>
<string name="exam_history_total_exams">考试次数: %d</string>
<string name="exam_history_best_score">最高分: %d%%</string>
<string name="exam_history_pass_rate">通过率: %d%%</string>
<string name="exam_detail_title">考试详情</string>
<string name="exam_detail_filter_all">全部</string>
<string name="exam_detail_filter_wrong">仅错题</string>
<string name="exam_detail_question_index">第 %1$d / %2$d 题</string>
<string name="exam_detail_your_answer">你的答案</string>
<string name="exam_detail_correct_answer">正确答案</string>
<string name="exam_detail_flagged">已标记</string>
<string name="exam_detail_not_answered">未作答</string>
```

**Step 3: Commit**

```bash
git add core/common/src/main/res/values/strings.xml core/common/src/main/res/values-zh/strings.xml
git commit -m "feat(i18n): add exam history bilingual string resources"
```

---

## Phase 3: Exam History Screen (List)

### Task 3.1: Create ExamHistoryViewModel

**Files:**
- Create: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryViewModel.kt`

**Step 1: Create ViewModel with UiState**

```kotlin
package com.example.sie.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExamHistoryViewModel @Inject constructor(
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<ExamHistoryUiState> = examRepository.getExamResults()
        .map { results ->
            if (results.isEmpty()) {
                ExamHistoryUiState.Empty
            } else {
                ExamHistoryUiState.Success(results = results)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExamHistoryUiState.Loading
        )
}

sealed interface ExamHistoryUiState {
    data object Loading : ExamHistoryUiState
    data object Empty : ExamHistoryUiState
    data class Success(val results: List<ExamResult>) : ExamHistoryUiState
}
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryViewModel.kt
git commit -m "feat(exam): create ExamHistoryViewModel"
```

---

### Task 3.2: Create ExamHistoryScreen

**Files:**
- Create: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryScreen.kt`

**Step 1: Create the screen composable**

```kotlin
package com.example.sie.feature.exam

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.designsystem.theme.PrimaryGradient
import com.example.sie.core.designsystem.theme.SecondaryGradient
import com.example.sie.core.designsystem.theme.SuccessGradient
import com.example.sie.core.designsystem.theme.ErrorGradient
import com.example.sie.core.model.ExamResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExamHistoryRoute(
    onBackClick: () -> Unit,
    onExamClick: (Int) -> Unit,
    viewModel: ExamHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExamHistoryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onExamClick = onExamClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamHistoryScreen(
    uiState: ExamHistoryUiState,
    onBackClick: () -> Unit,
    onExamClick: (Int) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(CommonR.string.exam_history_title),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
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
                .padding(paddingValues)
        ) {
            when (uiState) {
                ExamHistoryUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                ExamHistoryUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(CommonR.string.exam_history_empty),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is ExamHistoryUiState.Success -> {
                    ExamHistoryContent(
                        results = uiState.results,
                        onExamClick = onExamClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamHistoryContent(
    results: List<ExamResult>,
    onExamClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card
        item {
            val totalExams = results.size
            val bestScore = results.maxOfOrNull { it.score } ?: 0
            val passCount = results.count { it.score >= 70 }
            val passRate = if (totalExams > 0) (passCount * 100) / totalExams else 0

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = PrimaryGradient
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStatItem(
                        label = stringResource(CommonR.string.exam_history_total_exams, totalExams),
                        value = totalExams.toString()
                    )
                    SummaryStatItem(
                        label = stringResource(CommonR.string.exam_history_best_score, bestScore),
                        value = "$bestScore%"
                    )
                    SummaryStatItem(
                        label = stringResource(CommonR.string.exam_history_pass_rate, passRate),
                        value = "$passRate%"
                    )
                }
            }
        }

        items(results, key = { it.id }) { result ->
            val passed = result.score >= 70
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                gradient = if (passed) SuccessGradient else ErrorGradient,
                onClick = { onExamClick(result.id) }
            ) {
                ExamHistoryItemContent(result = result, passed = passed)
            }
        }
    }
}

@Composable
private fun SummaryStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ExamHistoryItemContent(result: ExamResult, passed: Boolean) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateFormat.format(Date(result.date)),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.correctCount}/${result.totalQuestions}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${result.score}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryScreen.kt
git commit -m "feat(exam): create ExamHistoryScreen with summary and list"
```

---

## Phase 4: Exam Detail Screen (Review)

### Task 4.1: Create ExamDetailViewModel

**Files:**
- Create: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailViewModel.kt`

**Step 1: Create ViewModel**

```kotlin
package com.example.sie.feature.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import com.example.sie.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val examResultId: Int = checkNotNull(savedStateHandle["examResultId"])

    private val _uiState = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Loading)
    val uiState: StateFlow<ExamDetailUiState> = _uiState.asStateFlow()

    init {
        loadExamDetail()
    }

    private fun loadExamDetail() {
        viewModelScope.launch {
            try {
                val result = examRepository.getExamResultById(examResultId).first()
                val answers = examRepository.getExamAnswers(examResultId).first()

                if (result == null || answers.isEmpty()) {
                    _uiState.value = ExamDetailUiState.Error
                    return@launch
                }

                // Load questions by IDs
                val questionIds = answers.map { it.questionId }
                val allQuestions = questionRepository.getAllQuestionsList()
                val questionsMap = allQuestions.associateBy { it.id }
                val questions = questionIds.mapNotNull { questionsMap[it] }

                _uiState.value = ExamDetailUiState.Success(
                    examResult = result,
                    answers = answers,
                    questions = questions
                )
            } catch (e: Exception) {
                _uiState.value = ExamDetailUiState.Error
            }
        }
    }

    fun toggleFilter() {
        _uiState.value.let { state ->
            if (state is ExamDetailUiState.Success) {
                _uiState.value = state.copy(filterWrongOnly = !state.filterWrongOnly)
            }
        }
    }
}

sealed interface ExamDetailUiState {
    data object Loading : ExamDetailUiState
    data object Error : ExamDetailUiState
    data class Success(
        val examResult: ExamResult,
        val answers: List<ExamAnswer>,
        val questions: List<Question>,
        val filterWrongOnly: Boolean = false
    ) : ExamDetailUiState {
        val filteredAnswers: List<ExamAnswer>
            get() = if (filterWrongOnly) answers.filter { !it.isCorrect } else answers

        val filteredQuestions: List<Question>
            get() {
                val filteredIds = filteredAnswers.map { it.questionId }.toSet()
                return questions.filter { it.id in filteredIds }
            }
    }
}
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailViewModel.kt
git commit -m "feat(exam): create ExamDetailViewModel with wrong-only filter"
```

---

### Task 4.2: Create ExamDetailScreen

**Files:**
- Create: `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt`

**Step 1: Create the detail screen**

```kotlin
package com.example.sie.feature.exam

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sie.core.common.R as CommonR
import com.example.sie.core.designsystem.component.QuestionCard
import com.example.sie.core.model.ExamAnswer
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
    ExamDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onToggleFilter = viewModel::toggleFilter
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamDetailScreen(
    uiState: ExamDetailUiState,
    onBackClick: () -> Unit,
    onToggleFilter: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(CommonR.string.exam_detail_title),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
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
                .padding(paddingValues)
        ) {
            when (uiState) {
                ExamDetailUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                ExamDetailUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(CommonR.string.common_error),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is ExamDetailUiState.Success -> {
                    ExamDetailContent(
                        state = uiState,
                        onToggleFilter = onToggleFilter
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExamDetailContent(
    state: ExamDetailUiState.Success,
    onToggleFilter: () -> Unit
) {
    val filteredQuestions = state.filteredQuestions
    val filteredAnswers = state.filteredAnswers
    val answersMap = filteredAnswers.associateBy { it.questionId }

    if (filteredQuestions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (state.filterWrongOnly) stringResource(CommonR.string.exam_result_correct)
                       else stringResource(CommonR.string.exam_history_empty),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { filteredQuestions.size })
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        // Header info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dateFormat.format(Date(state.examResult.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "${state.examResult.score}% · ${state.examResult.correctCount}/${state.examResult.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !state.filterWrongOnly,
                    onClick = { if (state.filterWrongOnly) onToggleFilter() },
                    label = { Text(stringResource(CommonR.string.exam_detail_filter_all)) }
                )
                FilterChip(
                    selected = state.filterWrongOnly,
                    onClick = { if (!state.filterWrongOnly) onToggleFilter() },
                    label = { Text(stringResource(CommonR.string.exam_detail_filter_wrong)) }
                )
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
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Question pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Correct/Incorrect badge
                        if (answer != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (answer.isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = if (answer.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (answer.isCorrect) stringResource(CommonR.string.exam_result_correct)
                                           else stringResource(CommonR.string.exam_result_incorrect),
                                    color = if (answer.isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Flagged indicator
                        if (answer?.isFlagged == true) {
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

                    Spacer(modifier = Modifier.height(8.dp))

                    QuestionCard(
                        question = question,
                        selectedOptionIndex = answer?.selectedOptionIndex,
                        onOptionSelected = {},
                        showFeedback = true,
                        showExplanation = true
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
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    scope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = pagerState.currentPage > 0,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.38f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (pagerState.currentPage > 0) Color.White else Color.White.copy(alpha = 0.12f)
                )
            ) {
                Text(stringResource(CommonR.string.common_previous))
            }

            androidx.compose.material3.Button(
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
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt
git commit -m "feat(exam): create ExamDetailScreen with pager and wrong-only filter"
```

---

## Phase 5: Navigation Integration

### Task 5.1: Add navigation routes for exam history and detail

**Files:**
- Modify: `feature/exam/src/main/java/com/example/sie/feature/exam/navigation/ExamNavigation.kt`

**Step 1: Add new routes and composable registrations**

Add to the file:

```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sie.feature.exam.ExamHistoryRoute
import com.example.sie.feature.exam.ExamDetailRoute

const val examHistoryRoute = "exam_history_route"
const val examDetailRoute = "exam_detail_route/{examResultId}"

fun NavController.navigateToExamHistory(navOptions: NavOptions? = null) {
    this.navigate(examHistoryRoute, navOptions)
}

fun NavController.navigateToExamDetail(examResultId: Int, navOptions: NavOptions? = null) {
    this.navigate("exam_detail_route/$examResultId", navOptions)
}

fun NavGraphBuilder.examHistoryScreen(
    onBackClick: () -> Unit,
    onExamClick: (Int) -> Unit
) {
    composable(route = examHistoryRoute) {
        ExamHistoryRoute(
            onBackClick = onBackClick,
            onExamClick = onExamClick
        )
    }
}

fun NavGraphBuilder.examDetailScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = examDetailRoute,
        arguments = listOf(navArgument("examResultId") { type = NavType.IntType })
    ) {
        ExamDetailRoute(onBackClick = onBackClick)
    }
}
```

**Step 2: Commit**

```bash
git add feature/exam/src/main/java/com/example/sie/feature/exam/navigation/ExamNavigation.kt
git commit -m "feat(navigation): add exam history and detail routes"
```

---

### Task 5.2: Integrate into SieNavHost

**Files:**
- Modify: `app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt`

**Step 1: Add imports and composable registrations**

Add imports:
```kotlin
import com.example.sie.feature.exam.navigation.examHistoryScreen
import com.example.sie.feature.exam.navigation.examDetailScreen
import com.example.sie.feature.exam.navigation.navigateToExamHistory
import com.example.sie.feature.exam.navigation.navigateToExamDetail
```

Add inside the `NavHost` block (after `examScreen`):
```kotlin
examHistoryScreen(
    onBackClick = { navController.popBackStack() },
    onExamClick = { examResultId -> navController.navigateToExamDetail(examResultId) }
)
examDetailScreen(
    onBackClick = { navController.popBackStack() }
)
```

**Step 2: Update statsScreen to accept navigation callbacks**

Change `statsScreen()` to:
```kotlin
statsScreen(
    onExamHistoryClick = { navController.navigateToExamHistory() },
    onExamResultClick = { examResultId -> navController.navigateToExamDetail(examResultId) }
)
```

**Step 3: Commit**

```bash
git add app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt
git commit -m "feat(navigation): integrate exam history routes into SieNavHost"
```

---

### Task 5.3: Update StatsScreen with exam history entry

**Files:**
- Modify: `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt`
- Modify: `feature/stats/src/main/java/com/example/sie/feature/stats/navigation/StatsNavigation.kt`

**Step 1: Update StatsNavigation.kt to accept callbacks**

Replace `statsScreen()` function:

```kotlin
fun NavGraphBuilder.statsScreen(
    onExamHistoryClick: () -> Unit = {},
    onExamResultClick: (Int) -> Unit = {}
) {
    composable(route = statsRoute) {
        StatsRoute(
            onExamHistoryClick = onExamHistoryClick,
            onExamResultClick = onExamResultClick
        )
    }
}
```

**Step 2: Update StatsScreen.kt**

Update `StatsRoute` composable signature:
```kotlin
@Composable
fun StatsRoute(
    onExamHistoryClick: () -> Unit = {},
    onExamResultClick: (Int) -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    StatsScreen(
        uiState = uiState,
        onExamHistoryClick = onExamHistoryClick,
        onExamResultClick = onExamResultClick
    )
}
```

Update `StatsScreen` composable signature:
```kotlin
@Composable
internal fun StatsScreen(
    uiState: StatsUiState,
    onExamHistoryClick: () -> Unit = {},
    onExamResultClick: (Int) -> Unit = {}
)
```

In the `Success` branch, add a "View All History" button between the score chart and the recent results section. Also make each result card clickable:

After the score chart `GlassCard` and its `Spacer`, before "Recent Results" text, add:

```kotlin
// View All History button
GlassCard(
    modifier = Modifier.fillMaxWidth(),
    gradient = SecondaryGradient,
    onClick = onExamHistoryClick
) {
    Text(
        text = stringResource(CommonR.string.exam_history_view_all),
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

Spacer(modifier = Modifier.height(24.dp))
```

Make each result item clickable by wrapping the GlassCard with onClick:

```kotlin
GlassCard(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    gradient = SecondaryGradient,
    onClick = { onExamResultClick(result.id) }  // ADD THIS
) {
    ExamResultItemContent(result)
}
```

Add import: `import androidx.compose.ui.text.style.TextAlign`

**Step 3: Commit**

```bash
git add feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt feature/stats/src/main/java/com/example/sie/feature/stats/navigation/StatsNavigation.kt
git commit -m "feat(stats): add exam history entry point to StatsScreen"
```

---

## Phase 6: Unit Tests

### Task 6.1: Test ExamHistoryViewModel

**Files:**
- Create: `feature/exam/src/test/java/com/example/sie/feature/exam/ExamHistoryViewModelTest.kt`

**Step 1: Write tests**

```kotlin
package com.example.sie.feature.exam

import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var examRepository: ExamRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        examRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        every { examRepository.getExamResults() } returns flowOf(emptyList())
        val viewModel = ExamHistoryViewModel(examRepository)
        assertEquals(ExamHistoryUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty results produces Empty state`() = runTest {
        every { examRepository.getExamResults() } returns flowOf(emptyList())
        val viewModel = ExamHistoryViewModel(examRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ExamHistoryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `non-empty results produces Success state`() = runTest {
        val results = listOf(
            ExamResult(id = 1, date = 1000L, score = 80, totalQuestions = 32, correctCount = 26)
        )
        every { examRepository.getExamResults() } returns flowOf(results)
        val viewModel = ExamHistoryViewModel(examRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is ExamHistoryUiState.Success)
        assertEquals(1, (state as ExamHistoryUiState.Success).results.size)
    }
}
```

**Step 2: Run tests**

```bash
./gradlew :feature:exam:testDebugUnitTest --tests "com.example.sie.feature.exam.ExamHistoryViewModelTest" -q
```

Expected: 3 tests PASS

**Step 3: Commit**

```bash
git add feature/exam/src/test/java/com/example/sie/feature/exam/ExamHistoryViewModelTest.kt
git commit -m "test(exam): add ExamHistoryViewModel unit tests"
```

---

### Task 6.2: Test ExamDetailViewModel

**Files:**
- Create: `feature/exam/src/test/java/com/example/sie/feature/exam/ExamDetailViewModelTest.kt`

**Step 1: Write tests**

```kotlin
package com.example.sie.feature.exam

import androidx.lifecycle.SavedStateHandle
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import com.example.sie.core.model.Question
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var examRepository: ExamRepository
    private lateinit var questionRepository: QuestionRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testResult = ExamResult(id = 1, date = 1000L, score = 75, totalQuestions = 2, correctCount = 1)
    private val testAnswers = listOf(
        ExamAnswer(id = 1, examResultId = 1, questionId = 10, selectedOptionIndex = 0, isCorrect = true),
        ExamAnswer(id = 2, examResultId = 1, questionId = 20, selectedOptionIndex = 1, isCorrect = false)
    )
    private val testQuestions = listOf(
        Question(id = 10, content = "Q1", options = listOf("A", "B"), correctAnswerIndex = 0, explanation = "E1", category = "C1"),
        Question(id = 20, content = "Q2", options = listOf("A", "B"), correctAnswerIndex = 0, explanation = "E2", category = "C1")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        examRepository = mockk()
        questionRepository = mockk()
        savedStateHandle = SavedStateHandle(mapOf("examResultId" to 1))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads exam detail successfully`() = runTest {
        every { examRepository.getExamResultById(1) } returns flowOf(testResult)
        every { examRepository.getExamAnswers(1) } returns flowOf(testAnswers)
        coEvery { questionRepository.getAllQuestionsList() } returns testQuestions

        val viewModel = ExamDetailViewModel(savedStateHandle, examRepository, questionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ExamDetailUiState.Success)
        assertEquals(2, (state as ExamDetailUiState.Success).questions.size)
        assertEquals(2, state.answers.size)
    }

    @Test
    fun `filter toggle shows only wrong answers`() = runTest {
        every { examRepository.getExamResultById(1) } returns flowOf(testResult)
        every { examRepository.getExamAnswers(1) } returns flowOf(testAnswers)
        coEvery { questionRepository.getAllQuestionsList() } returns testQuestions

        val viewModel = ExamDetailViewModel(savedStateHandle, examRepository, questionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFilter()
        val state = viewModel.uiState.value as ExamDetailUiState.Success
        assertTrue(state.filterWrongOnly)
        assertEquals(1, state.filteredAnswers.size)
        assertEquals(20, state.filteredAnswers[0].questionId)
    }

    @Test
    fun `null result produces Error state`() = runTest {
        every { examRepository.getExamResultById(1) } returns flowOf(null)
        every { examRepository.getExamAnswers(1) } returns flowOf(emptyList())
        coEvery { questionRepository.getAllQuestionsList() } returns emptyList()

        val viewModel = ExamDetailViewModel(savedStateHandle, examRepository, questionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ExamDetailUiState.Error, viewModel.uiState.value)
    }
}
```

**Step 2: Run tests**

```bash
./gradlew :feature:exam:testDebugUnitTest --tests "com.example.sie.feature.exam.ExamDetailViewModelTest" -q
```

Expected: 3 tests PASS

**Step 3: Commit**

```bash
git add feature/exam/src/test/java/com/example/sie/feature/exam/ExamDetailViewModelTest.kt
git commit -m "test(exam): add ExamDetailViewModel unit tests"
```

---

## Phase 7: Final Verification

### Task 7.1: Run all exam module tests

**Step 1: Run full test suite**

```bash
./gradlew :feature:exam:testDebugUnitTest -q
```

Expected: All tests pass

**Step 2: Build the full project**

```bash
./gradlew assembleDebug 2>&1 | tail -5
```

Expected: BUILD SUCCESSFUL

---

### Task 7.2: Final commit and summary

**Step 1: Check git status**

```bash
git status
git log --oneline -15
```

**Step 2: Verify all changes are committed**

Ensure no unstaged changes remain.

---

## File Change Summary

### New Files (6)
1. `core/model/src/main/java/com/example/sie/core/model/ExamAnswer.kt`
2. `core/database/src/main/java/com/example/sie/core/database/model/ExamAnswerEntity.kt`
3. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryViewModel.kt`
4. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamHistoryScreen.kt`
5. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailViewModel.kt`
6. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamDetailScreen.kt`

### Modified Files (9)
1. `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt` — Migration 13→14, entity registration
2. `core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt` — Register migration
3. `core/database/src/main/java/com/example/sie/core/database/dao/ExamResultDao.kt` — New query methods
4. `core/data/src/main/java/com/example/sie/core/data/repository/ExamRepository.kt` — New interface methods
5. `core/data/src/main/java/com/example/sie/core/data/repository/OfflineExamRepository.kt` — Implementations
6. `feature/exam/src/main/java/com/example/sie/feature/exam/ExamViewModel.kt` — Save answers on submit
7. `feature/exam/src/main/java/com/example/sie/feature/exam/navigation/ExamNavigation.kt` — New routes
8. `feature/stats/src/main/java/com/example/sie/feature/stats/StatsScreen.kt` — History entry point
9. `feature/stats/src/main/java/com/example/sie/feature/stats/navigation/StatsNavigation.kt` — Navigation callbacks
10. `app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt` — Register new routes
11. `core/common/src/main/res/values/strings.xml` — English strings
12. `core/common/src/main/res/values-zh/strings.xml` — Chinese strings

### Test Files (2)
1. `feature/exam/src/test/java/com/example/sie/feature/exam/ExamHistoryViewModelTest.kt`
2. `feature/exam/src/test/java/com/example/sie/feature/exam/ExamDetailViewModelTest.kt`
