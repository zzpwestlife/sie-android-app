package com.example.sie.feature.exam

import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.model.ExamResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExamHistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var examRepository: ExamRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        examRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        every { examRepository.getExamResults() } returns flowOf(emptyList())
        val viewModel = ExamHistoryViewModel(examRepository)
        // Before collection starts, initial value is Loading
        assertEquals(ExamHistoryUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty results produces Empty state`() = runTest {
        every { examRepository.getExamResults() } returns flowOf(emptyList())
        val viewModel = ExamHistoryViewModel(examRepository)
        val job = launch(testDispatcher) { viewModel.uiState.collect() }
        assertEquals(ExamHistoryUiState.Empty, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun `non-empty results produces Success state`() = runTest {
        val results = listOf(
            ExamResult(id = 1, date = 1000L, score = 80, totalQuestions = 32, correctCount = 26)
        )
        every { examRepository.getExamResults() } returns flowOf(results)
        val viewModel = ExamHistoryViewModel(examRepository)
        val job = launch(testDispatcher) { viewModel.uiState.collect() }
        val state = viewModel.uiState.value
        assertTrue(state is ExamHistoryUiState.Success)
        assertEquals(1, (state as ExamHistoryUiState.Success).results.size)
        job.cancel()
    }
}
