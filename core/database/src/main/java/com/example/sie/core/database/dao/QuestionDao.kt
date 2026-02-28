package com.example.sie.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sie.core.database.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Int): QuestionEntity?
    
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuestions(limit: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isWrong = 1")
    fun getWrongQuestions(): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET isBookmarked = NOT isBookmarked WHERE id = :questionId")
    suspend fun toggleBookmark(questionId: Int)

    @Query("UPDATE questions SET isWrong = 1 WHERE id IN (:questionIds)")
    suspend fun markAsWrong(questionIds: List<Int>)


    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsList(): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("DELETE FROM questions")
    suspend fun clearAllQuestions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)
}
