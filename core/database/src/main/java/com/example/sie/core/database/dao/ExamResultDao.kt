package com.example.sie.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.sie.core.database.model.ExamAnswerEntity
import com.example.sie.core.database.model.ExamResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results ORDER BY date DESC")
    fun getExamResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(examResult: ExamResultEntity)

    @Insert
    suspend fun insertExamResultAndGetId(examResult: ExamResultEntity): Long

    @Insert
    suspend fun insertExamAnswers(answers: List<ExamAnswerEntity>)

    @Query("SELECT * FROM exam_answers WHERE examResultId = :examResultId")
    fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswerEntity>>

    @Query("SELECT * FROM exam_results WHERE id = :id")
    fun getExamResultById(id: Int): Flow<ExamResultEntity?>
}
