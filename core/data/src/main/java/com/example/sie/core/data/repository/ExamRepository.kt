package com.example.sie.core.data.repository

import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun getExamResults(): Flow<List<ExamResult>>
    suspend fun saveExamResult(examResult: ExamResult)
    suspend fun saveExamResultWithAnswers(examResult: ExamResult, answers: List<ExamAnswer>)
    fun getExamAnswers(examResultId: Int): Flow<List<ExamAnswer>>
    fun getExamResultById(id: Int): Flow<ExamResult?>
    suspend fun clearAllExamHistory()
    suspend fun getRecentExamQuestionIds(limit: Int): List<Int>
}
