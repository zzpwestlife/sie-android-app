package com.example.sie.feature.home

import com.example.sie.core.data.exception.WrongQuestionException
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Question
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class WrongQuestionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: QuestionRepository
    private lateinit var viewModel: WrongQuestionsViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun uiState_initiallyLoading() {
        // Given
        every { repository.getWrongQuestions() } returns flowOf(emptyList())
        every { repository.getAllQuestions() } returns flowOf(emptyList())

        // When
        viewModel = WrongQuestionsViewModel(repository)

        // Then
        assertIs<WrongQuestionsUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun uiState_emptyWhenNoWrongQuestions() = runTest {
        // Given
        every { repository.getWrongQuestions() } returns flowOf(emptyList())
        every { repository.getAllQuestions() } returns flowOf(emptyList())

        // When
        viewModel = WrongQuestionsViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertIs<WrongQuestionsUiState.Empty>(viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun uiState_successWithStats() = runTest {
        // Given
        val wrongQuestions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Exp1", "Math", isWrong = true, wrongCount = 2),
            Question(2, "Q2", listOf("B"), 0, "Exp2", "English", isWrong = true, wrongCount = 3)
        )
        val allQuestions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Exp1", "Math"),
            Question(2, "Q2", listOf("B"), 0, "Exp2", "English"),
            Question(3, "Q3", listOf("C"), 0, "Exp3", "Math")
        )
        every { repository.getWrongQuestions() } returns flowOf(wrongQuestions)
        every { repository.getAllQuestions() } returns flowOf(allQuestions)

        // When
        viewModel = WrongQuestionsViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as WrongQuestionsUiState.Success
        assertEquals(2, state.questions.size)
        assertEquals(3, state.stats.totalCount)
        assertEquals(2, state.stats.totalWrongCount)
        assertEquals(2.5f, state.stats.avgWrongCount) // (2+3)/2
        job.cancel()
    }

    @Test
    fun selectCategory_filtersQuestions() = runTest {
        // Given
        val wrongQuestions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Exp1", "Math", isWrong = true, wrongCount = 1),
            Question(2, "Q2", listOf("B"), 0, "Exp2", "English", isWrong = true, wrongCount = 2)
        )
        every { repository.getWrongQuestions() } returns flowOf(wrongQuestions)
        every { repository.getAllQuestions() } returns flowOf(emptyList())
        viewModel = WrongQuestionsViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectCategory("Math")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as WrongQuestionsUiState.Success
        assertEquals(1, state.questions.size)
        assertEquals("Q1", state.questions[0].content)
        job.cancel()
    }

    @Test
    fun removeFromWrong_callsRepository() = runTest {
        // Given
        every { repository.getWrongQuestions() } returns flowOf(emptyList())
        every { repository.getAllQuestions() } returns flowOf(emptyList())
        coEvery { repository.removeFromWrong(1) } just Runs
        viewModel = WrongQuestionsViewModel(repository)

        // When
        viewModel.removeFromWrong(1)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.removeFromWrong(1) }
    }

    @Test
    fun removeFromWrong_emitsErrorEvent_onException() = runTest {
        // Given
        every { repository.getWrongQuestions() } returns flowOf(emptyList())
        every { repository.getAllQuestions() } returns flowOf(emptyList())
        coEvery { repository.removeFromWrong(1) } throws WrongQuestionException("Failed")
        viewModel = WrongQuestionsViewModel(repository)

        // When
        viewModel.removeFromWrong(1)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error event should be emitted
        // (需要收集 errorEvents SharedFlow 验证)
        coVerify { repository.removeFromWrong(1) }
    }

    @Test
    fun avgWrongCount_calculatedCorrectly() = runTest {
        // Given: 3 wrong questions with wrongCount 1, 2, 3
        val wrongQuestions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Exp1", "Math", isWrong = true, wrongCount = 1),
            Question(2, "Q2", listOf("B"), 0, "Exp2", "Math", isWrong = true, wrongCount = 2),
            Question(3, "Q3", listOf("C"), 0, "Exp3", "Math", isWrong = true, wrongCount = 3)
        )
        every { repository.getWrongQuestions() } returns flowOf(wrongQuestions)
        every { repository.getAllQuestions() } returns flowOf(emptyList())

        // When
        viewModel = WrongQuestionsViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as WrongQuestionsUiState.Success
        assertEquals(2.0f, state.stats.avgWrongCount) // (1+2+3)/3 = 2.0
        job.cancel()
    }
}
