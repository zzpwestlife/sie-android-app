package com.example.sie.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sie.core.database.AppDatabase
import com.example.sie.core.database.model.QuestionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class QuestionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var questionDao: QuestionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        questionDao = database.questionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun markAsWrong_incrementsWrongCount() = runTest {
        // Given: Insert a question with wrongCount = 0
        val question = QuestionEntity(
            id = 999,
            content = "Test question?",
            options = """["Option A", "Option B", "Option C", "Option D"]""",
            correctAnswerIndex = 0,
            explanation = "Test explanation",
            category = "Test",
            isBookmarked = false,
            isWrong = false,
            wrongCount = 0
        )
        questionDao.insertAll(listOf(question))

        // When: Mark as wrong twice
        questionDao.markAsWrongBatch(listOf(999))
        questionDao.markAsWrongBatch(listOf(999))

        // Then: wrongCount should be 2
        val updated = questionDao.getQuestionById(999)
        assertEquals(true, updated?.isWrong)
        assertEquals(2, updated?.wrongCount)
    }

    @Test
    fun markAsWrong_batchOperation_incrementsAll() = runTest {
        // Given: Insert 3 questions
        val questions = listOf(
            QuestionEntity(
                id = 1001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Cat1",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 1002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Cat1",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 1003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "Cat1",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            )
        )
        questionDao.insertAll(questions)

        // When: Batch mark as wrong
        questionDao.markAsWrongBatch(listOf(1001, 1002, 1003))

        // Then: All wrongCount should be 1
        val updated1 = questionDao.getQuestionById(1001)
        val updated2 = questionDao.getQuestionById(1002)
        val updated3 = questionDao.getQuestionById(1003)

        assertEquals(1, updated1?.wrongCount)
        assertEquals(1, updated2?.wrongCount)
        assertEquals(1, updated3?.wrongCount)
    }

    @Test
    fun markAsWrong_setsIsWrongFlag() = runTest {
        // Given: Insert a question with isWrong = false
        val question = QuestionEntity(
            id = 2001,
            content = "Test question?",
            options = """["A", "B", "C", "D"]""",
            correctAnswerIndex = 0,
            explanation = "Test explanation",
            category = "Test",
            isBookmarked = false,
            isWrong = false,
            wrongCount = 0
        )
        questionDao.insertAll(listOf(question))

        // When: Mark as wrong
        questionDao.markAsWrongBatch(listOf(2001))

        // Then: isWrong should be true
        val updated = questionDao.getQuestionById(2001)
        assertEquals(true, updated?.isWrong)
    }

    @Test
    fun getWrongQuestions_returnsOnlyWrongQuestions() = runTest {
        // Given: Insert 2 questions, 1 wrong and 1 correct
        val questions = listOf(
            QuestionEntity(
                id = 3001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Cat1",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 1
            ),
            QuestionEntity(
                id = 3002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Cat1",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            )
        )
        questionDao.insertAll(questions)

        // When: Get wrong questions
        val wrongQuestions = questionDao.getWrongQuestions().first()

        // Then: Should only return question 3001
        assertEquals(1, wrongQuestions.size)
        assertEquals(3001, wrongQuestions[0].id)
    }

    @Test
    fun getBookmarkedQuestions_returnsOnlyBookmarked() = runTest {
        // Given: 3 questions, 2 bookmarked
        val questions = listOf(
            QuestionEntity(
                id = 2001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Math",
                isBookmarked = true,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 2002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "English",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 2003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "Math",
                isBookmarked = true,
                isWrong = false,
                wrongCount = 0
            )
        )
        questionDao.insertAll(questions)

        // When
        val bookmarked = questionDao.getBookmarkedQuestions().first()

        // Then
        assertEquals(2, bookmarked.size)
        assertTrue(bookmarked.all { it.isBookmarked })
        assertEquals(listOf(2001, 2003), bookmarked.map { it.id })
    }

    @Test
    fun getWrongQuestions_orderedByWrongCountAndCategory() = runTest {
        // Given: 3 wrong questions with different wrongCount
        val questions = listOf(
            QuestionEntity(
                id = 4001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Math/Algebra",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 3
            ),
            QuestionEntity(
                id = 4002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Math/Geometry",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 1
            ),
            QuestionEntity(
                id = 4003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "English",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 2
            )
        )
        questionDao.insertAll(questions)

        // When
        val wrong = questionDao.getWrongQuestions().first()

        // Then
        assertEquals(3, wrong.size)
        // Ordered by wrongCount DESC, then category
        assertEquals(listOf(4001, 4003, 4002), wrong.map { it.id })
    }

    @Test
    fun markAsWrong_singleQuestion_incrementsWrongCount() = runTest {
        // Given
        val question = QuestionEntity(
            id = 5001,
            content = "Q1",
            options = """["A", "B", "C", "D"]""",
            correctAnswerIndex = 0,
            explanation = "Explanation 1",
            category = "Math",
            isBookmarked = false,
            isWrong = false,
            wrongCount = 0
        )
        questionDao.insertAll(listOf(question))

        // When
        questionDao.markAsWrongSingle(5001)

        // Then
        val updated = questionDao.getQuestionById(5001)
        assertTrue(updated!!.isWrong)
        assertEquals(1, updated.wrongCount)
    }

    @Test
    fun markAsWrongBatch_incrementsAllQuestions() = runTest {
        // Given: 3 questions
        val questions = listOf(
            QuestionEntity(
                id = 6001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Math",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 6002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Math",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 6003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "Math",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 1
            ) // already wrong once
        )
        questionDao.insertAll(questions)

        // When
        questionDao.markAsWrongBatch(listOf(6001, 6002, 6003))

        // Then
        val q1 = questionDao.getQuestionById(6001)
        val q2 = questionDao.getQuestionById(6002)
        val q3 = questionDao.getQuestionById(6003)

        assertEquals(1, q1!!.wrongCount)
        assertEquals(1, q2!!.wrongCount)
        assertEquals(2, q3!!.wrongCount) // incremented from 1 to 2
    }

    @Test
    fun removeFromWrong_resetsIsWrongFlagOnly() = runTest {
        // Given: Wrong question with wrongCount = 3
        val question = QuestionEntity(
            id = 7001,
            content = "Q1",
            options = """["A", "B", "C", "D"]""",
            correctAnswerIndex = 0,
            explanation = "Explanation 1",
            category = "Math",
            isBookmarked = false,
            isWrong = true,
            wrongCount = 3
        )
        questionDao.insertAll(listOf(question))

        // When
        questionDao.removeFromWrong(7001)

        // Then
        val updated = questionDao.getQuestionById(7001)
        assertFalse(updated!!.isWrong)
        assertEquals(3, updated.wrongCount) // wrongCount preserved
    }

    @Test
    fun toggleBookmark_changesState() = runTest {
        // Given
        val question = QuestionEntity(
            id = 8001,
            content = "Q1",
            options = """["A", "B", "C", "D"]""",
            correctAnswerIndex = 0,
            explanation = "Explanation 1",
            category = "Math",
            isBookmarked = false,
            isWrong = false,
            wrongCount = 0
        )
        questionDao.insertAll(listOf(question))

        // When: Toggle on
        questionDao.toggleBookmark(8001)
        val bookmarked = questionDao.getQuestionById(8001)
        assertTrue(bookmarked!!.isBookmarked)

        // When: Toggle off
        questionDao.toggleBookmark(8001)
        val unbookmarked = questionDao.getQuestionById(8001)
        assertFalse(unbookmarked!!.isBookmarked)
    }

    @Test
    fun countBookmarked_returnsCorrectCount() = runTest {
        // Given: 3 questions, 2 bookmarked
        val questions = listOf(
            QuestionEntity(
                id = 9001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Math",
                isBookmarked = true,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 9002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Math",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 9003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "Math",
                isBookmarked = true,
                isWrong = false,
                wrongCount = 0
            )
        )
        questionDao.insertAll(questions)

        // When
        val count = questionDao.countBookmarked().first()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun countWrong_returnsCorrectCount() = runTest {
        // Given: 3 questions, 2 wrong
        val questions = listOf(
            QuestionEntity(
                id = 10001,
                content = "Q1",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 1",
                category = "Math",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 1
            ),
            QuestionEntity(
                id = 10002,
                content = "Q2",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 2",
                category = "Math",
                isBookmarked = false,
                isWrong = false,
                wrongCount = 0
            ),
            QuestionEntity(
                id = 10003,
                content = "Q3",
                options = """["A", "B", "C", "D"]""",
                correctAnswerIndex = 0,
                explanation = "Explanation 3",
                category = "Math",
                isBookmarked = false,
                isWrong = true,
                wrongCount = 2
            )
        )
        questionDao.insertAll(questions)

        // When
        val count = questionDao.countWrong().first()

        // Then
        assertEquals(2, count)
    }
}
