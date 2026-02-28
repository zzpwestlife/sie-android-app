package com.example.sie.core.model

data class ExamAnswer(
    val id: Int = 0,
    val examResultId: Int,
    val questionId: Int,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val isFlagged: Boolean = false
)
