package com.example.sie.feature.chapter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.Chapter
import com.example.sie.core.model.extractLocalizedName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChapterSelectionUiState {
    data object Loading : ChapterSelectionUiState
    data class Success(val chapters: List<Chapter>) : ChapterSelectionUiState
    data class Error(val message: String) : ChapterSelectionUiState
}

@HiltViewModel
class ChapterSelectionViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChapterSelectionUiState>(ChapterSelectionUiState.Loading)
    val uiState: StateFlow<ChapterSelectionUiState> = _uiState.asStateFlow()

    val language: StateFlow<String> = userDataRepository.userData
        .map { it.language }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    init {
        loadChapters()
    }

    fun setLanguage(lang: String) {
        // No-op: language is managed by userDataRepository
        loadChapters()
    }

    private fun loadChapters() {
        viewModelScope.launch {
            try {
                questionRepository.getAllCategories()
                    .combine(questionRepository.getAllQuestions()) { categories, questions ->
                        categories.map { category ->
                            val chapterQuestions = questions.filter { it.category == category }
                            val studiedQuestions = chapterQuestions.filter { it.lastStudiedAt != null }
                            val correctCount = studiedQuestions.count { !it.isWrong }

                            Chapter(
                                name = category,
                                displayName = category.extractLocalizedName(language.value),
                                totalQuestions = chapterQuestions.size,
                                studiedQuestions = studiedQuestions.size,
                                correctCount = correctCount,
                                wrongCount = chapterQuestions.sumOf { it.wrongCount },
                                accuracyRate = if (studiedQuestions.isEmpty()) 0f
                                    else (correctCount.toFloat() / studiedQuestions.size) * 100f,
                                lastStudiedAt = chapterQuestions.mapNotNull { it.lastStudiedAt }.maxOrNull()
                            )
                        }
                    }
                    .collect { chapters ->
                        _uiState.value = ChapterSelectionUiState.Success(chapters)
                    }
            } catch (e: Exception) {
                _uiState.value = ChapterSelectionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleChapterSelection(chapterName: String) {
        val currentState = _uiState.value
        if (currentState is ChapterSelectionUiState.Success) {
            val updatedChapters = currentState.chapters.map { chapter ->
                if (chapter.name == chapterName) {
                    chapter.copy(isSelected = !chapter.isSelected)
                } else {
                    chapter
                }
            }
            _uiState.value = currentState.copy(chapters = updatedChapters)
            android.util.Log.d("ChapterViewModel", "Toggled $chapterName. Selected chapters: ${getSelectedChapters()}")
        }
    }

    fun getSelectedChapters(): List<String> {
        val currentState = _uiState.value
        val selected = if (currentState is ChapterSelectionUiState.Success) {
            currentState.chapters.filter { it.isSelected }.map { it.name }
        } else {
            emptyList()
        }
        android.util.Log.d("ChapterViewModel", "getSelectedChapters() returning: $selected")
        return selected
    }

    fun hasSelectedChapters(): Boolean {
        val has = getSelectedChapters().isNotEmpty()
        android.util.Log.d("ChapterViewModel", "hasSelectedChapters() = $has")
        return has
    }
}
