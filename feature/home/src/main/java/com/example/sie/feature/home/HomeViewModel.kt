package com.example.sie.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        questionRepository.getAllQuestions(),
        examRepository.getExamResults()
    ) { allQuestions, examResults ->
        // Calculate statistics
        val totalQuestions = allQuestions.size
        val studiedQuestions = allQuestions.count { it.lastStudiedAt != null }
        val studyProgress = if (totalQuestions > 0) {
            studiedQuestions.toFloat() / totalQuestions.toFloat()
        } else {
            0f
        }

        // Calculate correct rate from exam history
        val correctRate = if (examResults.isNotEmpty()) {
            val totalExamQuestions = examResults.sumOf { it.totalQuestions }
            val totalCorrect = examResults.sumOf { it.correctCount }
            if (totalExamQuestions > 0) {
                (totalCorrect * 100) / totalExamQuestions
            } else {
                0
            }
        } else {
            0
        }

        HomeUiState.Success(
            studyProgress = studyProgress,
            questionsStudied = studiedQuestions,
            correctRate = correctRate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val studyProgress: Float,
        val questionsStudied: Int,
        val correctRate: Int
    ) : HomeUiState
}
