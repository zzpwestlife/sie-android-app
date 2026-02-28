package com.example.sie.feature.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.CardRepository
import com.example.sie.core.model.Card
import com.example.sie.core.model.CardStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardCreateViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardCreateUiState())
    val uiState: StateFlow<CardCreateUiState> = _uiState.asStateFlow()

    fun updateFront(front: String) {
        _uiState.update { it.copy(front = front) }
    }

    fun updateBack(back: String) {
        _uiState.update { it.copy(back = back) }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun saveCard() {
        val currentState = _uiState.value
        if (currentState.front.isBlank() || currentState.back.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val newId = cardRepository.generateNewId()
                val newCard = Card(
                    id = newId,
                    front = currentState.front,
                    back = currentState.back,
                    category = currentState.category.ifBlank { "" },
                    status = CardStatus.NEW,
                    nextReviewTime = 0,
                    reviewCount = 0,
                    proficiency = 0
                )
                
                cardRepository.insertCard(newCard)
                
                _uiState.update {
                    CardCreateUiState(
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}

data class CardCreateUiState(
    val front: String = "",
    val back: String = "",
    val category: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)
