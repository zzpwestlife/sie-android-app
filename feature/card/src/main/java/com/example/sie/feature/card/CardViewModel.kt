package com.example.sie.feature.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    val uiState: StateFlow<CardUiState> = combine(
        cardRepository.getCount(),
        cardRepository.getLearnedCount(),
        cardRepository.getMasteredCount(),
        cardRepository.getReviewPendingCount()
    ) { total, learned, mastered, review ->
        CardUiState.Success(
            totalCount = total,
            learnedCount = learned,
            masteredCount = mastered,
            reviewPendingCount = review
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CardUiState.Loading
    )
}

sealed interface CardUiState {
    data object Loading : CardUiState
    data class Success(
        val totalCount: Int,
        val learnedCount: Int,
        val masteredCount: Int,
        val reviewPendingCount: Int
    ) : CardUiState
}
