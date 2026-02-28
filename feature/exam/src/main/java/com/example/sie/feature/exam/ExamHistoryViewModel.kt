package com.example.sie.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.model.ExamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExamHistoryViewModel @Inject constructor(
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<ExamHistoryUiState> = examRepository.getExamResults()
        .map { results ->
            if (results.isEmpty()) {
                ExamHistoryUiState.Empty
            } else {
                ExamHistoryUiState.Success(results = results)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExamHistoryUiState.Loading
        )
}

sealed interface ExamHistoryUiState {
    data object Loading : ExamHistoryUiState
    data object Empty : ExamHistoryUiState
    data class Success(val results: List<ExamResult>) : ExamHistoryUiState
}
