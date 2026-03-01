package com.example.sie.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = questionRepository.getAllQuestions()
        .map { allQuestions ->
            // Calculate statistics
            val totalQuestions = allQuestions.size
            val studiedQuestions = allQuestions.count { it.lastStudiedAt != null }
            val studyProgress = if (totalQuestions > 0) {
                studiedQuestions.toFloat() / totalQuestions.toFloat()
            } else {
                0f
            }

            HomeUiState.Success(
                studyProgress = studyProgress
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val studyProgress: Float
    ) : HomeUiState
}
