package com.example.sie.feature.home

import com.example.sie.core.model.Question

sealed interface WrongQuestionsUiState {
    data object Loading : WrongQuestionsUiState
    data object Empty : WrongQuestionsUiState
    data class Success(
        val questions: List<Question>,
        val categories: List<String>,
        val selectedCategory: String? = null,
        val stats: WrongQuestionsStats
    ) : WrongQuestionsUiState
}

data class WrongQuestionsStats(
    val totalCount: Int,           // 题库总数
    val totalWrongCount: Int,      // 错题总数
    val avgWrongCount: Float       // 平均错误次数
)
