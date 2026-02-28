package com.example.sie.core.data.repository

import com.example.sie.core.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getAllQuestions(): Flow<List<Question>>
    suspend fun getAllQuestionsList(): List<Question>
    suspend fun getQuestion(id: Int): Question?
    fun getRandomQuestions(limit: Int): Flow<List<Question>>
}
