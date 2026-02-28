package com.example.sie.core.data.repository

import com.example.sie.core.data.exception.BookmarkException
import com.example.sie.core.data.exception.WrongQuestionException
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.asExternalModel
import com.example.sie.core.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineQuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) : QuestionRepository {

    override fun getAllQuestions(): Flow<List<Question>> =
        questionDao.getAllQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun getAllQuestionsList(): List<Question> =
        questionDao.getAllQuestionsList().map { it.asExternalModel() }

    override suspend fun getQuestion(id: Int): Question? =
        questionDao.getQuestionById(id)?.asExternalModel()

    override fun getRandomQuestions(limit: Int): Flow<List<Question>> =
        questionDao.getRandomQuestions(limit).map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getBookmarkedQuestions(): Flow<List<Question>> =
        questionDao.getBookmarkedQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override fun getWrongQuestions(): Flow<List<Question>> =
        questionDao.getWrongQuestions().map { entities ->
            entities.map { it.asExternalModel() }
        }

    override suspend fun toggleBookmark(questionId: Int) {
        try {
            questionDao.toggleBookmark(questionId)
        } catch (e: Exception) {
            throw BookmarkException("Failed to toggle bookmark for question $questionId", e)
        }
    }

    override suspend fun markAsWrong(questionId: Int) {
        try {
            questionDao.markAsWrong(questionId)
        } catch (e: Exception) {
            throw WrongQuestionException("Failed to mark question $questionId as wrong", e)
        }
    }

    override suspend fun markAsWrongBatch(questionIds: List<Int>) {
        try {
            questionDao.markAsWrongBatch(questionIds)
        } catch (e: Exception) {
            // Retry individually
            val failures = mutableListOf<Int>()
            questionIds.forEach { id ->
                try {
                    questionDao.markAsWrong(id)
                } catch (ex: Exception) {
                    failures.add(id)
                }
            }
            if (failures.isNotEmpty()) {
                throw WrongQuestionException(
                    "Failed to mark ${failures.size} questions as wrong: $failures"
                )
            }
        }
    }

    override suspend fun removeFromWrong(questionId: Int) {
        questionDao.removeFromWrong(questionId)
    }

    override fun countBookmarked(): Flow<Int> {
        return questionDao.countBookmarked()
    }

    override fun countWrong(): Flow<Int> {
        return questionDao.countWrong()
    }

    override fun getAllCategories(): Flow<List<String>> {
        return questionDao.getAllCategories()
    }

    override fun getQuestionsByCategories(categories: List<String>): Flow<List<Question>> {
        return questionDao.getQuestionsByCategories(categories).map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun markQuestionAsStudied(questionId: Int) {
        questionDao.updateLastStudiedAt(questionId, System.currentTimeMillis())
    }
}
