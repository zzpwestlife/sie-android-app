# Chapter-Based Learning Feature

## Overview

The Chapter-Based Learning feature allows users to study questions organized by chapters (categories), with multi-select support and smart sorting for optimal learning efficiency.

## User Flow

1. **Entry Point**: Home → Chapter Selection
2. **Selection**: Select one or more chapters from the list
3. **Study**: Click "Start" → Study Screen with filtered questions
4. **Smart Ordering**: Questions auto-sorted: wrong first → unstudied → by error count
5. **Progress Tracking**: Answer questions → `lastStudiedAt` updated automatically
6. **Review**: Return to Chapter Selection → progress refreshed

## Architecture

### Data Layer

#### QuestionEntity (Database)

**New Field:**
- `lastStudiedAt: Long?` - Timestamp in milliseconds when question was last studied (nullable)

**New Index:**
```sql
CREATE INDEX index_category_studied ON questions(category, lastStudiedAt, id)
```

**Migration 12→13:**
```kotlin
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE questions ADD COLUMN lastStudiedAt INTEGER")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_category_studied ON questions(category, lastStudiedAt, id)")
    }
}
```

#### QuestionDao

**New Methods:**
```kotlin
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

### Domain Layer

#### Chapter Model

```kotlin
data class Chapter(
    val name: String,              // Raw category name from database
    val displayName: String,        // Localized name
    val totalQuestions: Int,        // Total questions in this chapter
    val studiedQuestions: Int,      // Questions with lastStudiedAt != null
    val correctCount: Int,          // Correctly answered questions
    val wrongCount: Int,            // Total wrong answers
    val accuracyRate: Float,        // Percentage: correctCount / studiedQuestions * 100
    val lastStudiedAt: Long?,       // Latest lastStudiedAt in this chapter
    val isSelected: Boolean = false // UI state
)
```

#### QuestionRepository

**New Methods:**
```kotlin
fun getAllCategories(): Flow<List<String>>
fun getQuestionsByCategories(categories: List<String>): Flow<List<Question>>
suspend fun markQuestionAsStudied(questionId: Int)
```

### UI Layer

#### feature/chapter Module

**Structure:**
```
feature/chapter/
├── ChapterSelectionScreen.kt    # Main UI
├── ChapterSelectionViewModel.kt # Progress aggregation & selection state
├── ChapterCard.kt               # Reusable card component
└── navigation/
    └── ChapterNavigation.kt     # Navigation routes
```

**ChapterSelectionViewModel:**
- Aggregates progress statistics per chapter
- Manages chapter selection state
- Reacts to language changes from UserDataRepository
- Exposes UI state via StateFlow

**ChapterSelectionScreen:**
- Displays list of chapters with progress
- Supports multi-select via checkbox
- Start button enabled only when ≥1 chapter selected
- Loading/Error/Empty states handled

**ChapterCard:**
- Glassmorphism styling with GlassCard component
- Shows chapter name, total questions, studied count, accuracy
- Highlighted border when selected
- Clickable for selection toggle

### Integration

#### Navigation

**Route:** `CHAPTER_SELECTION_ROUTE = "chapter_selection"`

**Integration in NavHost:**
```kotlin
chapterSelectionScreen(
    onBackClick = { navController.popBackStack() },
    onStartStudy = { selectedCategories ->
        navController.navigate("study?categories=${selectedCategories.joinToString(",")}")
    },
    language = language
)
```

#### StudyViewModel

**New Method:**
```kotlin
fun startChapterStudy(categories: List<String>) {
    selectedCategories = categories
    initializeChapterQuestions()
}
```

**Chapter Question Initialization:**
1. Fetch questions by selected categories
2. Apply smart sorting algorithm
3. Load into study queue

**Mark as Studied:**
```kotlin
viewModelScope.launch {
    questionRepository.markQuestionAsStudied(questionId)
}
```

#### HomeScreen

**Updated Navigation:**
- Changed: "Start Practice" → "Chapter Selection"
- Navigation: `onNavigateToChapterSelection()` → Chapter Selection Screen

## Smart Sorting Algorithm

### Implementation

```kotlin
private fun smartSortQuestions(questions: List<Question>): List<Question> {
    return questions.sortedWith(
        compareByDescending<Question> { it.isWrong }        // 1. Wrong questions first
            .thenByDescending { it.wrongCount }             // 2. More errors first
            .thenBy { it.lastStudiedAt ?: 0L }              // 3. Unstudied or oldest first
            .thenBy { it.id }                                // 4. Stable sort
    )
}
```

### Sorting Rules (Priority Order)

1. **Wrong Questions First**: `isWrong = true` questions appear at the top
2. **Error Count Descending**: Among wrong questions, sort by `wrongCount DESC`
3. **Unstudied Then Oldest**:
   - Questions with `lastStudiedAt = null` (never studied) come first
   - Then questions ordered by `lastStudiedAt ASC` (oldest first)
4. **ID as Tiebreaker**: Ensures stable sort when all other fields are equal

### Example

Given questions:
- Q1: correct, studied yesterday (lastStudiedAt = 1000)
- Q2: wrong, wrongCount = 2
- Q3: wrong, wrongCount = 3
- Q4: correct, never studied (lastStudiedAt = null)
- Q5: correct, studied last week (lastStudiedAt = 500)

**Sorted Order:** Q3 → Q2 → Q4 → Q5 → Q1

## Localization

### Chapter Name Format

Categories in database use bilingual format:
```
"1. Macroeconomics\n1. 宏观经济学"
```

### Extraction Function

```kotlin
fun String.extractLocalizedName(language: String = "en"): String {
    val parts = this.split("\n")
    return if (language == "zh") {
        parts.lastOrNull() ?: this  // Chinese line
    } else {
        parts.firstOrNull() ?: this  // English line
    }
}
```

### Usage in ViewModel

```kotlin
val language: StateFlow<String> = userDataRepository.userData
    .map { it.language }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

// Apply localization in loadChapters()
displayName = category.extractLocalizedName(language.value)
```

## Resource Strings

### English (values/strings.xml)

```xml
<string name="chapter_title">Chapter Selection</string>
<string name="chapter_start">Start</string>
<string name="chapter_empty">No chapters available</string>
<string name="chapter_error">Failed to load chapters. Please try again.</string>
<string name="chapter_retry">Retry</string>
<string name="chapter_select_hint">Select at least one chapter</string>
<string name="chapter_progress_format">%1$d questions | Studied %2$d | %3$.0f%%</string>
<string name="chapter_not_studied">Not studied yet</string>
```

### Chinese (values-zh/strings.xml)

```xml
<string name="chapter_title">章节选择</string>
<string name="chapter_start">开始</string>
<string name="chapter_empty">暂无章节</string>
<string name="chapter_error">加载章节失败，请重试</string>
<string name="chapter_retry">重试</string>
<string name="chapter_select_hint">请至少选择一个章节</string>
<string name="chapter_progress_format">%1$d题 | 已学%2$d | %3$.0f%%</string>
<string name="chapter_not_studied">尚未学习</string>
```

## Testing

### Unit Tests

#### ChapterSelectionViewModelTest

**Tests:**
1. `loadChapters calculates progress correctly` - Verifies progress aggregation
2. `toggleChapterSelection updates isSelected state` - Tests selection toggle
3. `getSelectedChapters returns only selected chapters` - Tests filter logic

**Location:** `feature/chapter/src/test/java/com/example/sie/feature/chapter/ChapterSelectionViewModelTest.kt`

#### SmartSortTest

**Tests:**
1. `wrong questions appear first` - Verifies wrong questions prioritized
2. `higher wrongCount appears first among wrong questions` - Tests error count sorting
3. `unstudied questions appear before studied among correct questions` - Tests study status
4. `complete sorting order is correct` - End-to-end sorting verification

**Location:** `feature/study/src/test/java/com/example/sie/feature/study/SmartSortTest.kt`

### Manual Testing

**Checklist:** See `docs/testing/2026-02-28-chapter-feature-test-results.md`

**Key Test Cases:**
1. Database migration verification
2. Chapter selection UI interactions
3. Study flow with filtered questions
4. Language switching
5. Error handling

## Performance Considerations

### Database Optimization

- **Composite Index**: `index_category_studied` on `(category, lastStudiedAt, id)` enables efficient chapter-based queries
- **Flow-based Queries**: Reactive updates without manual polling
- **Lazy Loading**: Questions fetched only when needed

### UI Optimization

- **StateFlow**: Single source of truth for UI state
- **Stable Keys**: LazyColumn items use `key = { it.name }` for efficient recomposition
- **Minimal Recomposition**: Only affected chapters update on selection change

### Memory Management

- **Flow Cancellation**: Collectors canceled when ViewModel is cleared
- **Shallow Copies**: Chapter selection uses immutable data classes with structural sharing

## Future Enhancements

### Planned Features

1. **Chapter Notes**: Allow users to add notes per chapter
2. **Chapter Achievements**: Badges for completing chapters with high accuracy
3. **Intelligent Recommendations**: Suggest chapters based on weak areas
4. **Chapter Progress Widget**: Home screen widget showing current chapter progress
5. **Export Progress**: Share chapter progress as PDF or image

### Technical Improvements

1. **Pagination**: Load chapters in batches for large datasets
2. **Search/Filter**: Search chapters by name or filter by progress
3. **Sorting Options**: User-selectable sort order (alphabetical, progress, etc.)
4. **Offline Sync**: Cache chapter progress for offline access

## Troubleshooting

### Common Issues

**Issue:** Migration fails on app update
- **Cause:** Existing data conflicts with new schema
- **Solution:** Ensure migration script handles nullable `lastStudiedAt`

**Issue:** Chapter progress not updating after study
- **Cause:** `markQuestionAsStudied()` not called
- **Solution:** Verify `selectOption()` in StudyViewModel calls the repository method

**Issue:** Smart sorting not working as expected
- **Cause:** Incorrect comparator chain
- **Solution:** Review `smartSortQuestions()` implementation and ensure proper field access

## References

- **Migration Guide:** `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt`
- **ViewModel Implementation:** `feature/chapter/src/main/java/com/example/sie/feature/chapter/ChapterSelectionViewModel.kt`
- **Navigation Setup:** `app/src/main/java/com/example/sie_android_app/navigation/SieNavHost.kt`
- **Test Suite:** `feature/chapter/src/test/` and `feature/study/src/test/`
