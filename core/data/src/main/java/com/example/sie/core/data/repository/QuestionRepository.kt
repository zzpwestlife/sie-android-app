package com.example.sie.core.data.repository

import com.example.sie.core.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getAllQuestions(): Flow<List<Question>>
    suspend fun getAllQuestionsList(): List<Question>
    suspend fun getQuestion(id: Int): Question?
    fun getRandomQuestions(limit: Int): Flow<List<Question>>
    fun getBookmarkedQuestions(): Flow<List<Question>>
    fun getWrongQuestions(): Flow<List<Question>>
    suspend fun toggleBookmark(questionId: Int)
    suspend fun markAsWrong(questionId: Int)
    suspend fun markAsWrongBatch(questionIds: List<Int>)
    suspend fun removeFromWrong(questionId: Int)
    fun countBookmarked(): Flow<Int>
    fun countWrong(): Flow<Int>

    // Chapter-based learning
    fun getAllCategories(): Flow<List<String>>
    fun getQuestionsByCategories(categories: List<String>): Flow<List<Question>>
    suspend fun markQuestionAsStudied(questionId: Int)

    // Data management
    suspend fun clearStudyHistory()
    suspend fun clearWrongQuestions()
    suspend fun clearBookmarks()
    suspend fun clearAllUserData()
}
