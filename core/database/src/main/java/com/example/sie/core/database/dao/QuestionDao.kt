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

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsList(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Int): QuestionEntity?

    @Query("SELECT * FROM questions WHERE id = :id")
    fun getQuestionByIdFlow(id: Int): Flow<QuestionEntity?>
    
    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuestions(limit: Int): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY category, id")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isWrong = 1 ORDER BY wrongCount DESC, category, id")
    fun getWrongQuestions(): Flow<List<QuestionEntity>>

    @Query("""
        UPDATE questions
        SET isBookmarked = CASE WHEN isBookmarked = 1 THEN 0 ELSE 1 END
        WHERE id = :questionId
    """)
    suspend fun toggleBookmark(questionId: Int)

    @Query("UPDATE questions SET isWrong = 1, wrongCount = wrongCount + 1 WHERE id = :questionId")
    suspend fun markAsWrong(questionId: Int)

    @Query("UPDATE questions SET isWrong = 1, wrongCount = wrongCount + 1 WHERE id IN (:questionIds)")
    suspend fun markAsWrongBatch(questionIds: List<Int>)

    @Query("UPDATE questions SET isWrong = 0 WHERE id = :questionId")
    suspend fun removeFromWrong(questionId: Int)

    @Query("SELECT COUNT(*) FROM questions WHERE isBookmarked = 1")
    fun countBookmarked(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE isWrong = 1")
    fun countWrong(): Flow<Int>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsSync(): List<QuestionEntity>

    @androidx.room.Update
    suspend fun update(question: QuestionEntity)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Query("DELETE FROM questions")
    suspend fun clearAllQuestions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    // Chapter-based learning queries

    @Query("SELECT DISTINCT category FROM questions ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    // Data management operations
    @Query("UPDATE questions SET lastStudiedAt = NULL")
    suspend fun clearStudyHistory()

    @Query("UPDATE questions SET isWrong = 0, wrongCount = 0")
    suspend fun clearWrongQuestions()

    @Query("UPDATE questions SET isBookmarked = 0")
    suspend fun clearBookmarks()

    @Query("UPDATE questions SET lastStudiedAt = NULL, isWrong = 0, wrongCount = 0, isBookmarked = 0")
    suspend fun clearAllUserData()

    @Query("""
        SELECT * FROM questions
        WHERE category IN (:categories)
        ORDER BY
            CASE WHEN isWrong = 1 THEN 0 ELSE 1 END,
            wrongCount DESC,
            CASE WHEN lastStudiedAt IS NULL THEN 0 ELSE 1 END,
            lastStudiedAt ASC,
            id
    """)
    fun getQuestionsByCategories(categories: List<String>): Flow<List<QuestionEntity>>

    @Query("UPDATE questions SET lastStudiedAt = :timestamp WHERE id = :questionId")
    suspend fun updateLastStudiedAt(questionId: Int, timestamp: Long)

    @Query("""
        SELECT * FROM questions
        WHERE category = :category
        ORDER BY
            CASE WHEN isWrong = 1 THEN 0 ELSE 1 END,
            wrongCount DESC,
            CASE WHEN lastStudiedAt IS NULL THEN 0 ELSE lastStudiedAt END,
            id
    """)
    fun getQuestionsByCategorySmartSorted(category: String): Flow<List<QuestionEntity>>
}
