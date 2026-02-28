package com.example.sie.core.data.repository

import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.model.ExamResultEntity
import com.example.sie.core.database.model.asExternalModel
import com.example.sie.core.model.ExamResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineExamRepository @Inject constructor(
    private val examResultDao: ExamResultDao
) : ExamRepository {
    override fun getExamResults(): Flow<List<ExamResult>> =
        examResultDao.getExamResults().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun saveExamResult(examResult: ExamResult) {
        examResultDao.insertExamResult(
            ExamResultEntity(
                date = examResult.date,
                score = examResult.score,
                totalQuestions = examResult.totalQuestions,
                correctCount = examResult.correctCount
            )
        )
    }
}
