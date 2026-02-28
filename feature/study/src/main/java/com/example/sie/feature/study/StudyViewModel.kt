package com.example.sie.feature.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StudyUiState {
    data object Loading : StudyUiState
    data class Success(
        val currentQuestion: Question,
        val selectedOptionIndex: Int? = null,
        val isAnswerRevealed: Boolean = false,
        val isCorrect: Boolean = false
    ) : StudyUiState
    data class Error(val message: String) : StudyUiState
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    init {
        loadNewQuestion()
    }

    fun loadNewQuestion() {
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            try {
                // Fetch 1 random question
                val questions = questionRepository.getRandomQuestions(1).first()
                if (questions.isNotEmpty()) {
                    _uiState.value = StudyUiState.Success(currentQuestion = questions.first())
                } else {
                    _uiState.value = StudyUiState.Error("No questions available")
                }
            } catch (e: Exception) {
                _uiState.value = StudyUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectOption(index: Int) {
        _uiState.update { currentState ->
            if (currentState is StudyUiState.Success && !currentState.isAnswerRevealed) {
                val isCorrect = index == currentState.currentQuestion.correctAnswerIndex
                currentState.copy(
                    selectedOptionIndex = index,
                    isAnswerRevealed = true,
                    isCorrect = isCorrect
                )
            } else {
                currentState
            }
        }
    }
}
