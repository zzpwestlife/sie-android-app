package com.example.sie.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkedViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents: SharedFlow<String> = _errorEvents.asSharedFlow()

    val uiState: StateFlow<BookmarkedUiState> = combine(
        questionRepository.getBookmarkedQuestions(),
        _selectedCategory
    ) { allBookmarked, selectedCategory ->
        if (allBookmarked.isEmpty()) {
            BookmarkedUiState.Empty
        } else {
            val filtered = if (selectedCategory != null) {
                allBookmarked.filter { it.category == selectedCategory }
            } else {
                allBookmarked
            }

            val categories = allBookmarked.map { it.category }.distinct().sorted()

            BookmarkedUiState.Success(
                questions = filtered,
                categories = categories,
                selectedCategory = selectedCategory
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookmarkedUiState.Loading
    )

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
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
