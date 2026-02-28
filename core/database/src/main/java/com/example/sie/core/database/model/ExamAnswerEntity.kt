package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sie.core.model.ExamAnswer

@Entity(
    tableName = "exam_answers",
    foreignKeys = [ForeignKey(
        entity = ExamResultEntity::class,
        parentColumns = ["id"],
        childColumns = ["examResultId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["examResultId"])]
)
data class ExamAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)

fun ExamAnswerEntity.asExternalModel() = ExamAnswer(
    id = id,
    examResultId = examResultId,
    questionId = questionId,
    selectedOptionIndex = selectedOptionIndex,
    isCorrect = isCorrect,
    isFlagged = isFlagged
)

fun ExamAnswer.asEntity() = ExamAnswerEntity(
    id = id,
    examResultId = examResultId,
    questionId = questionId,
    selectedOptionIndex = selectedOptionIndex,
    isCorrect = isCorrect,
    isFlagged = isFlagged
)
