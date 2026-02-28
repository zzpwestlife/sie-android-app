package com.example.sie.feature.exam

import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.Question
import com.example.sie.core.model.UserData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    class MainDispatcherRule(
        val testDispatcher: TestDispatcher = StandardTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val questionRepository: QuestionRepository = mockk()
    private val examRepository: ExamRepository = mockk(relaxed = true)
    private val userDataRepository: UserDataRepository = mockk(relaxed = true)
    private lateinit var viewModel: ExamViewModel

    @Test
    fun submitExam_calculatesScoreAndSavesResult() = runTest {
        // Arrange
        val questions = listOf(
            Question(
                id = 1,
                content = "Question 1",
                options = listOf("A", "B", "C", "D"),
                correctAnswerIndex = 0, // A
                explanation = "Explanation 1",
                category = "Category 1",
                lastStudiedAt = null
            ),
            Question(
                id = 2,
                content = "Question 2",
                options = listOf("A", "B", "C", "D"),
                correctAnswerIndex = 1, // B
                explanation = "Explanation 2",
                category = "Category 2",
                lastStudiedAt = null
            )
        )

        coEvery { questionRepository.getAllQuestionsList() } returns questions
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Initialize ViewModel
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)

        // Ensure init block runs
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Start the exam
        viewModel.startExam()
        // Use runCurrent to execute only immediate coroutines, not delayed ones (timer)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Verify exam started
        val stateAfterStart = viewModel.uiState.value
        assertTrue("State should be InProgress after startExam, but was $stateAfterStart",
            stateAfterStart is ExamUiState.InProgress)

        // Act
        // Answer Question 1 Correctly (0)
        viewModel.onAnswerSelected(1, 0)

        // Answer Question 2 Incorrectly (0, correct is 1)
        viewModel.onAnswerSelected(2, 0)

        // Submit
        viewModel.submitExam()

        // Ensure saveResult coroutine runs
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val uiState = viewModel.uiState.value
        assertTrue("State should be Finished", uiState is ExamUiState.Finished)

        val finishedState = uiState as ExamUiState.Finished
        assertEquals("User answers count should be 2", 2, finishedState.userAnswers.size)
        assertEquals(50, finishedState.score) // 1/2 correct = 50%
        assertEquals(2, finishedState.totalQuestions)

        // Verify Repository Interaction
        coVerify { examRepository.saveExamResult(any()) }
    }

    @Test
    fun startExam_loads32QuestionsSuccessfully() = runTest {
        // Arrange
        val questions = (1..100).map { id ->
            Question(
                id = id,
                content = "Question $id",
                options = listOf("A", "B", "C", "D"),
                correctAnswerIndex = 0,
                explanation = "Explanation $id",
                category = "1. Macroeconomics / 宏观经济学",
                lastStudiedAt = null
            )
        }
        coEvery { questionRepository.getAllQuestionsList() } returns questions
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Act
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        viewModel.startExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("State should be InProgress", state is ExamUiState.InProgress)
        assertEquals(32, (state as ExamUiState.InProgress).questions.size)
        assertEquals(0, state.currentQuestionIndex)
        assertEquals(30 * 60 * 1000L, state.timeLeftMillis) // 30 minutes
    }

    @Test
    fun onAnswerSelected_updatesUserAnswersMap() = runTest {
        // Arrange
        val questions = listOf(
            Question(1, "Q1", listOf("A", "B"), 0, "Exp1", "Cat1"),
            Question(2, "Q2", listOf("C", "D"), 1, "Exp2", "Cat2")
        )
        coEvery { questionRepository.getAllQuestionsList() } returns questions
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Act
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        viewModel.startExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        viewModel.onAnswerSelected(1, 0) // Answer question 1 with option 0
        viewModel.onAnswerSelected(2, 1) // Answer question 2 with option 1
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val state = viewModel.uiState.value as ExamUiState.InProgress
        assertEquals(0, state.userAnswers[1])
        assertEquals(1, state.userAnswers[2])
    }

    @Test
    fun submitExam_withTimeout_autoSubmitsExam() = runTest {
        // Arrange
        val questions = listOf(
            Question(1, "Q1", listOf("A", "B"), 0, "Exp1", "Cat1"),
            Question(2, "Q2", listOf("C", "D"), 1, "Exp2", "Cat2")
        )
        coEvery { questionRepository.getAllQuestionsList() } returns questions
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Act
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        viewModel.startExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Verify exam started
        val stateAfterStart = viewModel.uiState.value
        assertTrue("State should be InProgress", stateAfterStart is ExamUiState.InProgress)

        // Advance time by 30 minutes to trigger timeout
        mainDispatcherRule.testDispatcher.scheduler.advanceTimeBy(30 * 60 * 1000L + 100)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val state = viewModel.uiState.value
        assertTrue("State should be Finished after timeout", state is ExamUiState.Finished)
        coVerify { examRepository.saveExamResult(any()) }
    }

    @Test
    fun submitExam_calculatesZeroPercentForAllWrongAnswers() = runTest {
        // Arrange
        val questions = listOf(
            Question(1, "Q1", listOf("A", "B"), 0, "Exp1", "Cat1"),
            Question(2, "Q2", listOf("C", "D"), 1, "Exp2", "Cat2"),
            Question(3, "Q3", listOf("E", "F"), 0, "Exp3", "Cat3")
        )
        coEvery { questionRepository.getAllQuestionsList() } returns questions
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Act
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        viewModel.startExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Answer all questions incorrectly
        viewModel.onAnswerSelected(1, 1) // Correct is 0
        viewModel.onAnswerSelected(2, 0) // Correct is 1
        viewModel.onAnswerSelected(3, 1) // Correct is 0

        viewModel.submitExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert
        val state = viewModel.uiState.value as ExamUiState.Finished
        assertEquals(0, state.score) // 0/3 = 0%
        assertEquals(false, state.passed) // < 70%
        assertEquals(3, state.totalQuestions)
        coVerify { examRepository.saveExamResult(any()) }
    }

    @Test
    fun handleError_whenRepositoryFails() = runTest {
        // Arrange
        coEvery { questionRepository.getAllQuestionsList() } throws RuntimeException("Database error")
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Act
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        viewModel.startExam()
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        // Assert - should remain in Loading or handle gracefully
        val state = viewModel.uiState.value
        // The implementation catches exception and prints stack trace,
        // but doesn't update state, so it remains Loading
        assertTrue("State should remain Loading after error", state is ExamUiState.Loading)
    }
}
