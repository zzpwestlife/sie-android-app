package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sie.core.model.ExamResult

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int
)

fun ExamResultEntity.asExternalModel() = ExamResult(
    id = id,
    date = date,
    score = score,
    totalQuestions = totalQuestions,
    correctCount = correctCount
)
