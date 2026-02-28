package com.example.sie.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.sie.core.database.model.CardEntity
import com.example.sie.core.model.CardStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCard(id: Int): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("SELECT COUNT(*) FROM cards")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards")
    suspend fun getCardCount(): Int

    @Query("SELECT COUNT(*) FROM cards WHERE status != 'NEW'")
    fun getLearnedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE status = 'MASTERED'")
    fun getMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE status != 'NEW' AND nextReviewTime <= :currentTime")
    fun getReviewPendingCount(currentTime: Long): Flow<Int>

    @Query("SELECT * FROM cards WHERE status != 'NEW' AND nextReviewTime <= :currentTime ORDER BY nextReviewTime ASC LIMIT :limit")
    fun getReviewCards(currentTime: Long, limit: Int): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE status = 'NEW' LIMIT :limit")
    fun getNewCards(limit: Int): Flow<List<CardEntity>>
    @Query("SELECT MAX(id) FROM cards")
    suspend fun getMaxId(): Int?
}
