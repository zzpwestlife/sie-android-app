# Chapter-Based Learning Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add chapter-based learning feature allowing users to select multiple chapters and study questions with smart sorting (wrong questions first, then unstudied, then by error count).

**Architecture:** Lightweight chapter filter approach - new feature module `feature/chapter` with ChapterSelectionScreen, extends existing StudyViewModel with category filtering, adds `lastStudiedAt` field to QuestionEntity.

**Tech Stack:** Kotlin, Jetpack Compose, Room Database (migration 12→13), Hilt DI, Kotlin Coroutines/Flow, Material3

---

## Phase 1: Data Layer (Database & Repository)

### Task 1.1: Add lastStudiedAt Field to QuestionEntity

**Goal:** Extend database schema to track when each question was last studied

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/model/QuestionEntity.kt:17-28`
- Modify: `core/model/src/main/java/com/example/sie/core/model/Question.kt:3-13`

**Step 1: Add lastStudiedAt to QuestionEntity**

```kotlin
@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["isBookmarked", "category", "id"], name = "index_bookmark_category"),
        Index(value = ["isWrong", "wrongCount", "category", "id"], name = "index_wrong_category"),
        Index(value = ["category", "lastStudiedAt", "id"], name = "index_category_studied")  // NEW
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val options: String,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,
    val lastStudiedAt: Long? = null  // NEW: Timestamp in milliseconds
)
```

**Step 2: Update asExternalModel mapper**

```kotlin
fun QuestionEntity.asExternalModel() = Question(
    id = id,
    content = content,
    options = try {
        val jsonArray = JSONArray(options)
        List(jsonArray.length()) { jsonArray.getString(it) }
    } catch (e: JSONException) {
        listOf()
    },
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    category = category,
    isBookmarked = isBookmarked,
    isWrong = isWrong,
    wrongCount = wrongCount,
    lastStudiedAt = lastStudiedAt  // NEW
)
```

**Step 3: Add lastStudiedAt to Question model**

```kotlin
data class Question(
    val id: Int,
    val content: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,
    val lastStudiedAt: Long? = null  // NEW
) {
    // ... existing methods
}
```

**Step 4: Verify compilation**

```bash
./gradlew :core:database:build :core:model:build
```

Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/model/QuestionEntity.kt \
        core/model/src/main/java/com/example/sie/core/model/Question.kt
git commit -m "feat(database): add lastStudiedAt field to Question model"
```

---

### Task 1.2: Create Database Migration 12→13

**Goal:** Add migration script to alter questions table

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt:57-64`

**Step 1: Add MIGRATION_12_13**

Insert after line 55 (after MIGRATION_11_12):

```kotlin
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add lastStudiedAt column
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN lastStudiedAt INTEGER"
        )

        // Add index for chapter-based queries
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_category_studied ON questions(category, lastStudiedAt, id)"
        )
    }
}
```

**Step 2: Update Database version and migrations**

```kotlin
@Database(
    entities = [QuestionEntity::class, ExamResultEntity::class, CardEntity::class],
    version = 13,  // CHANGED: 12 → 13
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun cardDao(): CardDao
}
```

**Step 3: Register migration in DatabaseModule**

Find file: `core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt`

Update `@Provides fun provideAppDatabase()` to include new migration:

```kotlin
.addMigrations(
    MIGRATION_9_10,
    MIGRATION_10_11,
    MIGRATION_11_12,
    MIGRATION_12_13  // NEW
)
```

**Step 4: Create schema export directory**

```bash
mkdir -p core/database/schemas/com.example.sie.core.database.AppDatabase
```

**Step 5: Verify compilation**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL + new schema file created at `core/database/schemas/com.example.sie.core.database.AppDatabase/13.json`

**Step 6: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt \
        core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt \
        core/database/schemas/
git commit -m "feat(database): add migration 12→13 for lastStudiedAt field"
```

---

### Task 1.3: Extend QuestionDao with Chapter Queries

**Goal:** Add DAO methods for category-based queries and lastStudiedAt updates

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/dao/QuestionDao.kt:64`

**Step 1: Add new DAO methods**

Append after line 63 (after `insertAll`):

```kotlin
// Chapter-based learning queries

@Query("SELECT DISTINCT category FROM questions ORDER BY category")
fun getAllCategories(): Flow<List<String>>

@Query("SELECT * FROM questions WHERE category IN (:categories)")
fun getQuestionsByCategories(categories: List<String>): Flow<List<QuestionEntity>>

@Query("UPDATE questions SET lastStudiedAt = :timestamp WHERE id = :questionId")
suspend fun updateLastStudiedAt(questionId: Int, timestamp: Long)

@Query("""
    SELECT * FROM questions
    WHERE category = :category
    ORDER BY
        CASE WHEN isWrong = 1 THEN 0 ELSE 1 END,
        wrongCount DESC,
        CASE WHEN lastStudiedAt IS NULL THEN 0 ELSE lastStudiedAt END,
        id
""")
fun getQuestionsByCategorySmartSorted(category: String): Flow<List<QuestionEntity>>
```

**Step 2: Verify compilation**

```bash
./gradlew :core:database:build
```

Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add core/database/src/main/java/com/example/sie/core/database/dao/QuestionDao.kt
git commit -m "feat(database): add chapter-based query methods to QuestionDao"
```

---

### Task 1.4: Extend QuestionRepository Interface and Implementation

**Goal:** Add repository methods for chapter operations

**Files:**
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/QuestionRepository.kt`
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/OfflineQuestionRepository.kt:90`

**Step 1: Add interface methods**

In `QuestionRepository.kt`, append after existing methods:

```kotlin
// Chapter-based learning
fun getAllCategories(): Flow<List<String>>
fun getQuestionsByCategories(categories: List<String>): Flow<List<Question>>
suspend fun markQuestionAsStudied(questionId: Int)
```

**Step 2: Implement in OfflineQuestionRepository**

Append after line 89 (after `countWrong()`):

```kotlin
override fun getAllCategories(): Flow<List<String>> {
    return questionDao.getAllCategories()
}

override fun getQuestionsByCategories(categories: List<String>): Flow<List<Question>> {
    return questionDao.getQuestionsByCategories(categories).map { entities ->
        entities.map { it.asExternalModel() }
    }
}

override suspend fun markQuestionAsStudied(questionId: Int) {
    questionDao.updateLastStudiedAt(questionId, System.currentTimeMillis())
}
```

**Step 3: Verify compilation**

```bash
./gradlew :core:data:build
```

Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add core/data/src/main/java/com/example/sie/core/data/repository/QuestionRepository.kt \
        core/data/src/main/java/com/example/sie/core/data/repository/OfflineQuestionRepository.kt
git commit -m "feat(data): add chapter-based methods to QuestionRepository"
```

---

## Phase 2: Core Models

### Task 2.1: Create Chapter Model

**Goal:** Define Chapter data class with progress statistics

**Files:**
- Create: `core/model/src/main/java/com/example/sie/core/model/Chapter.kt`

**Step 1: Create Chapter model**

```kotlin
package com.example.sie.core.model

data class Chapter(
    val name: String,              // e.g., "1. Macroeconomics"
    val displayName: String,        // Localized name, e.g., "宏观经济学"
    val totalQuestions: Int,        // Total questions in this chapter
    val studiedQuestions: Int,      // Questions with lastStudiedAt != null
    val correctCount: Int,          // Correctly answered questions
    val wrongCount: Int,            // Total wrong answers
    val accuracyRate: Float,        // Percentage: correctCount / studiedQuestions * 100
    val lastStudiedAt: Long?,       // Latest lastStudiedAt in this chapter
    val isSelected: Boolean = false // UI state: is this chapter selected?
)

/**
 * Extract localized display name from category string.
 *
 * Categories use format: "1. Macroeconomics\n1. 宏观经济学"
 * - For English (language="en"): returns "1. Macroeconomics"
 * - For Chinese (language="zh"): returns "1. 宏观经济学"
 */
fun String.extractLocalizedName(language: String = "en"): String {
    val parts = this.split("\n")
    return if (language == "zh") {
        parts.lastOrNull() ?: this
    } else {
        parts.firstOrNull() ?: this
    }
}
```

**Step 2: Verify compilation**

```bash
./gradlew :core:model:build
```

Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add core/model/src/main/java/com/example/sie/core/model/Chapter.kt
git commit -m "feat(model): add Chapter data class with localization support"
```

---

## Phase 3: ViewModel Layer

### Task 3.1: Create ChapterSelectionViewModel

**Goal:** Implement ViewModel for chapter selection with progress aggregation

**Files:**
- Create: `feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterSelectionViewModel.kt`

**Step 1: Create feature/chapter module structure**

```bash
mkdir -p feature/chapter/src/main/java/com/example/sie/feature/chapter
```

**Step 2: Create ChapterSelectionViewModel**

```kotlin
package com.example.sie.feature.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Chapter
import com.example.sie.core.model.extractLocalizedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChapterSelectionUiState {
    data object Loading : ChapterSelectionUiState
    data class Success(val chapters: List<Chapter>) : ChapterSelectionUiState
    data class Error(val message: String) : ChapterSelectionUiState
}

@HiltViewModel
class ChapterSelectionViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChapterSelectionUiState>(ChapterSelectionUiState.Loading)
    val uiState: StateFlow<ChapterSelectionUiState> = _uiState.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        loadChapters()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        // Reload chapters to update displayName
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            try {
                questionRepository.getAllCategories()
                    .combine(questionRepository.getAllQuestions()) { categories, questions ->
                        categories.map { category ->
                            val chapterQuestions = questions.filter { it.category == category }
                            val studiedQuestions = chapterQuestions.filter { it.lastStudiedAt != null }
                            val correctCount = studiedQuestions.count { !it.isWrong }

                            Chapter(
                                name = category,
                                displayName = category.extractLocalizedName(_language.value),
                                totalQuestions = chapterQuestions.size,
                                studiedQuestions = studiedQuestions.size,
                                correctCount = correctCount,
                                wrongCount = chapterQuestions.sumOf { it.wrongCount },
                                accuracyRate = if (studiedQuestions.isEmpty()) 0f
                                    else (correctCount.toFloat() / studiedQuestions.size) * 100f,
                                lastStudiedAt = chapterQuestions.mapNotNull { it.lastStudiedAt }.maxOrNull()
                            )
                        }
                    }
                    .collect { chapters ->
                        _uiState.value = ChapterSelectionUiState.Success(chapters)
                    }
            } catch (e: Exception) {
                _uiState.value = ChapterSelectionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleChapterSelection(chapterName: String) {
        val currentState = _uiState.value
        if (currentState is ChapterSelectionUiState.Success) {
            val updatedChapters = currentState.chapters.map { chapter ->
                if (chapter.name == chapterName) {
                    chapter.copy(isSelected = !chapter.isSelected)
                } else {
                    chapter
                }
            }
            _uiState.value = currentState.copy(chapters = updatedChapters)
        }
    }

    fun getSelectedChapters(): List<String> {
        val currentState = _uiState.value
        return if (currentState is ChapterSelectionUiState.Success) {
            currentState.chapters.filter { it.isSelected }.map { it.name }
        } else {
            emptyList()
        }
    }

    fun hasSelectedChapters(): Boolean {
        return getSelectedChapters().isNotEmpty()
    }
}
```

**Step 3: Verify compilation (will fail - need build.gradle.kts)**

Expected: Module not configured yet

**Step 4: Create build.gradle.kts for feature/chapter**

```bash
touch feature/chapter/build.gradle.kts
```

Content:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.sie.feature.chapter"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

**Step 5: Register module in settings.gradle.kts**

Add to root `settings.gradle.kts`:

```kotlin
include(":feature:chapter")
```

**Step 6: Create AndroidManifest.xml**

```bash
mkdir -p feature/chapter/src/main/
```

Create `feature/chapter/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

**Step 7: Verify compilation**

```bash
./gradlew :feature:chapter:build
```

Expected: BUILD SUCCESSFUL

**Step 8: Commit**

```bash
git add feature/chapter/ settings.gradle.kts
git commit -m "feat(chapter): create chapter feature module with ViewModel"
```

---

### Task 3.2: Extend StudyViewModel with Chapter Support

**Goal:** Add chapter filtering and smart sorting to StudyViewModel

**Files:**
- Modify: `feature/study/src/main/java/com/example/sie/feature/study/StudyViewModel.kt:58-91`

**Step 1: Add chapter study initialization**

Insert after line 61 (after `initializeQuestions()` call in init block):

```kotlin
// Support for chapter-based learning
private var selectedCategories: List<String>? = null

fun startChapterStudy(categories: List<String>) {
    selectedCategories = categories
    initializeChapterQuestions()
}

private fun initializeChapterQuestions() {
    viewModelScope.launch {
        _uiState.value = StudyUiState.Loading
        try {
            val categories = selectedCategories ?: run {
                // Fallback to all questions if no categories specified
                initializeQuestions()
                return@launch
            }

            questionRepository.getQuestionsByCategories(categories).collect { questions ->
                if (questions.isNotEmpty()) {
                    allQuestionsCache = smartSortQuestions(questions)

                    if (history.isEmpty()) {
                        availableQuestions.clear()
                        availableQuestions.addAll(allQuestionsCache)
                        loadNextQuestion()
                    }

                    this.cancel()
                }
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException) {
                _uiState.value = StudyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

private fun smartSortQuestions(questions: List<Question>): List<Question> {
    return questions.sortedWith(
        compareByDescending<Question> { it.isWrong }           // 1. Wrong questions first
            .thenByDescending { it.wrongCount }                // 2. Higher error count first
            .thenBy { it.lastStudiedAt ?: 0L }                 // 3. Unstudied or oldest first
            .thenBy { it.id }                                   // 4. Stable sort by ID
    )
}
```

**Step 2: Update selectOption to mark as studied**

Modify the `selectOption` method (around line 140-150) to add:

```kotlin
fun selectOption(index: Int) {
    val currentItem = history.getOrNull(currentIndex) ?: return

    if (!currentItem.isAnswerRevealed) {
        val isCorrect = index == currentItem.question.correctAnswerIndex

        // Update history item
        currentItem.selectedOptionIndex = index
        currentItem.isAnswerRevealed = true
        currentItem.isCorrect = isCorrect

        // Update stats
        stats = stats.copy(
            totalAnswered = stats.totalAnswered + 1,
            correctCount = if (isCorrect) stats.correctCount + 1 else stats.correctCount
        )

        // Mark question as studied (NEW)
        viewModelScope.launch {
            try {
                questionRepository.markQuestionAsStudied(currentItem.question.id)
            } catch (_: Exception) {
                // Silently ignore
            }
        }

        // Mark wrong questions in database for review later
        if (!isCorrect) {
            viewModelScope.launch {
                try {
                    questionRepository.markAsWrong(currentItem.question.id)
                } catch (_: Exception) {
                    // Silently ignore — marking wrong questions is non-critical
                }
            }
        }

        updateUiState()
    }
}
```

**Step 3: Verify compilation**

```bash
./gradlew :feature:study:build
```

Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add feature/study/src/main/java/com/example/sie/feature/study/StudyViewModel.kt
git commit -m "feat(study): add chapter filtering and smart sorting to StudyViewModel"
```

---

## Phase 4: UI Components

### Task 4.1: Add Chapter Strings to Resources

**Goal:** Add localized strings for chapter selection UI

**Files:**
- Modify: `core/common/src/main/res/values/strings.xml`
- Modify: `core/common/src/main/res/values-zh/strings.xml`

**Step 1: Add English strings**

Append to `values/strings.xml`:

```xml
<!-- Chapter Selection Screen -->
<string name="chapter_title">Chapter Selection</string>
<string name="chapter_start">Start</string>
<string name="chapter_empty">No chapters available</string>
<string name="chapter_error">Failed to load chapters. Please try again.</string>
<string name="chapter_retry">Retry</string>
<string name="chapter_select_hint">Select at least one chapter</string>
<string name="chapter_progress_format">%1$d questions | Studied %2$d | %3$.0f%%</string>
<string name="chapter_not_studied">Not studied yet</string>
```

**Step 2: Add Chinese strings**

Append to `values-zh/strings.xml`:

```xml
<!-- Chapter Selection Screen -->
<string name="chapter_title">章节选择</string>
<string name="chapter_start">开始</string>
<string name="chapter_empty">暂无章节</string>
<string name="chapter_error">加载章节失败，请重试</string>
<string name="chapter_retry">重试</string>
<string name="chapter_select_hint">请至少选择一个章节</string>
<string name="chapter_progress_format">%1$d题 | 已学%2$d | %3$.0f%%</string>
<string name="chapter_not_studied">尚未学习</string>
```

**Step 3: Verify compilation**

```bash
./gradlew :core:common:build
```

Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add core/common/src/main/res/values/strings.xml \
        core/common/src/main/res/values-zh/strings.xml
git commit -m "feat(resources): add chapter selection localized strings"
```

---

### Task 4.2: Create ChapterCard Component

**Goal:** Create reusable chapter card with glassmorphism style

**Files:**
- Create: `feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterCard.kt`

**Step 1: Create ChapterCard composable**

```kotlin
package com.example.sie.feature.chapter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sie.core.designsystem.component.GlassCard
import com.example.sie.core.model.Chapter
import com.example.sie.core.common.R as CommonR

@Composable
fun ChapterCard(
    chapter: Chapter,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() },
        border = if (chapter.isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chapter name
                Text(
                    text = chapter.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Progress info
                if (chapter.studiedQuestions > 0) {
                    Text(
                        text = stringResource(
                            CommonR.string.chapter_progress_format,
                            chapter.totalQuestions,
                            chapter.studiedQuestions,
                            chapter.accuracyRate
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(CommonR.string.chapter_not_studied),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Checkbox
            Checkbox(
                checked = chapter.isSelected,
                onCheckedChange = { onToggleSelection() }
            )
        }
    }
}
```

**Step 2: Verify compilation**

```bash
./gradlew :feature:chapter:build
```

Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterCard.kt
git commit -m "feat(chapter): create ChapterCard component with glassmorphism"
```

---

### Task 4.3: Create ChapterSelectionScreen

**Goal:** Build main chapter selection UI with list and start button

**Files:**
- Create: `feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterSelectionScreen.kt`

**Step 1: Create ChapterSelectionScreen**

```kotlin
package com.example.sie.feature.chapter

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    language: String,
    modifier: Modifier = Modifier,
    viewModel: ChapterSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(language) {
        viewModel.setLanguage(language)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CommonR.string.chapter_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(CommonR.string.common_back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val selected = viewModel.getSelectedChapters()
                            if (selected.isNotEmpty()) {
                                onStartStudy(selected)
                            }
                        },
                        enabled = viewModel.hasSelectedChapters()
                    ) {
                        Text(stringResource(CommonR.string.chapter_start))
                    }
                }
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
                    CircularProgressIndicator()
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
                            style = MaterialTheme.typography.bodyLarge
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { /* TODO: Implement retry */ }) {
                            Text(stringResource(CommonR.string.chapter_retry))
                        }
                    }
                }
            }
        }
    }
}
```

**Step 2: Verify compilation**

```bash
./gradlew :feature:chapter:build
```

Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterSelectionScreen.kt
git commit -m "feat(chapter): create ChapterSelectionScreen with loading states"
```

---

### Task 4.4: Add Chapter Navigation

**Goal:** Create navigation route and NavHost integration

**Files:**
- Create: `feature/chapter/src/main/java/com/example/sie/feature/chapter/navigation/ChapterNavigation.kt`

**Step 1: Create navigation file**

```kotlin
package com.example.sie.feature.chapter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.chapter.ChapterSelectionScreen

const val CHAPTER_SELECTION_ROUTE = "chapter_selection"

fun NavController.navigateToChapterSelection(navOptions: NavOptions? = null) {
    navigate(CHAPTER_SELECTION_ROUTE, navOptions)
}

fun NavGraphBuilder.chapterSelectionScreen(
    onBackClick: () -> Unit,
    onStartStudy: (List<String>) -> Unit,
    language: String
) {
    composable(route = CHAPTER_SELECTION_ROUTE) {
        ChapterSelectionScreen(
            onBackClick = onBackClick,
            onStartStudy = onStartStudy,
            language = language
        )
    }
}
```

**Step 2: Verify compilation**

```bash
./gradlew :feature:chapter:build
```

Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add feature/chapter/src/main/java/com/example/sie/feature/chapter/navigation/
git commit -m "feat(chapter): add navigation routes for chapter selection"
```

---

## Phase 5: Integration

### Task 5.1: Integrate Chapter Navigation into NavHost

**Goal:** Wire chapter navigation into main navigation graph

**Files:**
- Modify: `app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt`
- Modify: `app/build.gradle.kts` (add :feature:chapter dependency)

**Step 1: Add dependency to app module**

In `app/build.gradle.kts`, add to dependencies block:

```kotlin
implementation(project(":feature:chapter"))
```

**Step 2: Import chapter navigation in SieNavHost.kt**

Add imports:

```kotlin
import com.example.sie.feature.chapter.navigation.CHAPTER_SELECTION_ROUTE
import com.example.sie.feature.chapter.navigation.chapterSelectionScreen
import com.example.sie.feature.chapter.navigation.navigateToChapterSelection
```

**Step 3: Add chapter route to NavHost**

In the `NavHost` composable, add after study route:

```kotlin
chapterSelectionScreen(
    onBackClick = { navController.popBackStack() },
    onStartStudy = { selectedCategories ->
        // Navigate to study screen with categories
        navController.navigate("study?categories=${selectedCategories.joinToString(",")}")
    },
    language = language
)
```

**Step 4: Update study route to accept categories parameter**

Modify study route (if not already parameterized):

```kotlin
composable(
    route = "study?categories={categories}",
    arguments = listOf(
        navArgument("categories") {
            type = NavType.StringType
            nullable = true
        }
    )
) { backStackEntry ->
    val categoriesString = backStackEntry.arguments?.getString("categories")
    val categories = categoriesString?.split(",")?.filter { it.isNotBlank() }

    StudyScreen(
        onBackClick = { navController.popBackStack() },
        language = language,
        selectedCategories = categories  // Pass to StudyScreen
    )
}
```

**Step 5: Update StudyScreen to accept categories**

Modify `feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt`:

Add parameter:

```kotlin
@Composable
fun StudyScreen(
    onBackClick: () -> Unit,
    language: String,
    selectedCategories: List<String>? = null,  // NEW
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = hiltViewModel()
) {
    // Initialize with categories if provided
    LaunchedEffect(selectedCategories) {
        if (selectedCategories != null && selectedCategories.isNotEmpty()) {
            viewModel.startChapterStudy(selectedCategories)
        }
    }

    // ... rest of StudyScreen
}
```

**Step 6: Sync build files**

```bash
./gradlew build
```

Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add app/build.gradle.kts \
        app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt \
        feature/study/src/main/java/com/example/sie/feature/study/StudyScreen.kt
git commit -m "feat(navigation): integrate chapter selection into main nav graph"
```

---

### Task 5.2: Update HomeScreen to Link Chapter Selection

**Goal:** Replace "Start Practice" with "Chapter Selection" entry point

**Files:**
- Modify: `feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt`

**Step 1: Find "Start Practice" card in HomeScreen**

Locate the card (around line 80-100) that navigates to study screen.

**Step 2: Update navigation to chapter selection**

Change from:

```kotlin
onClick = { onNavigateToStudy() }
```

To:

```kotlin
onClick = { onNavigateToChapterSelection() }
```

**Step 3: Update HomeScreen signature**

Change:

```kotlin
fun HomeScreen(
    onNavigateToStudy: () -> Unit,
    ...
)
```

To:

```kotlin
fun HomeScreen(
    onNavigateToChapterSelection: () -> Unit,
    ...
)
```

**Step 4: Update HomeScreen call in navigation**

In `feature/home/src/main/java/com/example/sie/feature/home/navigation/HomeNavigation.kt`:

Change:

```kotlin
homeScreen(
    onNavigateToStudy = { navController.navigate("study") },
    ...
)
```

To:

```kotlin
homeScreen(
    onNavigateToChapterSelection = { navController.navigateToChapterSelection() },
    ...
)
```

Add import:

```kotlin
import com.example.sie.feature.chapter.navigation.navigateToChapterSelection
```

**Step 5: Verify compilation**

```bash
./gradlew :app:build
```

Expected: BUILD SUCCESSFUL

**Step 6: Commit**

```bash
git add feature/home/src/main/java/com/example/sie/feature/home/HomeScreen.kt \
        feature/home/src/main/java/com/example/sie/feature/home/navigation/HomeNavigation.kt
git commit -m "feat(home): replace Start Practice with Chapter Selection entry"
```

---

## Phase 6: Testing & Verification

### Task 6.1: Manual Testing Checklist

**Goal:** Verify all features work end-to-end

**Manual Test Steps:**

1. **Database Migration**
   ```bash
   # Uninstall app to trigger fresh install with migration
   adb uninstall com.example.sie_android_app
   npm run android
   ```
   - Verify app launches without crash
   - Check logcat for migration success: `adb logcat | grep Migration`

2. **Chapter Selection Screen**
   - Navigate: Home → Chapter Selection
   - Verify: All chapters display with correct names (English/Chinese based on language setting)
   - Verify: Progress shows "Not studied yet" for unstudied chapters
   - Verify: Can select/deselect chapters with checkbox
   - Verify: "Start" button disabled when no chapters selected
   - Verify: "Start" button enabled when ≥1 chapter selected

3. **Chapter Study Flow**
   - Select 2-3 chapters → Click "Start"
   - Verify: Study screen loads with questions from selected chapters only
   - Verify: Questions appear in smart order (wrong → unstudied → by error count)
   - Answer several questions (mix correct/wrong)
   - Return to Chapter Selection
   - Verify: Progress updated (studied count increased, accuracy updated)

4. **Language Switching**
   - Go to Settings → Change language to 中文
   - Return to Chapter Selection
   - Verify: Chapter names display in Chinese

5. **Error Handling**
   - Force database error (turn off device storage permission if possible)
   - Verify: Error state displays with retry button

**Expected Results:**
- All navigation flows work
- Progress persists across sessions
- Smart sorting prioritizes wrong questions
- Multi-language support works correctly

**Step: Document test results**

Create file: `docs/testing/2026-02-28-chapter-feature-test-results.md`

```markdown
# Chapter-Based Learning - Test Results

**Date:** 2026-02-28
**Tester:** [Your Name]
**Build:** [Commit Hash]

## Test Results

### 1. Database Migration ✅/❌
- [ ] App launches after fresh install
- [ ] Migration 12→13 completes successfully
- [ ] No crash on first run

### 2. Chapter Selection UI ✅/❌
- [ ] Chapters display with correct names
- [ ] Progress shows correctly
- [ ] Multi-select works
- [ ] Start button state updates

### 3. Study Flow ✅/❌
- [ ] Questions filtered by selected chapters
- [ ] Smart sorting applied
- [ ] Progress updates after answering

### 4. Localization ✅/❌
- [ ] English/Chinese switching works
- [ ] Chapter names localized

### 5. Error Handling ✅/❌
- [ ] Error state displays
- [ ] Retry button works

## Issues Found

[List any bugs or issues]

## Notes

[Additional observations]
```

**Commit test results:**

```bash
git add docs/testing/2026-02-28-chapter-feature-test-results.md
git commit -m "test: add manual test results for chapter feature"
```

---

### Task 6.2: Unit Tests for ChapterSelectionViewModel

**Goal:** Test chapter progress aggregation and selection logic

**Files:**
- Create: `feature/chapter/src/test/java/com/example/sie/feature/chapter/ChapterSelectionViewModelTest.kt`

**Step 1: Create test file**

```kotlin
package com.example.sie.feature.chapter

import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Question
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterSelectionViewModelTest {

    private lateinit var repository: QuestionRepository
    private lateinit var viewModel: ChapterSelectionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @Test
    fun `loadChapters calculates progress correctly`() = runTest {
        // Given
        val questions = listOf(
            Question(
                id = 1,
                content = "Q1",
                options = listOf("A", "B"),
                correctAnswerIndex = 0,
                explanation = "E1",
                category = "1. Macroeconomics",
                lastStudiedAt = System.currentTimeMillis(),
                isWrong = false
            ),
            Question(
                id = 2,
                content = "Q2",
                options = listOf("A", "B"),
                correctAnswerIndex = 0,
                explanation = "E2",
                category = "1. Macroeconomics",
                lastStudiedAt = null,
                isWrong = false
            )
        )

        every { repository.getAllCategories() } returns flowOf(listOf("1. Macroeconomics"))
        every { repository.getAllQuestions() } returns flowOf(questions)

        // When
        viewModel = ChapterSelectionViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ChapterSelectionUiState.Success
        assertEquals(1, state.chapters.size)

        val chapter = state.chapters[0]
        assertEquals(2, chapter.totalQuestions)
        assertEquals(1, chapter.studiedQuestions)
        assertEquals(100f, chapter.accuracyRate, 0.01f)
    }

    @Test
    fun `toggleChapterSelection updates isSelected state`() = runTest {
        // Given
        every { repository.getAllCategories() } returns flowOf(listOf("Chapter 1"))
        every { repository.getAllQuestions() } returns flowOf(emptyList())

        viewModel = ChapterSelectionViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.toggleChapterSelection("Chapter 1")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ChapterSelectionUiState.Success
        assertTrue(state.chapters[0].isSelected)
    }

    @Test
    fun `getSelectedChapters returns only selected chapters`() = runTest {
        // Given
        every { repository.getAllCategories() } returns flowOf(listOf("Chapter 1", "Chapter 2"))
        every { repository.getAllQuestions() } returns flowOf(emptyList())

        viewModel = ChapterSelectionViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.toggleChapterSelection("Chapter 1")
        advanceUntilIdle()

        // Then
        val selected = viewModel.getSelectedChapters()
        assertEquals(1, selected.size)
        assertEquals("Chapter 1", selected[0])
    }
}
```

**Step 2: Add test dependencies to feature/chapter/build.gradle.kts**

```kotlin
dependencies {
    // ... existing dependencies

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
```

**Step 3: Run tests**

```bash
./gradlew :feature:chapter:test
```

Expected: All tests pass

**Step 4: Commit**

```bash
git add feature/chapter/src/test/ feature/chapter/build.gradle.kts
git commit -m "test(chapter): add unit tests for ChapterSelectionViewModel"
```

---

### Task 6.3: Unit Tests for Smart Sorting

**Goal:** Verify smart sorting algorithm correctness

**Files:**
- Create: `feature/study/src/test/java/com/example/sie/feature/study/SmartSortTest.kt`

**Step 1: Create test file**

```kotlin
package com.example.sie.feature.study

import com.example.sie.core.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSortTest {

    private fun smartSort(questions: List<Question>): List<Question> {
        return questions.sortedWith(
            compareByDescending<Question> { it.isWrong }
                .thenByDescending { it.wrongCount }
                .thenBy { it.lastStudiedAt ?: 0L }
                .thenBy { it.id }
        )
    }

    @Test
    fun `wrong questions appear first`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = false),
            createQuestion(id = 2, isWrong = true),
            createQuestion(id = 3, isWrong = false)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // Wrong question first
    }

    @Test
    fun `higher wrongCount appears first among wrong questions`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = true, wrongCount = 1),
            createQuestion(id = 2, isWrong = true, wrongCount = 3),
            createQuestion(id = 3, isWrong = true, wrongCount = 2)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // wrongCount=3
        assertEquals(3, sorted[1].id) // wrongCount=2
        assertEquals(1, sorted[2].id) // wrongCount=1
    }

    @Test
    fun `unstudied questions appear before studied among correct questions`() {
        val questions = listOf(
            createQuestion(id = 1, lastStudiedAt = 1000L),
            createQuestion(id = 2, lastStudiedAt = null),
            createQuestion(id = 3, lastStudiedAt = 500L)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // Unstudied (null)
        assertEquals(3, sorted[1].id) // Older studied
        assertEquals(1, sorted[2].id) // Recent studied
    }

    @Test
    fun `complete sorting order is correct`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = false, lastStudiedAt = 1000L),
            createQuestion(id = 2, isWrong = true, wrongCount = 2),
            createQuestion(id = 3, isWrong = true, wrongCount = 3),
            createQuestion(id = 4, isWrong = false, lastStudiedAt = null),
            createQuestion(id = 5, isWrong = false, lastStudiedAt = 500L)
        )

        val sorted = smartSort(questions)

        // Expected order: 3 (wrong, count=3), 2 (wrong, count=2), 4 (unstudied), 5 (old), 1 (recent)
        assertEquals(listOf(3, 2, 4, 5, 1), sorted.map { it.id })
    }

    private fun createQuestion(
        id: Int,
        isWrong: Boolean = false,
        wrongCount: Int = 0,
        lastStudiedAt: Long? = null
    ) = Question(
        id = id,
        content = "Question $id",
        options = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = "Test",
        isWrong = isWrong,
        wrongCount = wrongCount,
        lastStudiedAt = lastStudiedAt
    )
}
```

**Step 2: Run tests**

```bash
./gradlew :feature:study:test
```

Expected: All tests pass

**Step 3: Commit**

```bash
git add feature/study/src/test/java/com/example/sie/feature/study/SmartSortTest.kt
git commit -m "test(study): add unit tests for smart sorting algorithm"
```

---

## Phase 7: Final Verification & Documentation

### Task 7.1: Update CHANGELOG

**Goal:** Document new feature in changelog

**Files:**
- Modify: `CHANGELOG.md`

**Step 1: Add entry to CHANGELOG.md**

```markdown
## [Unreleased]

### Added

- **Chapter-Based Learning**: New feature allowing users to:
  - Select multiple chapters for targeted study
  - View detailed progress per chapter (total questions, studied count, accuracy)
  - Smart question sorting (wrong questions first, then unstudied, then by error count)
  - Real-time progress tracking using lastStudiedAt timestamps
- Database migration 12→13 adding `lastStudiedAt` field to questions table
- New `feature/chapter` module with ChapterSelectionScreen and ChapterSelectionViewModel
- Localization support for chapter names (English/Chinese)

### Changed

- Home screen: "Start Practice" now navigates to Chapter Selection
- StudyViewModel: Added chapter filtering and smart sorting capabilities
- QuestionRepository: Added methods for category-based queries
```

**Step 2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs: update CHANGELOG with chapter-based learning feature"
```

---

### Task 7.2: Create Feature Documentation

**Goal:** Document usage and architecture for future developers

**Files:**
- Create: `docs/features/chapter-based-learning.md`

**Step 1: Create documentation**

```markdown
# Chapter-Based Learning Feature

## Overview

Allows users to study questions organized by chapters (categories), with multi-select support and smart sorting.

## User Flow

1. Home → Chapter Selection
2. Select one or more chapters
3. Click "Start" → Study Screen with filtered questions
4. Questions auto-sorted: wrong first → unstudied → by error count
5. Answer questions → `lastStudiedAt` updated automatically
6. Return to Chapter Selection → progress refreshed

## Architecture

### Data Layer

- **QuestionEntity**: Added `lastStudiedAt: Long?` field
- **Migration 12→13**: Adds column and index
- **QuestionDao**: New methods:
  - `getAllCategories(): Flow<List<String>>`
  - `getQuestionsByCategories(categories: List<String>): Flow<List<QuestionEntity>>`
  - `updateLastStudiedAt(questionId: Int, timestamp: Long)`

### Domain Layer

- **Chapter Model**: Aggregates progress statistics per category
- **QuestionRepository**: Added chapter-based query methods

### UI Layer

- **feature/chapter**: New module
  - `ChapterSelectionScreen`: Main UI
  - `ChapterSelectionViewModel`: Progress aggregation and selection state
  - `ChapterCard`: Reusable component with glassmorphism

### Integration

- **NavHost**: Added `chapterSelectionScreen()` route
- **StudyViewModel**: Added `startChapterStudy(categories: List<String>)` method
- **HomeScreen**: Updated to navigate to Chapter Selection

## Smart Sorting Algorithm

```kotlin
questions.sortedWith(
    compareByDescending { it.isWrong }        // 1. Wrong questions first
        .thenByDescending { it.wrongCount }   // 2. More errors first
        .thenBy { it.lastStudiedAt ?: 0L }    // 3. Unstudied or oldest first
        .thenBy { it.id }                      // 4. Stable sort
)
```

## Localization

Chapter names support bilingual format:
- Category: `"1. Macroeconomics\n1. 宏观经济学"`
- `extractLocalizedName("en")` → `"1. Macroeconomics"`
- `extractLocalizedName("zh")` → `"1. 宏观经济学"`

## Testing

- Unit tests: `ChapterSelectionViewModelTest`, `SmartSortTest`
- Manual test checklist: See `docs/testing/2026-02-28-chapter-feature-test-results.md`
```

**Step 2: Commit**

```bash
git add docs/features/chapter-based-learning.md
git commit -m "docs: add comprehensive feature documentation for chapter learning"
```

---

### Task 7.3: Final Build & Clean

**Goal:** Ensure clean build and no lint errors

**Step 1: Clean build**

```bash
./gradlew clean
./gradlew build
```

Expected: BUILD SUCCESSFUL

**Step 2: Run lint**

```bash
./gradlew lint
```

Expected: No errors (warnings acceptable)

**Step 3: Run all tests**

```bash
./gradlew test
```

Expected: All tests pass

**Step 4: Final commit**

```bash
git add .
git commit -m "chore: final cleanup and verification for chapter feature"
```

---

## Summary

**Total Tasks:** 25 tasks across 7 phases
**Estimated Time:** 2-3 days
**Files Created:** 15 new files
**Files Modified:** 12 existing files

**Key Deliverables:**
1. ✅ Database migration 12→13 with `lastStudiedAt` field
2. ✅ Chapter model with progress aggregation
3. ✅ ChapterSelectionViewModel with reactive updates
4. ✅ ChapterSelectionScreen with glassmorphism UI
5. ✅ Smart sorting algorithm in StudyViewModel
6. ✅ Navigation integration
7. ✅ Comprehensive tests and documentation

**Next Steps After Completion:**
1. Manual testing on physical device
2. User acceptance testing
3. Performance profiling (if needed)
4. Potential future enhancements:
   - Chapter notes
   - Chapter achievements
   - Intelligent chapter recommendations
