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
        questionDao.markAsWrong(listOf(999))
        questionDao.markAsWrong(listOf(999))

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
        questionDao.markAsWrong(listOf(1001, 1002, 1003))

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
        questionDao.markAsWrong(listOf(2001))

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
}
