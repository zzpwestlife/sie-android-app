package com.example.sie.core.data.repository

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
}
