# Study Enhancement Features Design

> **Design Date**: 2026-02-28
> **Status**: Approved
> **Architecture**: Lightweight Extension (Option A)

## Executive Summary

This document outlines the design for four major study enhancement features:
1. **Bookmark System Enhancement** - Fix existing bugs and add UI entry points
2. **Statistics History** - Expandable exam records with filtering
3. **Wrong Questions Management** - Auto-marking and dedicated review module
4. **Exam Result Interactions** - Clickable category breakdown with BottomSheet

**Key Design Decision**: Adopt lightweight extension approach (Option A) with minimal database changes and no new feature modules.

---

## Table of Contents

1. [Architecture Design](#1-architecture-design)
2. [Data Model Design](#2-data-model-design)
3. [UI Design](#3-ui-design)
4. [Implementation Details](#4-implementation-details)
5. [Error Handling & Testing](#5-error-handling--testing)

---

## 1. Architecture Design

### 1.1 Overall Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                    │
├─────────────────────────────────────────────────────────┤
│  HomeScreen (Extended)                                  │
│  ├─ New Card: "⭐ Bookmarked" → BookmarkedScreen        │
│  └─ New Card: "📕 Wrong Questions" → WrongQuestionsScreen│
│                                                         │
│  StudyScreen (Extended)                                 │
│  └─ New: Bookmark button (Star icon)                   │
│                                                         │
│  ExamScreen (Fixed + Extended)                          │
│  ├─ Fix: Add Toast on bookmark click                   │
│  └─ New: Category click opens BottomSheet              │
│                                                         │
│  StatsScreen (Extended)                                 │
│  └─ New: Expandable exam records + filters             │
│                                                         │
│  BookmarkedScreen (New)                                 │
│  └─ Display all bookmarked questions with category filter│
│                                                         │
│  WrongQuestionsScreen (New)                             │
│  └─ Display wrong questions, sorted by wrongCount       │
├─────────────────────────────────────────────────────────┤
│                   ViewModel Layer                       │
├─────────────────────────────────────────────────────────┤
│  StudyViewModel (Extended)                              │
│  ├─ toggleBookmark(questionId)                          │
│  └─ markAsWrong(questionId) - auto-called on incorrect │
│                                                         │
│  ExamViewModel (Extended)                               │
│  └─ submitExam() - batch mark wrong questions          │
│                                                         │
│  StatsViewModel (Extended)                              │
│  ├─ getExamDetailQuestions(examId)                     │
│  └─ Filter logic (by score, category, time)            │
│                                                         │
│  BookmarkedViewModel (New)                              │
│  ├─ getBookmarkedQuestions()                            │
│  └─ filterByCategory(category)                         │
│                                                         │
│  WrongQuestionsViewModel (New)                          │
│  ├─ getWrongQuestions()                                 │
│  └─ removeFromWrongList(questionId)                    │
├─────────────────────────────────────────────────────────┤
│                   Repository Layer                      │
├─────────────────────────────────────────────────────────┤
│  QuestionRepository (Extended)                          │
│  ├─ getBookmarkedQuestions()                            │
│  ├─ getWrongQuestions()                                 │
│  ├─ markAsWrong(questionId)                             │
│  └─ removeFromWrongList(questionId)                    │
├─────────────────────────────────────────────────────────┤
│                   Database Layer (Room)                 │
├─────────────────────────────────────────────────────────┤
│  QuestionEntity (Upgraded)                              │
│  ├─ isBookmarked: Boolean                               │
│  ├─ isWrong: Boolean                                    │
│  └─ wrongCount: Int (NEW)                              │
│                                                         │
│  QuestionDao (Extended)                                 │
│  ├─ getBookmarkedQuestions()                            │
│  ├─ getWrongQuestions()                                 │
│  ├─ markAsWrong(questionId) - auto-increments wrongCount│
│  └─ removeFromWrongList(questionId)                    │
└─────────────────────────────────────────────────────────┘
```

### 1.2 Module File Structure

```
sie-android-app/
├── feature/
│   ├── home/
│   │   ├── HomeScreen.kt (Extended: +2 cards)
│   │   ├── BookmarkedScreen.kt (NEW)
│   │   ├── WrongQuestionsScreen.kt (NEW)
│   │   ├── BookmarkedViewModel.kt (NEW)
│   │   ├── WrongQuestionsViewModel.kt (NEW)
│   │   └── navigation/
│   │       ├── BookmarkedNavigation.kt (NEW)
│   │       └── WrongNavigation.kt (NEW)
│   │
│   ├── study/
│   │   ├── StudyScreen.kt (Extended: +bookmark button)
│   │   └── StudyViewModel.kt (Extended: +toggleBookmark, markAsWrong)
│   │
│   ├── exam/
│   │   ├── ExamScreen.kt (Fixed: Toast; Extended: BottomSheet)
│   │   └── ExamViewModel.kt (Extended: mark wrong in submitExam)
│   │
│   └── stats/
│       ├── StatsScreen.kt (Extended: expandable + filters)
│       └── StatsViewModel.kt (Extended: filtering logic)
│
├── core/
│   ├── database/
│   │   ├── AppDatabase.kt (Version 9 → 10)
│   │   ├── model/
│   │   │   └── QuestionEntity.kt (+wrongCount field)
│   │   └── dao/
│   │       └── QuestionDao.kt (+4 methods)
│   │
│   ├── data/
│   │   └── repository/
│   │       ├── QuestionRepository.kt (interface extended)
│   │       └── OfflineQuestionRepository.kt (impl extended)
│   │
│   ├── model/
│   │   └── Question.kt (+wrongCount field)
│   │
│   └── designsystem/
│       └── component/
│           ├── CategoryBottomSheet.kt (NEW)
│           └── ExamRecordExpandableCard.kt (NEW)
│
└── app/
    └── navigation/
        └── SieNavHost.kt (+2 navigation routes)
```

### 1.3 Key Data Flows

#### Bookmark Flow
```
User clicks bookmark
    ↓
StudyScreen / ExamScreen
    ↓
ViewModel.toggleBookmark(questionId)
    ↓
QuestionRepository.toggleBookmark(questionId)
    ↓
QuestionDao.toggleBookmark(questionId)
    ↓
SQL: UPDATE questions SET isBookmarked = NOT isBookmarked WHERE id = :questionId
    ↓
Room triggers Flow update
    ↓
All subscribers auto-refresh
    ↓
Display Toast: "已收藏" / "已取消收藏"
```

#### Wrong Question Marking (Exam)
```
User submits exam
    ↓
ExamViewModel.submitExam()
    ↓
Identify wrong answers → wrongQuestionIds
    ↓
QuestionRepository.markAsWrongBatch(wrongQuestionIds)
    ↓
SQL: UPDATE questions SET isWrong = 1, wrongCount = wrongCount + 1 WHERE id IN (...)
    ↓
Room triggers Flow update
    ↓
WrongQuestionsScreen auto-shows new entries
```

#### Wrong Question Marking (Study)
```
User selects answer
    ↓
StudyViewModel.selectOption(optionIndex)
    ↓
Check if correct → if incorrect:
    ↓
QuestionRepository.markAsWrong(questionId)
    ↓
(Same SQL update as exam flow)
```

---

## 2. Data Model Design

### 2.1 Database Migration (9 → 10)

```kotlin
// AppDatabase.kt
@Database(
    entities = [QuestionEntity::class, ExamResultEntity::class],
    version = 10,  // Upgraded from 9
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() { ... }

// Migration
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN wrongCount INTEGER NOT NULL DEFAULT 0"
        )
    }
}
```

### 2.2 Data Models

#### QuestionEntity (Database Entity)
```kotlin
@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["isBookmarked"]),  // Speed up bookmark queries
        Index(value = ["isWrong"]),       // Speed up wrong question queries
        Index(value = ["category"])       // Speed up category filtering
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val options: String,              // JSON: ["A", "B", "C"]
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0           // NEW: Error count
)
```

#### Question (Domain Model)
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
    val wrongCount: Int = 0  // NEW
)
```

### 2.3 DAO Extensions

```kotlin
@Dao
interface QuestionDao {

    // ========== Existing Methods ==========
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET isBookmarked = NOT isBookmarked WHERE id = :questionId")
    suspend fun toggleBookmark(questionId: Int)

    // ========== NEW Methods ==========

    // 1. Get bookmarked questions
    @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY category, id")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    // 2. Get wrong questions (sorted by wrongCount DESC)
    @Query("""
        SELECT * FROM questions
        WHERE isWrong = 1
        ORDER BY wrongCount DESC, category, id
    """)
    fun getWrongQuestions(): Flow<List<QuestionEntity>>

    // 3. Mark question as wrong (auto-increment wrongCount)
    @Query("""
        UPDATE questions
        SET isWrong = 1, wrongCount = wrongCount + 1
        WHERE id = :questionId
    """)
    suspend fun markAsWrong(questionId: Int)

    // 4. Batch mark wrong questions (for exam submissions)
    @Query("""
        UPDATE questions
        SET isWrong = 1, wrongCount = wrongCount + 1
        WHERE id IN (:questionIds)
    """)
    suspend fun markAsWrongBatch(questionIds: List<Int>)

    // 5. Remove from wrong list (user action)
    @Query("UPDATE questions SET isWrong = 0 WHERE id = :questionId")
    suspend fun removeFromWrongList(questionId: Int)

    // 6. Get bookmarked by category
    @Query("""
        SELECT * FROM questions
        WHERE isBookmarked = 1 AND category = :category
        ORDER BY id
    """)
    fun getBookmarkedQuestionsByCategory(category: String): Flow<List<QuestionEntity>>

    // 7. Get wrong questions by category
    @Query("""
        SELECT * FROM questions
        WHERE isWrong = 1 AND category = :category
        ORDER BY wrongCount DESC, id
    """)
    fun getWrongQuestionsByCategory(category: String): Flow<List<QuestionEntity>>

    // 8. Get wrong question stats (by category)
    @Query("""
        SELECT category, COUNT(*) as count, SUM(wrongCount) as totalWrong
        FROM questions
        WHERE isWrong = 1
        GROUP BY category
        ORDER BY category
    """)
    fun getWrongQuestionsStats(): Flow<List<WrongQuestionStat>>
}

data class WrongQuestionStat(
    val category: String,
    val count: Int,           // Number of wrong questions in this category
    val totalWrong: Int       // Total error count in this category
)
```

### 2.4 Repository Extensions

```kotlin
interface QuestionRepository {
    // ========== Existing ==========
    fun getAllQuestions(): Flow<List<Question>>
    suspend fun toggleBookmark(questionId: Int)

    // ========== NEW ==========
    // Bookmark features
    fun getBookmarkedQuestions(): Flow<List<Question>>
    fun getBookmarkedQuestionsByCategory(category: String): Flow<List<Question>>

    // Wrong question features
    fun getWrongQuestions(): Flow<List<Question>>
    fun getWrongQuestionsByCategory(category: String): Flow<List<Question>>
    fun getWrongQuestionsStats(): Flow<List<WrongQuestionStat>>
    suspend fun markAsWrong(questionId: Int)
    suspend fun markAsWrongBatch(questionIds: List<Int>)
    suspend fun removeFromWrongList(questionId: Int)
}

class OfflineQuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override fun getBookmarkedQuestions(): Flow<List<Question>> =
        questionDao.getBookmarkedQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun markAsWrong(questionId: Int) {
        questionDao.markAsWrong(questionId)
    }

    override suspend fun markAsWrongBatch(questionIds: List<Int>) {
        questionDao.markAsWrongBatch(questionIds)
    }

    // ... other implementations
}
```

---

## 3. UI Design

### 3.1 HomeScreen - New Entry Cards

**Added 2 cards to Dashboard:**

```
┌─────────────────────────────────┐
│  ⭐ Bookmarked Questions        │  ← Card 5 (PrimaryGradient)
│     Review your saved questions │
├─────────────────────────────────┤
│  ❌ Wrong Questions             │  ← Card 6 (ErrorGradient)
│     Practice your mistakes      │
└─────────────────────────────────┘
```

### 3.2 BookmarkedScreen

**Layout:**
```
┌─────────────────────────────────┐
│  [←] Bookmarked Questions       │  ← TopAppBar
├─────────────────────────────────┤
│  [All] [Category A] [Category B]│  ← FilterChips
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ 1. Macroeconomics         │ │  ← Question Card
│  │ Which indicator is...?    │ │
│  │                       [★] │ │  ← Bookmark button
│  └───────────────────────────┘ │
│  ┌───────────────────────────┐ │
│  │ 2. Stocks                 │ │
│  │ What is the primary...?   │ │
│  │                       [★] │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**Features:**
- Filter by category (All / Category A / Category B / ...)
- Click card to view question details
- Click star to remove bookmark
- Empty state: "No bookmarked questions yet"

### 3.3 WrongQuestionsScreen

**Layout:**
```
┌─────────────────────────────────┐
│  [←] Wrong Questions            │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ Total: 15  Errors: 32     │ │  ← Stats card
│  │ Avg: 2.1                  │ │
│  └───────────────────────────┘ │
├─────────────────────────────────┤
│  [All] [Category A] [Category B]│  ← FilterChips
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ 1. Macroeconomics  [×3]   │ │  ← Wrong count badge
│  │ Which indicator is...?    │ │
│  │                       [×] │ │  ← Remove button
│  └───────────────────────────┘ │
│  ┌───────────────────────────┐ │
│  │ 3. Options         [×2]   │ │
│  │ What happens when...?     │ │
│  │                       [×] │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**Features:**
- Stats card: Total count, total errors, average errors per question
- Filter by category
- Badge shows error count (×3 means wrong 3 times)
- Click [×] to remove from wrong list (confirmation dialog)
- Empty state: "Great! No wrong questions yet" with ✓ icon

### 3.4 StudyScreen - Bookmark Button

**Added star icon (top-right corner):**
```
┌─────────────────────────────────┐
│  QuestionCard             [★]   │  ← Bookmark button
│  ┌──────────────────────────┐  │
│  │ Which indicator is...?   │  │
│  │ ○ Option A               │  │
│  │ ○ Option B               │  │
│  └──────────────────────────┘  │
└─────────────────────────────────┘
```
- Same style as ExamScreen
- Toggles between filled (★) and outlined (☆) star

### 3.5 ExamScreen - Fixed & Extended

**Bug Fix: Add Toast on bookmark click**
```kotlin
onClick = { questionId ->
    viewModel.toggleBookmark(questionId)
    // Show toast: "已收藏" / "已取消收藏"
}
```

**New Feature: Category BottomSheet**

Click category in result page → Opens BottomSheet:
```
┌─────────────────────────────────┐
│  1. Macroeconomics              │  ← BottomSheet title
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ [✓] Q1: Which indicator...│ │  ← Correct (green)
│  └───────────────────────────┘ │
│  ┌───────────────────────────┐ │
│  │ [✗] Q4: What is the...    │ │  ← Wrong (red)
│  └───────────────────────────┘ │
│  ┌───────────────────────────┐ │
│  │ [○] Q7: When does...      │ │  ← Unanswered (gray)
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```
- Shows all questions in that category
- Color-coded by correctness
- Click question to jump to detail page

### 3.6 StatsScreen - Expandable Records

**Layout:**
```
┌─────────────────────────────────┐
│  Average Score                  │
│  ┌───────────────────────────┐ │
│  │         75%               │ │  ← Score chart
│  └───────────────────────────┘ │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ Filters              [→]  │ │  ← Filter card (opens dialog)
│  └───────────────────────────┘ │
├─────────────────────────────────┤
│  Recent Results                 │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ 78%  Feb 28, 14:30   [▼] │ │  ← Click to expand
│  │ 25/32                     │ │
│  │───────────────────────────│ │
│  │ Category Breakdown        │ │  ← Expanded detail
│  │ Macroeconomics    83% █   │ │
│  │ Stocks            80% █   │ │
│  │ Options           60% █   │ │
│  └───────────────────────────┘ │
└─────────────────────────────────┘
```

**Filter Dialog:**
- Score: Pass only (≥70%) / Fail only (<70%)
- Category: All / Specific category
- Time Range: Last 7 days / Last 30 days / All time

---

## 4. Implementation Details

### 4.1 Wrong Question Marking Logic

#### Exam Scenario
```kotlin
// ExamViewModel.kt
fun submitExam() {
    viewModelScope.launch {
        // ... calculate score ...

        // Identify wrong answers
        val wrongQuestionIds = currentState.userAnswers
            .filter { (questionId, answerIndex) ->
                val question = currentState.questions.find { it.id == questionId }
                question?.correctAnswerIndex != answerIndex
            }
            .keys.toList()

        // Batch mark wrong
        if (wrongQuestionIds.isNotEmpty()) {
            questionRepository.markAsWrongBatch(wrongQuestionIds)
        }

        // ... update UI state ...
    }
}
```

#### Study Scenario
```kotlin
// StudyViewModel.kt
fun selectOption(optionIndex: Int) {
    val currentQuestion = (uiState.value as? StudyUiState.Success)?.currentQuestion ?: return
    val isCorrect = optionIndex == currentQuestion.correctAnswerIndex

    viewModelScope.launch {
        // If wrong, mark immediately
        if (!isCorrect) {
            questionRepository.markAsWrong(currentQuestion.id)
        }

        // Update stats and UI
        // ...
    }
}
```

### 4.2 ViewModel Implementations

#### BookmarkedViewModel
```kotlin
@HiltViewModel
class BookmarkedViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    val uiState: StateFlow<BookmarkedUiState> = combine(
        questionRepository.getBookmarkedQuestions(),
        _selectedCategory
    ) { allBookmarked, category ->
        if (allBookmarked.isEmpty()) {
            BookmarkedUiState.Empty
        } else {
            val filtered = if (category != null) {
                allBookmarked.filter { it.category == category }
            } else {
                allBookmarked
            }
            val categories = allBookmarked.map { it.category }.distinct().sorted()
            BookmarkedUiState.Success(questions = filtered, categories = categories)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookmarkedUiState.Loading
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            questionRepository.toggleBookmark(questionId)
        }
    }
}
```

#### WrongQuestionsViewModel
```kotlin
@HiltViewModel
class WrongQuestionsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    val uiState: StateFlow<WrongQuestionsUiState> = combine(
        questionRepository.getWrongQuestions(),
        _selectedCategory
    ) { allWrong, category ->
        if (allWrong.isEmpty()) {
            WrongQuestionsUiState.Empty
        } else {
            val filtered = if (category != null) {
                allWrong.filter { it.category == category }
            } else {
                allWrong
            }
            val stats = WrongQuestionsStats(
                totalCount = allWrong.size,
                totalWrongCount = allWrong.sumOf { it.wrongCount },
                avgWrongCount = allWrong.map { it.wrongCount }.average().toFloat()
            )
            val categories = allWrong.map { it.category }.distinct().sorted()
            WrongQuestionsUiState.Success(
                questions = filtered,
                categories = categories,
                stats = stats
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WrongQuestionsUiState.Loading
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun removeFromWrongList(questionId: Int) {
        viewModelScope.launch {
            questionRepository.removeFromWrongList(questionId)
        }
    }
}
```

### 4.3 Navigation Configuration

```kotlin
// BookmarkedNavigation.kt
const val BOOKMARKED_ROUTE = "bookmarked"

fun NavController.navigateToBookmarked(navOptions: NavOptions? = null) {
    navigate(BOOKMARKED_ROUTE, navOptions)
}

fun NavGraphBuilder.bookmarkedScreen(
    onBackClick: () -> Unit,
    onQuestionClick: (Int) -> Unit
) {
    composable(route = BOOKMARKED_ROUTE) {
        BookmarkedRoute(onBackClick, onQuestionClick)
    }
}

// WrongNavigation.kt
const val WRONG_QUESTIONS_ROUTE = "wrong_questions"

fun NavController.navigateToWrongQuestions(navOptions: NavOptions? = null) {
    navigate(WRONG_QUESTIONS_ROUTE, navOptions)
}

fun NavGraphBuilder.wrongQuestionsScreen(
    onBackClick: () -> Unit,
    onQuestionClick: (Int) -> Unit
) {
    composable(route = WRONG_QUESTIONS_ROUTE) {
        WrongQuestionsRoute(onBackClick, onQuestionClick)
    }
}

// SieNavHost.kt
@Composable
fun SieNavHost(navController: NavHostController, ...) {
    NavHost(...) {
        homeScreen(
            onBookmarkedClick = { navController.navigateToBookmarked() },
            onWrongQuestionsClick = { navController.navigateToWrongQuestions() },
            ...
        )
        bookmarkedScreen(
            onBackClick = { navController.popBackStack() },
            onQuestionClick = { questionId -> /* Navigate to detail */ }
        )
        wrongQuestionsScreen(
            onBackClick = { navController.popBackStack() },
            onQuestionClick = { questionId -> /* Navigate to detail */ }
        )
    }
}
```

---

## 5. Error Handling & Testing

### 5.1 Edge Cases

| Scenario | Handling Strategy |
|----------|-------------------|
| **Empty database** | Show empty state with guidance to start learning |
| **Empty bookmark list** | Display "No bookmarked questions yet" with star icon |
| **Empty wrong list** | Display "Great! No wrong questions yet" with ✓ icon |
| **Long question content** | Use `maxLines` + `TextOverflow.Ellipsis` |
| **Database corruption** | Room throws exception, prompt user to reinstall |
| **Rapid bookmark clicks** | ViewModel auto-queues operations via `viewModelScope.launch` |
| **App crash during exam** | Exam result saved to DB, recoverable in Stats |
| **wrongCount overflow** | Unlikely to reach Int.MAX_VALUE, can upgrade to Long if needed |

### 5.2 Error Handling

```kotlin
// Repository layer
class OfflineQuestionRepository(...) {
    override suspend fun toggleBookmark(questionId: Int) {
        try {
            questionDao.toggleBookmark(questionId)
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Failed to toggle bookmark", e)
            throw BookmarkException("Failed to update bookmark status", e)
        }
    }

    override suspend fun markAsWrongBatch(questionIds: List<Int>) {
        try {
            questionDao.markAsWrongBatch(questionIds)
        } catch (e: Exception) {
            // Retry individually
            val failures = mutableListOf<Int>()
            questionIds.forEach { id ->
                try {
                    questionDao.markAsWrong(id)
                } catch (ex: Exception) {
                    failures.add(id)
                }
            }
            if (failures.isNotEmpty()) {
                throw WrongQuestionException("Failed to mark ${failures.size} questions")
            }
        }
    }
}

// ViewModel layer
@HiltViewModel
class BookmarkedViewModel(...) {
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            try {
                questionRepository.toggleBookmark(questionId)
            } catch (e: BookmarkException) {
                _errorEvent.emit("Failed to update bookmark. Please try again.")
            }
        }
    }
}

// UI layer
@Composable
fun BookmarkedRoute(...) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        BookmarkedScreen(...)
    }
}
```

### 5.3 Testing Strategy

#### Test Coverage Targets

| Layer | Coverage Target | Priority |
|-------|----------------|----------|
| **DAO Layer** | 95%+ | High (core data operations) |
| **Repository Layer** | 90%+ | High (business logic) |
| **ViewModel Layer** | 85%+ | High (UI state management) |
| **UI Layer (Compose)** | 60%+ | Medium (key interactions) |

#### Sample Tests

**DAO Test:**
```kotlin
@RunWith(AndroidJUnit4::class)
class QuestionDaoTest {

    @Test
    fun markAsWrong_incrementsWrongCount() = runTest {
        // Given
        val question = createTestQuestion(id = 1, wrongCount = 0)
        questionDao.insertAll(listOf(question))

        // When
        questionDao.markAsWrong(1)
        val after1 = questionDao.getQuestionById(1).first()

        // Then
        assertEquals(1, after1?.wrongCount)
        assertTrue(after1?.isWrong ?: false)

        // When (mark again)
        questionDao.markAsWrong(1)
        val after2 = questionDao.getQuestionById(1).first()

        // Then
        assertEquals(2, after2?.wrongCount)
    }

    @Test
    fun getWrongQuestions_orderedByWrongCount() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, isWrong = true, wrongCount = 3),
            createTestQuestion(id = 2, isWrong = true, wrongCount = 1),
            createTestQuestion(id = 3, isWrong = true, wrongCount = 5)
        )
        questionDao.insertAll(questions)

        // When
        val wrong = questionDao.getWrongQuestions().first()

        // Then
        assertEquals(3, wrong.size)
        assertEquals(5, wrong[0].wrongCount)  // Descending order
        assertEquals(3, wrong[1].wrongCount)
        assertEquals(1, wrong[2].wrongCount)
    }
}
```

**ViewModel Test:**
```kotlin
@RunWith(JUnit4::class)
class BookmarkedViewModelTest {

    @Test
    fun `uiState filters questions by selected category`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A"),
            createTestQuestion(id = 2, category = "Category B"),
            createTestQuestion(id = 3, category = "Category A")
        )
        coEvery { questionRepository.getBookmarkedQuestions() } returns flowOf(questions)

        // When
        viewModel.selectCategory("Category A")
        advanceUntilIdle()
        val state = viewModel.uiState.value as BookmarkedUiState.Success

        // Then
        assertEquals(2, state.questions.size)
        assertTrue(state.questions.all { it.category == "Category A" })
    }
}
```

### 5.4 Performance Optimizations

**Database Indexes:**
```kotlin
@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["isBookmarked"]),  // Speed up bookmark queries
        Index(value = ["isWrong"]),       // Speed up wrong question queries
        Index(value = ["category"])       // Speed up category filtering
    ]
)
data class QuestionEntity(...)
```

**Flow Optimization:**
```kotlin
val uiState: StateFlow<BookmarkedUiState> = combine(...)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),  // Stop 5s after unsubscribe
        initialValue = BookmarkedUiState.Loading
    )
```

**LazyColumn Keys:**
```kotlin
LazyColumn {
    items(
        items = questions,
        key = { question -> question.id }  // Stable unique key for recomposition
    ) { question ->
        QuestionCard(...)
    }
}
```

---

## Conclusion

This design adopts a **lightweight extension approach** (Option A) to implement four major study enhancement features with minimal architectural changes:

1. **Database**: Only 1 new field (`wrongCount`)
2. **Modules**: No new feature modules, all extensions within existing `:feature:home`
3. **UI**: Consistent glassmorphism design with clear visual hierarchy
4. **Data Flow**: Leverages existing Room + Flow reactive architecture
5. **Testing**: Comprehensive unit tests for DAO, Repository, and ViewModel layers

**Key Benefits:**
- ✅ Simple implementation (minimal code churn)
- ✅ Low maintenance cost (code remains cohesive)
- ✅ Fast to implement (no complex module dependencies)
- ✅ Easy to test (clear separation of concerns)
- ✅ Future-proof (can refactor to modular if needed)

**Trade-offs:**
- ⚠️ Home module has more responsibilities (6 screens total)
- ⚠️ Exam detail history limited (only shows aggregated stats, not question-level history)

**Next Steps:**
- Proceed to **Implementation Planning** phase
- Create detailed task breakdown with TDD workflow
- Estimate 2-3 days for core implementation + 1 day for testing
