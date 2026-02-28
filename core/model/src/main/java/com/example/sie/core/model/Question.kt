package com.example.sie.core.model

data class Question(
    val id: Int,
    val content: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false
) {
    fun getLocalizedContent(language: String): String {
        val parts = content.split("\n")
        return if (language == "zh") {
            parts.lastOrNull() ?: content
        } else {
            parts.firstOrNull() ?: content
        }
    }

    fun getOption(index: Int, language: String): String {
        if (index !in options.indices) return ""
        val parts = options[index].split("\n")
        return if (language == "zh") {
            parts.lastOrNull() ?: options[index]
        } else {
            parts.firstOrNull() ?: options[index]
        }
    }

    fun getExplanation(language: String): String {
        val parts = explanation.split("\n")
        return if (language == "zh") {
            parts.lastOrNull() ?: explanation
        } else {
            parts.firstOrNull() ?: explanation
        }
    }
}

