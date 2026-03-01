package com.example.sie.feature.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
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
    private val questionRepository: QuestionRepository,
    private val userDataRepository: com.example.sie.core.data.repository.UserDataRepository
) : ViewModel() {

    private val examResultId: Int = checkNotNull(savedStateHandle["examResultId"])

    companion object {
        private const val TAG = "ExamDetailViewModel"
    }

    private val _uiState = MutableStateFlow<ExamDetailUiState>(ExamDetailUiState.Loading)
    val uiState: StateFlow<ExamDetailUiState> = _uiState.asStateFlow()

    private val _language = MutableStateFlow("zh")
    val language: StateFlow<String> = _language.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.userData.collect { userData ->
                _language.value = userData.language
            }
        }
        loadExamDetail()
    }

    private fun loadExamDetail() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading exam detail for examResultId: $examResultId")
                val result = examRepository.getExamResultById(examResultId).first()
                Log.d(TAG, "Exam result: $result")
                val answers = examRepository.getExamAnswers(examResultId).first()
                Log.d(TAG, "Exam answers count: ${answers.size}")

                if (result == null) {
                    Log.e(TAG, "Exam result is null for id: $examResultId")
                    _uiState.value = ExamDetailUiState.Error
                    return@launch
                }

                if (answers.isEmpty()) {
                    Log.w(TAG, "No answers found for exam result id: $examResultId (legacy exam without detailed answers)")
                    // Show a message that this is a legacy exam without detailed answers
                    _uiState.value = ExamDetailUiState.LegacyExam(examResult = result)
                    return@launch
                }

                val allQuestions = questionRepository.getAllQuestionsList()
                val questionsMap = allQuestions.associateBy { it.id }

                // Filter out answers whose questions no longer exist in the repository
                val validAnswers = mutableListOf<ExamAnswer>()
                val validQuestions = mutableListOf<Question>()
                var missingCount = 0

                for (answer in answers) {
                    val question = questionsMap[answer.questionId]
                    if (question != null) {
                        validAnswers.add(answer)
                        validQuestions.add(question)
                    } else {
                        missingCount++
                        Log.w(TAG, "Question ${answer.questionId} not found in repository")
                    }
                }

                if (missingCount > 0) {
                    Log.w(TAG, "Missing $missingCount questions from exam result $examResultId")
                }

                if (validAnswers.isEmpty() || validQuestions.isEmpty()) {
                    Log.e(TAG, "No valid questions/answers after filtering for exam result id: $examResultId")
                    _uiState.value = ExamDetailUiState.Error
                    return@launch
                }

                Log.d(TAG, "Successfully loaded exam detail: ${validQuestions.size} questions, ${validAnswers.size} answers")
                _uiState.value = ExamDetailUiState.Success(
                    examResult = result,
                    answers = validAnswers,
                    questions = validQuestions
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
    data class LegacyExam(val examResult: ExamResult) : ExamDetailUiState
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
