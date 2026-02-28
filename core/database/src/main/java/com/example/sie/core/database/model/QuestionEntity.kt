package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sie.core.model.Question

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String
)

fun QuestionEntity.asExternalModel() = Question(
    id = id,
    content = content,
    options = options,
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    category = category
)
