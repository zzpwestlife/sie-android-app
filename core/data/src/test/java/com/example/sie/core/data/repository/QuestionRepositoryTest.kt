package com.example.sie.core.data.repository

import com.example.sie.core.data.exception.BookmarkException
import com.example.sie.core.data.exception.WrongQuestionException
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.QuestionEntity
import com.example.sie.core.model.Question
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuestionRepositoryTest {

    private lateinit var questionDao: QuestionDao
    private lateinit var repository: QuestionRepository

    @Before
    fun setup() {
        questionDao = mockk()
        repository = OfflineQuestionRepository(questionDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getBookmarkedQuestions_returnsFlowOfQuestions() = runTest {
        // Given
        val entities = listOf(
            QuestionEntity(1, "Q1", """["A"]""", 0, "E", "Math", isBookmarked = true, isWrong = false, wrongCount = 0)
        )
        every { questionDao.getBookmarkedQuestions() } returns flowOf(entities)

        // When
        val result = repository.getBookmarkedQuestions().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Q1", result[0].content)
        verify { questionDao.getBookmarkedQuestions() }
    }

    @Test
    fun getWrongQuestions_returnsFlowOfQuestions() = runTest {
        // Given
        val entities = listOf(
            QuestionEntity(1, "Q1", """["A"]""", 0, "E", "Math", isBookmarked = false, isWrong = true, wrongCount = 2)
        )
        every { questionDao.getWrongQuestions() } returns flowOf(entities)

        // When
        val result = repository.getWrongQuestions().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(2, result[0].wrongCount)
    }

    @Test
    fun markAsWrong_callsDao() = runTest {
        // Given
        coEvery { questionDao.markAsWrong(1) } just Runs

        // When
        repository.markAsWrong(1)

        // Then
        coVerify { questionDao.markAsWrong(1) }
    }

    @Test
    fun markAsWrong_throwsWrongQuestionException_onDaoFailure() = runTest {
        // Given
        coEvery { questionDao.markAsWrong(1) } throws Exception("DB error")

        // When & Then
        assertFailsWith<WrongQuestionException> {
            repository.markAsWrong(1)
        }
    }

    @Test
    fun markAsWrongBatch_successfulBatchOperation() = runTest {
        // Given
        val ids = listOf(1, 2, 3)
        coEvery { questionDao.markAsWrongBatch(ids) } just Runs

        // When
        repository.markAsWrongBatch(ids)

        // Then
        coVerify { questionDao.markAsWrongBatch(ids) }
    }

    @Test
    fun markAsWrongBatch_retriesIndividually_onBatchFailure() = runTest {
        // Given: Batch fails but individual succeeds
        val ids = listOf(1, 2, 3)
        coEvery { questionDao.markAsWrongBatch(ids) } throws Exception("Batch failed")
        coEvery { questionDao.markAsWrong(1) } just Runs
        coEvery { questionDao.markAsWrong(2) } just Runs
        coEvery { questionDao.markAsWrong(3) } just Runs

        // When
        repository.markAsWrongBatch(ids)

        // Then: Should retry individually
        coVerify { questionDao.markAsWrongBatch(ids) }
        coVerify { questionDao.markAsWrong(1) }
        coVerify { questionDao.markAsWrong(2) }
        coVerify { questionDao.markAsWrong(3) }
    }

    @Test
    fun markAsWrongBatch_throwsException_withFailedIds() = runTest {
        // Given: Batch fails, and ID 2 fails individually
        val ids = listOf(1, 2, 3)
        coEvery { questionDao.markAsWrongBatch(ids) } throws Exception("Batch failed")
        coEvery { questionDao.markAsWrong(1) } just Runs
        coEvery { questionDao.markAsWrong(2) } throws Exception("Failed")
        coEvery { questionDao.markAsWrong(3) } just Runs

        // When & Then
        val exception = assertFailsWith<WrongQuestionException> {
            repository.markAsWrongBatch(ids)
        }
        assertTrue(exception.message!!.contains("Failed to mark 1 questions"))
        assertTrue(exception.message!!.contains("[2]"))
    }

    @Test
    fun removeFromWrong_callsDao() = runTest {
        // Given
        coEvery { questionDao.removeFromWrong(1) } just Runs

        // When
        repository.removeFromWrong(1)

        // Then
        coVerify { questionDao.removeFromWrong(1) }
    }

    @Test
    fun toggleBookmark_callsDao() = runTest {
        // Given
        coEvery { questionDao.toggleBookmark(1) } just Runs

        // When
        repository.toggleBookmark(1)

        // Then
        coVerify { questionDao.toggleBookmark(1) }
    }

    @Test
    fun toggleBookmark_throwsBookmarkException_onDaoFailure() = runTest {
        // Given
        coEvery { questionDao.toggleBookmark(1) } throws Exception("DB error")

        // When & Then
        assertFailsWith<BookmarkException> {
            repository.toggleBookmark(1)
        }
    }

    @Test
    fun countBookmarked_returnsFlowOfCount() = runTest {
        // Given
        every { questionDao.countBookmarked() } returns flowOf(5)

        // When
        val count = repository.countBookmarked().first()

        // Then
        assertEquals(5, count)
    }

    @Test
    fun countWrong_returnsFlowOfCount() = runTest {
        // Given
        every { questionDao.countWrong() } returns flowOf(10)

        // When
        val count = repository.countWrong().first()

        // Then
        assertEquals(10, count)
    }
}
