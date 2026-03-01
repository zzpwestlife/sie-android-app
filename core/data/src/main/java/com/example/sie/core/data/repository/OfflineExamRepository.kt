package com.example.sie.core.data.repository

import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.model.ExamAnswerEntity
import com.example.sie.core.database.model.ExamResultEntity
import com.example.sie.core.database.model.asExternalModel
import com.example.sie.core.model.ExamAnswer
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

    override suspend fun saveExamResultWithAnswers(examResult: ExamResult, answers: List<ExamAnswer>) {
        val resultEntity = ExamResultEntity(
            date = examResult.date,
            score = examResult.score,
            totalQuestions = examResult.totalQuestions,
            correctCount = examResult.correctCount
        )
        val answerEntities = answers.map { answer ->
            ExamAnswerEntity(
                examResultId = 0, // Will be set by DAO transaction
                questionId = answer.questionId,
                selectedOptionIndex = answer.selectedOptionIndex,
                isCorrect = answer.isCorrect,
                isFlagged = answer.isFlagged,
                isAnswered = answer.isAnswered
            )
        }
        examResultDao.insertExamResultWithAnswers(resultEntity, answerEntities)
    }

    override fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswer>> =
        examResultDao.getExamAnswers(examResultId).map { entities ->
            entities.map { entity ->
                ExamAnswer(
                    id = entity.id,
                    examResultId = entity.examResultId,
                    questionId = entity.questionId,
                    selectedOptionIndex = entity.selectedOptionIndex,
                    isCorrect = entity.isCorrect,
                    isFlagged = entity.isFlagged,
                    isAnswered = entity.isAnswered
                )
            }
        }

    override fun getExamResultById(id: Int): Flow<ExamResult?> =
        examResultDao.getExamResultById(id).map { entity ->
            entity?.asExternalModel()
        }

    override suspend fun clearAllExamHistory() {
        examResultDao.clearAllExamHistory()
    }

    override suspend fun getRecentExamQuestionIds(limit: Int): List<Int> {
        return examResultDao.getRecentExamQuestionIds(limit)
    }
}
