package com.example.sie.feature.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import com.example.sie.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val examResultId: Int = checkNotNull(savedStateHandle["examResultId"])

    private val _uiState = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Loading)
    val uiState: StateFlow<ExamDetailUiState> = _uiState.asStateFlow()

    init {
        loadExamDetail()
    }

    private fun loadExamDetail() {
        viewModelScope.launch {
            try {
                val result = examRepository.getExamResultById(examResultId).first()
                val answers = examRepository.getExamAnswers(examResultId).first()

                if (result == null || answers.isEmpty()) {
                    _uiState.value = ExamDetailUiState.Error
                    return@launch
                }

                val questionIds = answers.map { it.questionId }
                val allQuestions = questionRepository.getAllQuestionsList()
                val questionsMap = allQuestions.associateBy { it.id }
                val questions = questionIds.mapNotNull { questionsMap[it] }

                _uiState.value = ExamDetailUiState.Success(
                    examResult = result,
                    answers = answers,
                    questions = questions
                )
            } catch (e: Exception) {
                _uiState.value = ExamDetailUiState.Error
            }
        }
    }

    fun toggleFilter() {
        _uiState.value.let { state ->
            if (state is ExamDetailUiState.Success) {
                _uiState.value = state.copy(filterWrongOnly = !state.filterWrongOnly)
            }
        }
    }
}

sealed interface ExamDetailUiState {
    data object Loading : ExamDetailUiState
    data object Error : ExamDetailUiState
    data class Success(
        val examResult: ExamResult,
        val answers: List<ExamAnswer>,
        val questions: List<Question>,
        val filterWrongOnly: Boolean = false
    ) : ExamDetailUiState {
        val filteredAnswers: List<ExamAnswer>
            get() = if (filterWrongOnly) answers.filter { !it.isCorrect } else answers

        val filteredQuestions: List<Question>
            get() {
                val filteredIds = filteredAnswers.map { it.questionId }.toSet()
                return questions.filter { it.id in filteredIds }
            }
    }
}
