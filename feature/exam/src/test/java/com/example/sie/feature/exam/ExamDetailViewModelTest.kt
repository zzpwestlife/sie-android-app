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
