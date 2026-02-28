package com.example.sie.feature.chapter

import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.Question
import com.example.sie.core.model.UserData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterSelectionViewModelTest {

    private lateinit var questionRepository: QuestionRepository
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var viewModel: ChapterSelectionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        questionRepository = mockk()
        userDataRepository = mockk()

        // Mock default UserData
        every { userDataRepository.userData } returns flowOf(
            UserData(
                darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                useDynamicColor = false,
                fontSizeScale = 0,
                language = "en"
            )
        )
    }

    @Test
    fun `loadChapters calculates progress correctly`() = runTest {
        // Given
        val questions = listOf(
            Question(
                id = 1,
                content = "Q1",
                options = listOf("A", "B"),
                correctAnswerIndex = 0,
                explanation = "E1",
                category = "1. Macroeconomics",
                lastStudiedAt = System.currentTimeMillis(),
                isWrong = false
            ),
            Question(
                id = 2,
                content = "Q2",
                options = listOf("A", "B"),
                correctAnswerIndex = 0,
                explanation = "E2",
                category = "1. Macroeconomics",
                lastStudiedAt = null,
                isWrong = false
            )
        )

        every { questionRepository.getAllCategories() } returns flowOf(listOf("1. Macroeconomics"))
        every { questionRepository.getAllQuestions() } returns flowOf(questions)

        // When
        viewModel = ChapterSelectionViewModel(questionRepository, userDataRepository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ChapterSelectionUiState.Success
        assertEquals(1, state.chapters.size)

        val chapter = state.chapters[0]
        assertEquals(2, chapter.totalQuestions)
        assertEquals(1, chapter.studiedQuestions)
        assertEquals(100f, chapter.accuracyRate, 0.01f)
    }

    @Test
    fun `toggleChapterSelection updates isSelected state`() = runTest {
        // Given
        every { questionRepository.getAllCategories() } returns flowOf(listOf("Chapter 1"))
        every { questionRepository.getAllQuestions() } returns flowOf(emptyList())

        viewModel = ChapterSelectionViewModel(questionRepository, userDataRepository)
        advanceUntilIdle()

        // When
        viewModel.toggleChapterSelection("Chapter 1")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ChapterSelectionUiState.Success
        assertTrue(state.chapters[0].isSelected)
    }

    @Test
    fun `getSelectedChapters returns only selected chapters`() = runTest {
        // Given
        every { questionRepository.getAllCategories() } returns flowOf(listOf("Chapter 1", "Chapter 2"))
        every { questionRepository.getAllQuestions() } returns flowOf(emptyList())

        viewModel = ChapterSelectionViewModel(questionRepository, userDataRepository)
        advanceUntilIdle()

        // When
        viewModel.toggleChapterSelection("Chapter 1")
        advanceUntilIdle()

        // Then
        val selected = viewModel.getSelectedChapters()
        assertEquals(1, selected.size)
        assertEquals("Chapter 1", selected[0])
    }
}
