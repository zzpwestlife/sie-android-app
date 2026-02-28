package com.example.sie.feature.stats

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
class StatsViewModel @Inject constructor(
    examRepository: ExamRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = examRepository.getExamResults()
        .map { results ->
            if (results.isEmpty()) {
                StatsUiState.Empty
            } else {
                val averageScore = results.map { it.score }.average().toFloat()
                StatsUiState.Success(
                    recentResults = results.take(10), // Show last 10
                    averageScore = averageScore
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUiState.Loading
        )
}

sealed interface StatsUiState {
    data object Loading : StatsUiState
    data object Empty : StatsUiState
    data class Success(
        val recentResults: List<ExamResult>,
        val averageScore: Float
    ) : StatsUiState
}
