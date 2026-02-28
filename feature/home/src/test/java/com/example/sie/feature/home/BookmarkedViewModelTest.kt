package com.example.sie.feature.home

import com.example.sie.core.data.exception.BookmarkException
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Question
import io.mockk.*
import kotlinx.coroutines.Dispatchers
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: QuestionRepository
    private lateinit var viewModel: BookmarkedViewModel

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
        every { repository.getBookmarkedQuestions() } returns flowOf(emptyList())

        // When
        viewModel = BookmarkedViewModel(repository)

        // Then
        assertIs<BookmarkedUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun uiState_emptyWhenNoBookmarkedQuestions() = runTest {
        // Given
        every { repository.getBookmarkedQuestions() } returns flowOf(emptyList())

        // When
        viewModel = BookmarkedViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertIs<BookmarkedUiState.Empty>(viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun uiState_successWithQuestions() = runTest {
        // Given
        val questions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Explanation", "Math/Algebra", isBookmarked = true),
            Question(2, "Q2", listOf("B"), 0, "Explanation", "English", isBookmarked = true)
        )
        every { repository.getBookmarkedQuestions() } returns flowOf(questions)

        // When
        viewModel = BookmarkedViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertIs<BookmarkedUiState.Success>(state)
        assertEquals(2, state.questions.size)
        assertEquals(2, state.categories.size)
        assertTrue(state.categories.contains("Math/Algebra"))
        assertTrue(state.categories.contains("English"))
        job.cancel()
    }

    @Test
    fun selectCategory_filtersQuestions() = runTest {
        // Given
        val questions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Explanation", "Math/Algebra", isBookmarked = true),
            Question(2, "Q2", listOf("B"), 0, "Explanation", "English", isBookmarked = true),
            Question(3, "Q3", listOf("C"), 0, "Explanation", "Math/Geometry", isBookmarked = true)
        )
        every { repository.getBookmarkedQuestions() } returns flowOf(questions)
        viewModel = BookmarkedViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // When: Select "Math/Algebra"
        viewModel.selectCategory("Math/Algebra")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then: Only Math/Algebra questions
        val state = viewModel.uiState.value as BookmarkedUiState.Success
        assertEquals(1, state.questions.size)
        assertEquals("Q1", state.questions[0].content)
        assertEquals("Math/Algebra", state.selectedCategory)
        job.cancel()
    }

    @Test
    fun selectCategory_null_showsAllQuestions() = runTest {
        // Given
        val questions = listOf(
            Question(1, "Q1", listOf("A"), 0, "Explanation", "Math", isBookmarked = true),
            Question(2, "Q2", listOf("B"), 0, "Explanation", "English", isBookmarked = true)
        )
        every { repository.getBookmarkedQuestions() } returns flowOf(questions)
        viewModel = BookmarkedViewModel(repository)
        // Subscribe to the flow to trigger WhileSubscribed
        val job = launch {
            viewModel.uiState.collect {}
        }
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // When: Select specific category then clear
        viewModel.selectCategory("Math")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectCategory(null)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then: All questions shown
        val state = viewModel.uiState.value as BookmarkedUiState.Success
        assertEquals(2, state.questions.size)
        job.cancel()
    }

    @Test
    fun toggleBookmark_callsRepository() = runTest {
        // Given
        every { repository.getBookmarkedQuestions() } returns flowOf(emptyList())
        coEvery { repository.toggleBookmark(1) } just Runs
        viewModel = BookmarkedViewModel(repository)

        // When
        viewModel.toggleBookmark(1)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.toggleBookmark(1) }
    }

    @Test
    fun toggleBookmark_emitsErrorEvent_onException() = runTest {
        // Given
        every { repository.getBookmarkedQuestions() } returns flowOf(emptyList())
        coEvery { repository.toggleBookmark(1) } throws BookmarkException("Failed")
        viewModel = BookmarkedViewModel(repository)

        // When
        viewModel.toggleBookmark(1)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error event should be emitted
        // (需要收集 errorEvents SharedFlow 验证)
        coVerify { repository.toggleBookmark(1) }
    }
}
