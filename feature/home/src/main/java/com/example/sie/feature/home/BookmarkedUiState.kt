package com.example.sie.feature.home

import com.example.sie.core.model.Question

sealed interface BookmarkedUiState {
    data object Loading : BookmarkedUiState
    data object Empty : BookmarkedUiState
    data class Success(
        val questions: List<Question>,
        val categories: List<String>,
        val selectedCategory: String? = null
    ) : BookmarkedUiState
}
