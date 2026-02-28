package com.example.sie.core.data.repository

import com.example.sie.core.database.dao.CardDao
import com.example.sie.core.database.model.asEntity
import com.example.sie.core.database.model.asExternalModel
import com.example.sie.core.model.Card
import com.example.sie.core.model.CardStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineCardRepository @Inject constructor(
    private val cardDao: CardDao
) : CardRepository {

    override fun getAllCards(): Flow<List<Card>> =
        cardDao.getAllCards().map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun getCard(id: Int): Card? =
        cardDao.getCard(id)?.asExternalModel()

    override suspend fun updateCardStatus(id: Int, status: CardStatus) {
        val entity = cardDao.getCard(id)
        if (entity != null) {
            cardDao.updateCard(entity.copy(status = status))
        }
    }

    override suspend fun updateCardReview(id: Int, nextReviewTime: Long, status: CardStatus) {
        val entity = cardDao.getCard(id)
        if (entity != null) {
            cardDao.updateCard(
                entity.copy(
                    nextReviewTime = nextReviewTime,
                    status = status,
                    reviewCount = entity.reviewCount + 1
                )
            )
        }
    }

    override fun getCount(): Flow<Int> = cardDao.getCount()

    override fun getLearnedCount(): Flow<Int> = cardDao.getLearnedCount()

    override fun getMasteredCount(): Flow<Int> = cardDao.getMasteredCount()

    override fun getReviewPendingCount(): Flow<Int> = 
        cardDao.getReviewPendingCount(System.currentTimeMillis())

    override fun getReviewCards(limit: Int): Flow<List<Card>> =
        cardDao.getReviewCards(System.currentTimeMillis(), limit)
            .map { entities -> entities.map { it.asExternalModel() } }

    override fun getNewCards(limit: Int): Flow<List<Card>> =
        cardDao.getNewCards(limit).map { entities -> entities.map { it.asExternalModel() } }

    override suspend fun insertCard(card: Card) {
        cardDao.insertCards(listOf(card.asEntity()))
    }

    override suspend fun generateNewId(): Int {
        return (cardDao.getMaxId() ?: 0) + 1
    }
}
