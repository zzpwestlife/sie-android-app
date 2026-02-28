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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardLearningViewModel @Inject constructor(
    private val cardRepository: CardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardLearningUiState())
    val uiState: StateFlow<CardLearningUiState> = _uiState.asStateFlow()

    private val learningQueue = ArrayDeque<Card>()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Prioritize review cards, then new cards
            val reviewCards = cardRepository.getReviewCards(20).first()
            val newCards = cardRepository.getNewCards(10).first()
            
            learningQueue.clear()
            learningQueue.addAll(reviewCards)
            learningQueue.addAll(newCards)
            
            loadNextCard()
            _uiState.update { it.copy(isLoading = false, totalCards = learningQueue.size + (if (it.currentCard != null) 1 else 0)) }
        }
    }

    private fun loadNextCard() {
        val nextCard = learningQueue.removeFirstOrNull()
        _uiState.update { 
            it.copy(
                currentCard = nextCard,
                isFlipped = false,
                isFinished = nextCard == null
            )
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun markResult(known: Boolean) {
        val currentCard = _uiState.value.currentCard ?: return
        
        viewModelScope.launch {
            val newStatus = if (known) {
                if (currentCard.status == CardStatus.NEW) CardStatus.LEARNING else CardStatus.REVIEWING
            } else {
                CardStatus.LEARNING
            }
            
            // Simple spaced repetition logic
            // If known: review in 1 day (86400000ms) * proficiency factor
            // If unknown: review in 10 minutes
            val nextReviewTime = System.currentTimeMillis() + if (known) {
                86400000L * (currentCard.proficiency + 1)
            } else {
                600000L
            }

            cardRepository.updateCardReview(
                id = currentCard.id,
                nextReviewTime = nextReviewTime,
                status = newStatus
            )

            loadNextCard()
        }
    }
}

data class CardLearningUiState(
    val isLoading: Boolean = false,
    val currentCard: Card? = null,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val totalCards: Int = 0
)
