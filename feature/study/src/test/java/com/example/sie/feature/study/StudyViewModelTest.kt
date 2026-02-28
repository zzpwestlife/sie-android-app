package com.example.sie.feature.study

import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.Question
import com.example.sie.core.model.UserData
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: StudyViewModel
    private val questionRepository: QuestionRepository = mockk()
    private val userDataRepository: UserDataRepository = mockk()

    @Before
    fun setup() {
        clearAllMocks()
        // Mock UserDataRepository to return default UserData
        coEvery { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "en"
            )
        )
    }

    @Test
    fun `loadNewQuestion emits Loading then Success with question`() = runTest {
        // Arrange
        val mockQuestion = Question(
            id = 1,
            content = "What is the capital of France?",
            options = listOf("London", "Berlin", "Paris", "Madrid"),
            correctAnswerIndex = 2,
            explanation = "Paris is the capital.",
            category = "Geography"
        )
        coEvery { questionRepository.getAllQuestions() } returns flowOf(listOf(mockQuestion))

        // Act
        viewModel = StudyViewModel(questionRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("State should be Success, but was $state", state is StudyUiState.Success)
        assertEquals(mockQuestion, (state as StudyUiState.Success).currentQuestion)
        assertEquals(null, state.selectedOptionIndex)
        assertEquals(false, state.isAnswerRevealed)
    }

    @Test
    fun `selectOption with correct answer updates state and reveals answer`() = runTest {
        // Arrange
        val mockQuestion = Question(
            id = 2,
            content = "What is 2 + 2?",
            options = listOf("3", "4", "5", "6"),
            correctAnswerIndex = 1, // "4"
            explanation = "Basic math",
            category = "Math"
        )
        coEvery { questionRepository.getAllQuestions() } returns flowOf(listOf(mockQuestion))

        // Act
        viewModel = StudyViewModel(questionRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectOption(1) // Select correct answer
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value as StudyUiState.Success
        assertEquals(1, state.selectedOptionIndex)
        assertTrue(state.isAnswerRevealed)
        assertTrue(state.isCorrect)
    }

    @Test
    fun `selectOption with wrong answer shows explanation and marks incorrect`() = runTest {
        // Arrange
        val mockQuestion = Question(
            id = 3,
            content = "What is the capital of Japan?",
            options = listOf("Seoul", "Beijing", "Tokyo", "Bangkok"),
            correctAnswerIndex = 2, // "Tokyo"
            explanation = "Tokyo is the capital of Japan.",
            category = "Geography"
        )
        coEvery { questionRepository.getAllQuestions() } returns flowOf(listOf(mockQuestion))
        coEvery { questionRepository.markAsWrong(any()) } just Runs
        coEvery { questionRepository.markQuestionAsStudied(any()) } just Runs

        // Act
        viewModel = StudyViewModel(questionRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.selectOption(0) // Select wrong answer "Seoul"
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value as StudyUiState.Success
        assertEquals(0, state.selectedOptionIndex)
        assertTrue(state.isAnswerRevealed)
        assertEquals(false, state.isCorrect)
    }

    @Test
    fun `loadNewQuestion with empty list stays in Loading state`() = runTest {
        // Arrange - When getAllQuestions returns empty list, ViewModel stays in Loading
        // waiting for database population (design change from original getRandomQuestions)
        coEvery { questionRepository.getAllQuestions() } returns flowOf(emptyList())

        // Act
        viewModel = StudyViewModel(questionRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Empty list means data not yet populated, so stays Loading
        val state = viewModel.uiState.value
        assertTrue("State should be Loading when no questions available, but was $state", state is StudyUiState.Loading)
    }

    @Test
    fun `loadNewQuestion handles repository exception`() = runTest {
        // Arrange
        coEvery { questionRepository.getAllQuestions() } throws RuntimeException("Database error")

        // Act
        viewModel = StudyViewModel(questionRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("State should be Error, but was $state", state is StudyUiState.Error)
        assertTrue((state as StudyUiState.Error).message.contains("Database error"))
    }
}
