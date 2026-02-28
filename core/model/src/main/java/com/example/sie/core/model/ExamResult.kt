package com.example.sie.core.model

data class ExamResult(
    val id: Int,
    val date: Long,
    val score: Int,
    val totalQuestions: Int,
    val correctCount: Int
)
