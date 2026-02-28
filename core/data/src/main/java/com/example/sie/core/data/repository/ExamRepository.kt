package com.example.sie.core.data.repository

import com.example.sie.core.model.ExamResult
import kotlinx.coroutines.flow.Flow

interface ExamRepository {
    fun getExamResults(): Flow<List<ExamResult>>
    suspend fun saveExamResult(examResult: ExamResult)
}
