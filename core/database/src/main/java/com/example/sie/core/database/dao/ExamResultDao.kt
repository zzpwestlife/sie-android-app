package com.example.sie.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sie.core.database.model.ExamAnswerEntity
import com.example.sie.core.database.model.ExamResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExamResultDao {
    @Query("SELECT * FROM exam_results ORDER BY date DESC")
    abstract fun getExamResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExamResult(examResult: ExamResultEntity)

    @Insert
    abstract suspend fun insertExamResultAndGetId(examResult: ExamResultEntity): Long

    @Insert
    abstract suspend fun insertExamAnswers(answers: List<ExamAnswerEntity>)

    @Query("SELECT * FROM exam_answers WHERE examResultId = :examResultId")
    abstract fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswerEntity>>

    @Query("SELECT * FROM exam_results WHERE id = :id")
    abstract fun getExamResultById(id: Int): Flow<ExamResultEntity?>

    @Transaction
    open suspend fun insertExamResultWithAnswers(
        examResult: ExamResultEntity,
        answers: List<ExamAnswerEntity>
    ): Long {
        val resultId = insertExamResultAndGetId(examResult)
        insertExamAnswers(answers.map { it.copy(examResultId = resultId.toInt()) })
        return resultId
    }

    // Data management operations
    @Query("DELETE FROM exam_results")
    abstract suspend fun clearExamResults()

    @Query("DELETE FROM exam_answers")
    abstract suspend fun clearExamAnswers()

    @Transaction
    open suspend fun clearAllExamHistory() {
        clearExamAnswers()
        clearExamResults()
    }
}
