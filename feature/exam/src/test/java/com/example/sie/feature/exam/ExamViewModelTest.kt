package com.example.sie.feature.exam

import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.Question
import com.example.sie.core.model.UserData
import io.mockk.coEvery
import io.mockk.coVerify
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
                category = "Category 1"
            ),
            Question(
                id = 2,
                content = "Question 2",
                options = listOf("A", "B", "C", "D"),
                correctAnswerIndex = 1, // B
                explanation = "Explanation 2",
                category = "Category 2"
            )
        )

        coEvery { questionRepository.getRandomQuestions(75) } returns flowOf(questions)
        coEvery { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "zh"
            )
        )

        // Initialize ViewModel
        viewModel = ExamViewModel(questionRepository, examRepository, userDataRepository)
        
        // Ensure init block and startExam coroutine runs
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()
        
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
        println("DEBUG: uiState = $uiState")
        assertTrue("State should be Finished but was $uiState", uiState is ExamUiState.Finished)

        val finishedState = uiState as ExamUiState.Finished
        assertEquals("User answers count should be 2", 2, finishedState.userAnswers.size)
        assertEquals(50, finishedState.score) // 1/2 correct = 50%
        assertEquals(2, finishedState.totalQuestions)

        // Verify Repository Interaction
        coVerify { examRepository.saveExamResult(any()) }
    }
}
