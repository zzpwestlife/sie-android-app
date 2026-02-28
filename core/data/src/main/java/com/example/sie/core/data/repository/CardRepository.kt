package com.example.sie.core.data.repository

import com.example.sie.core.model.Card
import com.example.sie.core.model.CardStatus
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getAllCards(): Flow<List<Card>>
    suspend fun getCard(id: Int): Card?
    suspend fun updateCardStatus(id: Int, status: CardStatus)
    suspend fun updateCardReview(id: Int, nextReviewTime: Long, status: CardStatus)
    
    fun getCount(): Flow<Int>
    fun getLearnedCount(): Flow<Int>
    fun getMasteredCount(): Flow<Int>
    fun getReviewPendingCount(): Flow<Int>
    
    fun getReviewCards(limit: Int): Flow<List<Card>>
    fun getNewCards(limit: Int): Flow<List<Card>>
    
    suspend fun insertCard(card: Card)
    suspend fun generateNewId(): Int
}
