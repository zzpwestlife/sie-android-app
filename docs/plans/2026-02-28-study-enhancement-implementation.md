# Study Enhancement Features Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** Implement bookmark system enhancement, statistics history, wrong questions management, and exam result interactions with complete TDD coverage.

**Architecture:** Lightweight extension approach - extend existing modules without creating new feature modules. Add `wrongCount` field to database, extend DAOs/Repositories/ViewModels, and create new screens in `:feature:home`.

**Tech Stack:** Kotlin, Jetpack Compose, Room Database, Hilt, Kotlin Coroutines/Flow, JUnit4, MockK

---

## Prerequisites

- Design document reviewed: `docs/design/2026-02-28-study-enhancement-features.md`
- Current branch: `feature/unit-testing` (or create new branch `feature/study-enhancement`)
- All existing tests passing
- Android Studio ready with project synced

---

## Task 1: Database Migration - Add `wrongCount` Field

**Goal:** Upgrade database from version 9 to 10, adding `wrongCount` field to `questions` table with proper migration and tests.

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt`
- Modify: `core/database/src/main/java/com/example/sie/core/database/model/QuestionEntity.kt`
- Modify: `core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt`
- Modify: `core/model/src/main/java/com/example/sie/core/model/Question.kt`
- Test: `core/database/src/androidTest/java/com/example/sie/core/database/MigrationTest.kt` (create if needed)

### Step 1: Write migration test (test-first)

Create test file if it doesn't exist:

```kotlin
// core/database/src/androidTest/java/com/example/sie/core/database/MigrationTest.kt
package com.example.sie.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB_NAME = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate9To10_addsWrongCountField() {
        // Given: Database at version 9
        val db = helper.createDatabase(TEST_DB_NAME, 9).apply {
            // Insert test question without wrongCount field
            execSQL("""
                INSERT INTO questions (id, content, options, correctAnswerIndex, explanation, category, isBookmarked, isWrong)
                VALUES (1, 'Test', '["A","B"]', 0, 'Explanation', 'Category', 0, 0)
            """)
            close()
        }

        // When: Migrate to version 10
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB_NAME, 10, true, MIGRATION_9_10)

        // Then: wrongCount field exists with default value 0
        val cursor = migratedDb.query("SELECT wrongCount FROM questions WHERE id = 1")
        cursor.moveToFirst()
        val wrongCount = cursor.getInt(cursor.getColumnIndex("wrongCount"))
        cursor.close()

        assertEquals(0, wrongCount)
    }
}
```

### Step 2: Run test to verify it fails

```bash
./gradlew :core:database:connectedAndroidTest --tests MigrationTest.migrate9To10_addsWrongCountField
```

Expected: FAIL with "MIGRATION_9_10 not found" or similar compilation error.

### Step 3: Update QuestionEntity with wrongCount field

```kotlin
// core/database/src/main/java/com/example/sie/core/database/model/QuestionEntity.kt

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["isBookmarked"]),
        Index(value = ["isWrong"]),
        Index(value = ["category"])
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
    val wrongCount: Int = 0  // NEW: Error count
)

// Update asExternalModel() mapping
fun QuestionEntity.asExternalModel() = Question(
    id = id,
    content = content,
    options = JSONArray(options).let { jsonArray ->
        List(jsonArray.length()) { i -> jsonArray.getString(i) }
    },
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    category = category,
    isBookmarked = isBookmarked,
    isWrong = isWrong,
    wrongCount = wrongCount  // NEW
)
```

### Step 4: Update Question domain model

```kotlin
// core/model/src/main/java/com/example/sie/core/model/Question.kt

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
) {
    // Existing methods remain unchanged
    fun getContent(language: String): String = when (language) {
        "zh" -> content.split("\n").getOrNull(1) ?: content
        else -> content.split("\n").getOrNull(0) ?: content
    }

    // ... other existing methods
}
```

### Step 5: Create migration in DatabaseModule

```kotlin
// core/database/src/main/java/com/example/sie/core/database/di/DatabaseModule.kt

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            // Check if column already exists (prevent duplicate migration)
            val cursor = database.query("PRAGMA table_info(questions)")
            val columnNames = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columnNames.add(cursor.getString(cursor.getColumnIndex("name")))
            }
            cursor.close()

            if ("wrongCount" !in columnNames) {
                database.execSQL(
                    "ALTER TABLE questions ADD COLUMN wrongCount INTEGER NOT NULL DEFAULT 0"
                )
                Log.d("Migration", "Successfully added wrongCount column")
            } else {
                Log.d("Migration", "wrongCount column already exists, skipping")
            }
        } catch (e: Exception) {
            Log.e("Migration", "Failed to migrate database from 9 to 10", e)
            throw e
        }
    }
}

@Provides
@Singleton
fun providesAppDatabase(
    @ApplicationContext context: Context,
): AppDatabase = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "app-database",
)
.addCallback(DatabaseCallback(context, CoroutineScope(SupervisorJob())))
.addMigrations(MIGRATION_9_10)  // ADD THIS
.build()
```

### Step 6: Update AppDatabase version

```kotlin
// core/database/src/main/java/com/example/sie/core/database/AppDatabase.kt

@Database(
    entities = [QuestionEntity::class, ExamResultEntity::class],
    version = 10,  // Changed from 9
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
}
```

### Step 7: Run migration test to verify it passes

```bash
./gradlew :core:database:connectedAndroidTest --tests MigrationTest.migrate9To10_addsWrongCountField
```

Expected: PASS with "wrongCount field added successfully"

### Step 8: Commit

```bash
git add core/database/ core/model/
git commit -m "feat(database): add wrongCount field to questions table

- Migrate database from version 9 to 10
- Add wrongCount field with default value 0
- Add migration test coverage
- Update Question domain model

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Task 2: DAO Layer - Add Wrong Question Methods

**Goal:** Extend QuestionDao with methods to query and manipulate wrong questions, with comprehensive test coverage.

**Files:**
- Modify: `core/database/src/main/java/com/example/sie/core/database/dao/QuestionDao.kt`
- Test: `core/database/src/test/java/com/example/sie/core/database/dao/QuestionDaoTest.kt`

### Step 1: Write DAO tests (test-first)

```kotlin
// core/database/src/test/java/com/example/sie/core/database/dao/QuestionDaoTest.kt

@RunWith(AndroidJUnit4::class)
class QuestionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var questionDao: QuestionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        questionDao = database.questionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun markAsWrong_setsIsWrongToTrueAndIncrementsCount() = runTest {
        // Given
        val question = createTestQuestion(id = 1, isWrong = false, wrongCount = 0)
        questionDao.insertAll(listOf(question))

        // When
        questionDao.markAsWrong(1)
        val result = questionDao.getQuestionById(1).first()

        // Then
        assertTrue(result?.isWrong ?: false)
        assertEquals(1, result?.wrongCount)
    }

    @Test
    fun markAsWrong_incrementsExistingCount() = runTest {
        // Given
        val question = createTestQuestion(id = 1, isWrong = true, wrongCount = 2)
        questionDao.insertAll(listOf(question))

        // When
        questionDao.markAsWrong(1)
        val result = questionDao.getQuestionById(1).first()

        // Then
        assertEquals(3, result?.wrongCount)
    }

    @Test
    fun markAsWrongBatch_marksMultipleQuestions() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, wrongCount = 0),
            createTestQuestion(id = 2, wrongCount = 1),
            createTestQuestion(id = 3, wrongCount = 0)
        )
        questionDao.insertAll(questions)

        // When
        questionDao.markAsWrongBatch(listOf(1, 3))
        val q1 = questionDao.getQuestionById(1).first()
        val q2 = questionDao.getQuestionById(2).first()
        val q3 = questionDao.getQuestionById(3).first()

        // Then
        assertTrue(q1?.isWrong ?: false)
        assertEquals(1, q1?.wrongCount)
        assertFalse(q2?.isWrong ?: true)  // Unchanged
        assertTrue(q3?.isWrong ?: false)
        assertEquals(1, q3?.wrongCount)
    }

    @Test
    fun getWrongQuestions_returnsOnlyWrongQuestions() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, isWrong = true, wrongCount = 2),
            createTestQuestion(id = 2, isWrong = false, wrongCount = 0),
            createTestQuestion(id = 3, isWrong = true, wrongCount = 5)
        )
        questionDao.insertAll(questions)

        // When
        val wrong = questionDao.getWrongQuestions().first()

        // Then
        assertEquals(2, wrong.size)
        assertTrue(wrong.all { it.isWrong })
    }

    @Test
    fun getWrongQuestions_orderedByWrongCountDescending() = runTest {
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
        assertEquals(5, wrong[0].wrongCount)  // Highest first
        assertEquals(3, wrong[1].wrongCount)
        assertEquals(1, wrong[2].wrongCount)
    }

    @Test
    fun removeFromWrongList_setsIsWrongToFalse() = runTest {
        // Given
        val question = createTestQuestion(id = 1, isWrong = true, wrongCount = 3)
        questionDao.insertAll(listOf(question))

        // When
        questionDao.removeFromWrongList(1)
        val result = questionDao.getQuestionById(1).first()

        // Then
        assertFalse(result?.isWrong ?: true)
        assertEquals(3, result?.wrongCount)  // Count preserved
    }

    @Test
    fun getWrongQuestionsByCategory_filtersCorrectly() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A", isWrong = true),
            createTestQuestion(id = 2, category = "Category B", isWrong = true),
            createTestQuestion(id = 3, category = "Category A", isWrong = true)
        )
        questionDao.insertAll(questions)

        // When
        val wrongInA = questionDao.getWrongQuestionsByCategory("Category A").first()

        // Then
        assertEquals(2, wrongInA.size)
        assertTrue(wrongInA.all { it.category == "Category A" })
    }

    private fun createTestQuestion(
        id: Int,
        category: String = "Test Category",
        isBookmarked: Boolean = false,
        isWrong: Boolean = false,
        wrongCount: Int = 0
    ) = QuestionEntity(
        id = id,
        content = "Question $id",
        options = "[\"A\", \"B\", \"C\"]",
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = category,
        isBookmarked = isBookmarked,
        isWrong = isWrong,
        wrongCount = wrongCount
    )
}
```

### Step 2: Run tests to verify they fail

```bash
./gradlew :core:database:testDebugUnitTest --tests QuestionDaoTest
```

Expected: FAIL with "method not found" errors for new DAO methods.

### Step 3: Implement DAO methods

```kotlin
// core/database/src/main/java/com/example/sie/core/database/dao/QuestionDao.kt

@Dao
interface QuestionDao {

    // ========== Existing Methods (keep as-is) ==========
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    fun getQuestionById(id: Int): Flow<QuestionEntity?>

    @Query("UPDATE questions SET isBookmarked = NOT isBookmarked WHERE id = :questionId")
    suspend fun toggleBookmark(questionId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    // ========== NEW Methods ==========

    @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY category, id")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions
        WHERE isWrong = 1
        ORDER BY wrongCount DESC, category, id
    """)
    fun getWrongQuestions(): Flow<List<QuestionEntity>>

    @Query("""
        UPDATE questions
        SET isWrong = 1, wrongCount = wrongCount + 1
        WHERE id = :questionId
    """)
    suspend fun markAsWrong(questionId: Int)

    @Query("""
        UPDATE questions
        SET isWrong = 1, wrongCount = wrongCount + 1
        WHERE id IN (:questionIds)
    """)
    suspend fun markAsWrongBatch(questionIds: List<Int>)

    @Query("UPDATE questions SET isWrong = 0 WHERE id = :questionId")
    suspend fun removeFromWrongList(questionId: Int)

    @Query("""
        SELECT * FROM questions
        WHERE isBookmarked = 1 AND category = :category
        ORDER BY id
    """)
    fun getBookmarkedQuestionsByCategory(category: String): Flow<List<QuestionEntity>>

    @Query("""
        SELECT * FROM questions
        WHERE isWrong = 1 AND category = :category
        ORDER BY wrongCount DESC, id
    """)
    fun getWrongQuestionsByCategory(category: String): Flow<List<QuestionEntity>>
}
```

### Step 4: Run tests to verify they pass

```bash
./gradlew :core:database:testDebugUnitTest --tests QuestionDaoTest
```

Expected: PASS - all 8 new tests passing.

### Step 5: Commit

```bash
git add core/database/
git commit -m "feat(dao): add wrong question query and manipulation methods

- Add markAsWrong() and markAsWrongBatch() methods
- Add getWrongQuestions() with sorting by wrongCount DESC
- Add removeFromWrongList() method
- Add category-filtered query methods
- Add comprehensive DAO test coverage (8 new tests)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Task 3: Repository Layer - Extend QuestionRepository

**Goal:** Add repository methods for bookmarked and wrong questions with error handling and test coverage.

**Files:**
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/QuestionRepository.kt`
- Modify: `core/data/src/main/java/com/example/sie/core/data/repository/OfflineQuestionRepository.kt`
- Create: `core/data/src/main/java/com/example/sie/core/data/exception/QuestionExceptions.kt`
- Test: `core/data/src/test/java/com/example/sie/core/data/repository/OfflineQuestionRepositoryTest.kt`

### Step 1: Create custom exceptions

```kotlin
// core/data/src/main/java/com/example/sie/core/data/exception/QuestionExceptions.kt
package com.example.sie.core.data.exception

class BookmarkException(message: String, cause: Throwable? = null) : Exception(message, cause)
class WrongQuestionException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

### Step 2: Write repository tests (test-first)

```kotlin
// core/data/src/test/java/com/example/sie/core/data/repository/OfflineQuestionRepositoryTest.kt

@RunWith(JUnit4::class)
class OfflineQuestionRepositoryTest {

    private lateinit var repository: OfflineQuestionRepository
    private val questionDao: QuestionDao = mockk()

    @Before
    fun setup() {
        repository = OfflineQuestionRepository(questionDao)
    }

    @Test
    fun `getBookmarkedQuestions returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            createTestEntity(id = 1, isBookmarked = true),
            createTestEntity(id = 2, isBookmarked = true)
        )
        coEvery { questionDao.getBookmarkedQuestions() } returns flowOf(entities)

        // When
        val result = repository.getBookmarkedQuestions().first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isBookmarked })
    }

    @Test
    fun `getWrongQuestions returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            createTestEntity(id = 1, isWrong = true, wrongCount = 3),
            createTestEntity(id = 2, isWrong = true, wrongCount = 1)
        )
        coEvery { questionDao.getWrongQuestions() } returns flowOf(entities)

        // When
        val result = repository.getWrongQuestions().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(3, result[0].wrongCount)
        assertEquals(1, result[1].wrongCount)
    }

    @Test
    fun `toggleBookmark throws BookmarkException on failure`() = runTest {
        // Given
        coEvery { questionDao.toggleBookmark(any()) } throws SQLiteException("DB error")

        // When / Then
        assertThrows<BookmarkException> {
            repository.toggleBookmark(1)
        }
    }

    @Test
    fun `markAsWrong throws WrongQuestionException on failure`() = runTest {
        // Given
        coEvery { questionDao.markAsWrong(any()) } throws SQLiteException("DB error")

        // When / Then
        assertThrows<WrongQuestionException> {
            repository.markAsWrong(1)
        }
    }

    @Test
    fun `markAsWrongBatch retries individually on batch failure`() = runTest {
        // Given
        val questionIds = listOf(1, 2, 3)
        coEvery { questionDao.markAsWrongBatch(questionIds) } throws SQLiteException("Batch failed")
        coEvery { questionDao.markAsWrong(1) } just Runs
        coEvery { questionDao.markAsWrong(2) } throws SQLiteException("Failed")
        coEvery { questionDao.markAsWrong(3) } just Runs

        // When / Then
        val exception = assertThrows<WrongQuestionException> {
            repository.markAsWrongBatch(questionIds)
        }

        // Verify individual calls were made
        coVerify { questionDao.markAsWrong(1) }
        coVerify { questionDao.markAsWrong(2) }
        coVerify { questionDao.markAsWrong(3) }

        // Check error message mentions failed question
        assertTrue(exception.message?.contains("2") ?: false)
    }

    private fun createTestEntity(
        id: Int,
        category: String = "Test",
        isBookmarked: Boolean = false,
        isWrong: Boolean = false,
        wrongCount: Int = 0
    ) = QuestionEntity(
        id = id,
        content = "Question $id",
        options = "[\"A\", \"B\"]",
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = category,
        isBookmarked = isBookmarked,
        isWrong = isWrong,
        wrongCount = wrongCount
    )
}
```

### Step 3: Run tests to verify they fail

```bash
./gradlew :core:data:testDebugUnitTest --tests OfflineQuestionRepositoryTest
```

Expected: FAIL with "method not found" errors.

### Step 4: Extend QuestionRepository interface

```kotlin
// core/data/src/main/java/com/example/sie/core/data/repository/QuestionRepository.kt

interface QuestionRepository {
    // ========== Existing ==========
    fun getAllQuestions(): Flow<List<Question>>
    fun getQuestionById(id: Int): Flow<Question?>
    suspend fun toggleBookmark(questionId: Int)

    // ========== NEW ==========
    fun getBookmarkedQuestions(): Flow<List<Question>>
    fun getBookmarkedQuestionsByCategory(category: String): Flow<List<Question>>
    fun getWrongQuestions(): Flow<List<Question>>
    fun getWrongQuestionsByCategory(category: String): Flow<List<Question>>
    suspend fun markAsWrong(questionId: Int)
    suspend fun markAsWrongBatch(questionIds: List<Int>)
    suspend fun removeFromWrongList(questionId: Int)
}
```

### Step 5: Implement repository methods with error handling

```kotlin
// core/data/src/main/java/com/example/sie/core/data/repository/OfflineQuestionRepository.kt

class OfflineQuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    // ========== Existing implementations ==========
    override fun getAllQuestions(): Flow<List<Question>> =
        questionDao.getAllQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getQuestionById(id: Int): Flow<Question?> =
        questionDao.getQuestionById(id).map { it?.asExternalModel() }

    override suspend fun toggleBookmark(questionId: Int) {
        try {
            questionDao.toggleBookmark(questionId)
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Failed to toggle bookmark for question $questionId", e)
            throw BookmarkException("Failed to update bookmark status", e)
        }
    }

    // ========== NEW implementations ==========
    override fun getBookmarkedQuestions(): Flow<List<Question>> =
        questionDao.getBookmarkedQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getBookmarkedQuestionsByCategory(category: String): Flow<List<Question>> =
        questionDao.getBookmarkedQuestionsByCategory(category).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getWrongQuestions(): Flow<List<Question>> =
        questionDao.getWrongQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getWrongQuestionsByCategory(category: String): Flow<List<Question>> =
        questionDao.getWrongQuestionsByCategory(category).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun markAsWrong(questionId: Int) {
        try {
            questionDao.markAsWrong(questionId)
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Failed to mark question $questionId as wrong", e)
            throw WrongQuestionException("Failed to mark question as wrong", e)
        }
    }

    override suspend fun markAsWrongBatch(questionIds: List<Int>) {
        try {
            questionDao.markAsWrongBatch(questionIds)
        } catch (e: Exception) {
            Log.w("QuestionRepository", "Batch mark failed, trying one by one", e)
            val failures = mutableListOf<Int>()
            questionIds.forEach { id ->
                try {
                    questionDao.markAsWrong(id)
                } catch (ex: Exception) {
                    failures.add(id)
                    Log.e("QuestionRepository", "Failed to mark question $id", ex)
                }
            }
            if (failures.isNotEmpty()) {
                throw WrongQuestionException("Failed to mark ${failures.size} questions: $failures")
            }
        }
    }

    override suspend fun removeFromWrongList(questionId: Int) {
        try {
            questionDao.removeFromWrongList(questionId)
        } catch (e: Exception) {
            Log.e("QuestionRepository", "Failed to remove question $questionId from wrong list", e)
            throw WrongQuestionException("Failed to remove from wrong list", e)
        }
    }
}
```

### Step 6: Run tests to verify they pass

```bash
./gradlew :core:data:testDebugUnitTest --tests OfflineQuestionRepositoryTest
```

Expected: PASS - all 5 repository tests passing.

### Step 7: Commit

```bash
git add core/data/
git commit -m "feat(repository): extend QuestionRepository with bookmark and wrong question methods

- Add getBookmarkedQuestions() and getWrongQuestions() methods
- Add category-filtered query methods
- Add markAsWrong(), markAsWrongBatch(), removeFromWrongList()
- Implement error handling with custom exceptions
- Add fallback retry logic for batch operations
- Add comprehensive repository test coverage

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Task 4: BookmarkedViewModel - Create Bookmark Management Screen Logic

**Goal:** Create BookmarkedViewModel with state management, category filtering, and error handling.

**Files:**
- Create: `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedViewModel.kt`
- Create: `feature/home/src/main/java/com/example/sie/feature/home/BookmarkedUiState.kt`
- Test: `feature/home/src/test/java/com/example/sie/feature/home/BookmarkedViewModelTest.kt`

### Step 1: Create UI state definition

```kotlin
// feature/home/src/main/java/com/example/sie/feature/home/BookmarkedUiState.kt
package com.example.sie.feature.home

import com.example.sie.core.model.Question

sealed interface BookmarkedUiState {
    data object Loading : BookmarkedUiState
    data object Empty : BookmarkedUiState
    data class Success(
        val questions: List<Question>,
        val categories: List<String>
    ) : BookmarkedUiState
}
```

### Step 2: Write ViewModel tests (test-first)

```kotlin
// feature/home/src/test/java/com/example/sie/feature/home/BookmarkedViewModelTest.kt

@RunWith(JUnit4::class)
class BookmarkedViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: BookmarkedViewModel
    private val questionRepository: QuestionRepository = mockk()

    @Before
    fun setup() {
        viewModel = BookmarkedViewModel(questionRepository)
    }

    @Test
    fun `uiState is Loading initially`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state is BookmarkedUiState.Loading)
    }

    @Test
    fun `uiState is Empty when no bookmarked questions`() = runTest {
        // Given
        coEvery { questionRepository.getBookmarkedQuestions() } returns flowOf(emptyList())

        // When
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Then
        assertTrue(state is BookmarkedUiState.Empty)
    }

    @Test
    fun `uiState is Success with all categories`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A"),
            createTestQuestion(id = 2, category = "Category B"),
            createTestQuestion(id = 3, category = "Category A")
        )
        coEvery { questionRepository.getBookmarkedQuestions() } returns flowOf(questions)

        // When
        advanceUntilIdle()
        val state = viewModel.uiState.value as BookmarkedUiState.Success

        // Then
        assertEquals(3, state.questions.size)
        assertEquals(listOf("Category A", "Category B"), state.categories)
    }

    @Test
    fun `selectCategory filters questions correctly`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A"),
            createTestQuestion(id = 2, category = "Category B"),
            createTestQuestion(id = 3, category = "Category A")
        )
        coEvery { questionRepository.getBookmarkedQuestions() } returns flowOf(questions)

        // When
        advanceUntilIdle()
        viewModel.selectCategory("Category A")
        advanceUntilIdle()
        val state = viewModel.uiState.value as BookmarkedUiState.Success

        // Then
        assertEquals(2, state.questions.size)
        assertTrue(state.questions.all { it.category == "Category A" })
    }

    @Test
    fun `selectCategory null shows all questions`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A"),
            createTestQuestion(id = 2, category = "Category B")
        )
        coEvery { questionRepository.getBookmarkedQuestions() } returns flowOf(questions)

        // When
        advanceUntilIdle()
        viewModel.selectCategory("Category A")
        advanceUntilIdle()
        viewModel.selectCategory(null)  // Reset filter
        advanceUntilIdle()
        val state = viewModel.uiState.value as BookmarkedUiState.Success

        // Then
        assertEquals(2, state.questions.size)
    }

    @Test
    fun `toggleBookmark emits error event on failure`() = runTest {
        // Given
        coEvery { questionRepository.toggleBookmark(any()) } throws BookmarkException("Failed")
        val errors = mutableListOf<String>()
        val job = launch {
            viewModel.errorEvent.collect { errors.add(it) }
        }

        // When
        viewModel.toggleBookmark(1)
        advanceUntilIdle()

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("Failed to update bookmark"))

        job.cancel()
    }

    private fun createTestQuestion(
        id: Int,
        category: String = "Test"
    ) = Question(
        id = id,
        content = "Question $id",
        options = listOf("A", "B"),
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = category,
        isBookmarked = true,
        isWrong = false,
        wrongCount = 0
    )
}
```

### Step 3: Run tests to verify they fail

```bash
./gradlew :feature:home:testDebugUnitTest --tests BookmarkedViewModelTest
```

Expected: FAIL with "BookmarkedViewModel not found".

### Step 4: Implement BookmarkedViewModel

```kotlin
// feature/home/src/main/java/com/example/sie/feature/home/BookmarkedViewModel.kt

@HiltViewModel
class BookmarkedViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

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

            val categories = allBookmarked
                .map { it.category }
                .distinct()
                .sorted()

            BookmarkedUiState.Success(
                questions = filtered,
                categories = categories
            )
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
            try {
                questionRepository.toggleBookmark(questionId)
            } catch (e: BookmarkException) {
                Log.e("BookmarkedViewModel", "Toggle bookmark failed", e)
                _errorEvent.emit("Failed to update bookmark. Please try again.")
            } catch (e: Exception) {
                Log.e("BookmarkedViewModel", "Unexpected error", e)
                _errorEvent.emit("An unexpected error occurred.")
            }
        }
    }
}
```

### Step 5: Run tests to verify they pass

```bash
./gradlew :feature:home:testDebugUnitTest --tests BookmarkedViewModelTest
```

Expected: PASS - all 6 ViewModel tests passing.

### Step 6: Commit

```bash
git add feature/home/
git commit -m "feat(viewmodel): create BookmarkedViewModel with category filtering

- Create BookmarkedUiState (Loading, Empty, Success)
- Implement category filtering with StateFlow
- Add error handling with SharedFlow events
- Add comprehensive ViewModel test coverage (6 tests)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

## Task 5: WrongQuestionsViewModel - Create Wrong Questions Management Logic

**Goal:** Create WrongQuestionsViewModel with statistics calculation, category filtering, and remove functionality.

**Files:**
- Create: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt`
- Create: `feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsUiState.kt`
- Test: `feature/home/src/test/java/com/example/sie/feature/home/WrongQuestionsViewModelTest.kt`

### Step 1: Create UI state and stats models

```kotlin
// feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsUiState.kt
package com.example.sie.feature.home

import com.example.sie.core.model.Question

sealed interface WrongQuestionsUiState {
    data object Loading : WrongQuestionsUiState
    data object Empty : WrongQuestionsUiState
    data class Success(
        val questions: List<Question>,
        val categories: List<String>,
        val stats: WrongQuestionsStats
    ) : WrongQuestionsUiState
}

data class WrongQuestionsStats(
    val totalCount: Int,
    val totalWrongCount: Int,
    val avgWrongCount: Float
)
```

### Step 2: Write ViewModel tests (test-first)

```kotlin
// feature/home/src/test/java/com/example/sie/feature/home/WrongQuestionsViewModelTest.kt

@RunWith(JUnit4::class)
class WrongQuestionsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WrongQuestionsViewModel
    private val questionRepository: QuestionRepository = mockk()

    @Before
    fun setup() {
        viewModel = WrongQuestionsViewModel(questionRepository)
    }

    @Test
    fun `uiState is Loading initially`() {
        // When
        val state = viewModel.uiState.value

        // Then
        assertTrue(state is WrongQuestionsUiState.Loading)
    }

    @Test
    fun `uiState is Empty when no wrong questions`() = runTest {
        // Given
        coEvery { questionRepository.getWrongQuestions() } returns flowOf(emptyList())

        // When
        advanceUntilIdle()
        val state = viewModel.uiState.value

        // Then
        assertTrue(state is WrongQuestionsUiState.Empty)
    }

    @Test
    fun `uiState calculates stats correctly`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, wrongCount = 3),
            createTestQuestion(id = 2, wrongCount = 1),
            createTestQuestion(id = 3, wrongCount = 2)
        )
        coEvery { questionRepository.getWrongQuestions() } returns flowOf(questions)

        // When
        advanceUntilIdle()
        val state = viewModel.uiState.value as WrongQuestionsUiState.Success

        // Then
        assertEquals(3, state.stats.totalCount)
        assertEquals(6, state.stats.totalWrongCount)  // 3 + 1 + 2
        assertEquals(2.0f, state.stats.avgWrongCount, 0.01f)  // 6 / 3
    }

    @Test
    fun `selectCategory filters questions correctly`() = runTest {
        // Given
        val questions = listOf(
            createTestQuestion(id = 1, category = "Category A", wrongCount = 2),
            createTestQuestion(id = 2, category = "Category B", wrongCount = 1),
            createTestQuestion(id = 3, category = "Category A", wrongCount = 3)
        )
        coEvery { questionRepository.getWrongQuestions() } returns flowOf(questions)

        // When
        advanceUntilIdle()
        viewModel.selectCategory("Category A")
        advanceUntilIdle()
        val state = viewModel.uiState.value as WrongQuestionsUiState.Success

        // Then
        assertEquals(2, state.questions.size)
        assertTrue(state.questions.all { it.category == "Category A" })
        // Stats should still be based on ALL wrong questions
        assertEquals(3, state.stats.totalCount)
    }

    @Test
    fun `removeFromWrongList calls repository method`() = runTest {
        // Given
        coEvery { questionRepository.removeFromWrongList(any()) } just Runs

        // When
        viewModel.removeFromWrongList(1)
        advanceUntilIdle()

        // Then
        coVerify { questionRepository.removeFromWrongList(1) }
    }

    @Test
    fun `removeFromWrongList emits error event on failure`() = runTest {
        // Given
        coEvery { questionRepository.removeFromWrongList(any()) } throws WrongQuestionException("Failed")
        val errors = mutableListOf<String>()
        val job = launch {
            viewModel.errorEvent.collect { errors.add(it) }
        }

        // When
        viewModel.removeFromWrongList(1)
        advanceUntilIdle()

        // Then
        assertEquals(1, errors.size)
        assertTrue(errors[0].contains("Failed to remove"))

        job.cancel()
    }

    private fun createTestQuestion(
        id: Int,
        category: String = "Test",
        wrongCount: Int = 1
    ) = Question(
        id = id,
        content = "Question $id",
        options = listOf("A", "B"),
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = category,
        isBookmarked = false,
        isWrong = true,
        wrongCount = wrongCount
    )
}
```

### Step 3: Run tests to verify they fail

```bash
./gradlew :feature:home:testDebugUnitTest --tests WrongQuestionsViewModelTest
```

Expected: FAIL with "WrongQuestionsViewModel not found".

### Step 4: Implement WrongQuestionsViewModel

```kotlin
// feature/home/src/main/java/com/example/sie/feature/home/WrongQuestionsViewModel.kt

@HiltViewModel
class WrongQuestionsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    val uiState: StateFlow<WrongQuestionsUiState> = combine(
        questionRepository.getWrongQuestions(),
        _selectedCategory
    ) { allWrong, category ->
        if (allWrong.isEmpty()) {
            WrongQuestionsUiState.Empty
        } else {
            // Calculate stats from ALL wrong questions
            val stats = WrongQuestionsStats(
                totalCount = allWrong.size,
                totalWrongCount = allWrong.sumOf { it.wrongCount },
                avgWrongCount = allWrong.map { it.wrongCount }.average().toFloat()
            )

            // Filter questions by category
            val filtered = if (category != null) {
                allWrong.filter { it.category == category }
            } else {
                allWrong
            }

            val categories = allWrong
                .map { it.category }
                .distinct()
                .sorted()

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
            try {
                questionRepository.removeFromWrongList(questionId)
            } catch (e: WrongQuestionException) {
                Log.e("WrongQuestionsViewModel", "Remove from wrong list failed", e)
                _errorEvent.emit("Failed to remove question. Please try again.")
            } catch (e: Exception) {
                Log.e("WrongQuestionsViewModel", "Unexpected error", e)
                _errorEvent.emit("An unexpected error occurred.")
            }
        }
    }
}
```

### Step 5: Run tests to verify they pass

```bash
./gradlew :feature:home:testDebugUnitTest --tests WrongQuestionsViewModelTest
```

Expected: PASS - all 6 ViewModel tests passing.

### Step 6: Commit

```bash
git add feature/home/
git commit -m "feat(viewmodel): create WrongQuestionsViewModel with stats and filtering

- Create WrongQuestionsUiState with stats model
- Calculate totalCount, totalWrongCount, avgWrongCount
- Implement category filtering
- Add removeFromWrongList() with error handling
- Add comprehensive ViewModel test coverage (6 tests)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```

---

**Note:** This implementation plan continues with Tasks 6-12 covering:
- Task 6: BookmarkedScreen UI
- Task 7: WrongQuestionsScreen UI
- Task 8: Update StudyViewModel with bookmark and wrong marking
- Task 9: Update ExamViewModel with batch wrong marking
- Task 10: Update HomeScreen with new entry cards
- Task 11: Navigation setup
- Task 12: Integration testing

Due to length constraints, the remaining tasks follow the same TDD pattern. Would you like me to continue with the remaining tasks, or would you prefer to start implementation with these first 5 tasks?

---

## Execution Options

**Plan saved to:** `docs/plans/2026-02-28-study-enhancement-implementation.md`

**Ready to implement? Choose one:**

1. **Subagent-Driven (this session)** - Fast iteration with review checkpoints
2. **Parallel Session (separate)** - Batch execution with independent session

Which approach would you like to use?
