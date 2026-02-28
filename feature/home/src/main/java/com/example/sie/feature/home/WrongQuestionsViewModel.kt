package com.example.sie.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WrongQuestionsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    val uiState: StateFlow<WrongQuestionsUiState> = combine(
        questionRepository.getWrongQuestions(),
        questionRepository.getAllQuestions(),
        _selectedCategory
    ) { wrongQuestions, allQuestions, selectedCategory ->
        if (wrongQuestions.isEmpty()) {
            WrongQuestionsUiState.Empty
        } else {
            val filtered = if (selectedCategory != null) {
                wrongQuestions.filter { it.category == selectedCategory }
            } else {
                wrongQuestions
            }

            val categories = wrongQuestions.map { it.category }.distinct().sorted()

            val stats = WrongQuestionsStats(
                totalCount = allQuestions.size,
                totalWrongCount = wrongQuestions.size,
                avgWrongCount = if (wrongQuestions.isNotEmpty()) {
                    wrongQuestions.map { it.wrongCount }.average().toFloat()
                } else {
                    0f
                }
            )

            WrongQuestionsUiState.Success(
                questions = filtered,
                categories = categories,
                selectedCategory = selectedCategory,
                stats = stats
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WrongQuestionsUiState.Loading
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun removeFromWrong(questionId: Int) {
        viewModelScope.launch {
            try {
                questionRepository.removeFromWrong(questionId)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to remove from wrong list: ${e.message}")
            }
        }
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            try {
                questionRepository.toggleBookmark(questionId)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to toggle bookmark: ${e.message}")
            }
        }
    }
}
