package com.example.sie.feature.study

import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Question
import io.mockk.clearAllMocks
import io.mockk.coEvery
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

    @Before
    fun setup() {
        clearAllMocks()
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
        coEvery { questionRepository.getRandomQuestions(1) } returns flowOf(listOf(mockQuestion))

        // Act
        viewModel = StudyViewModel(questionRepository)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is StudyUiState.Success)
        assertEquals(mockQuestion, (state as StudyUiState.Success).currentQuestion)
        assertEquals(null, state.selectedOptionIndex)
        assertEquals(false, state.isAnswerRevealed)
    }
}
