package com.example.sie.core.model

data class Question(
    val id: Int,
    val content: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,  // Track wrong answer count
    val lastStudiedAt: Long? = null  // NEW: Timestamp in milliseconds
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
        if (!explanation.contains("\n")) return explanation
        
        val parts = explanation.split("\n")
        if (parts.size == 2) {
             return if (language == "zh") parts[1] else parts[0]
        }
        
        // Handle multi-line explanations by detecting Chinese characters
        val firstChineseLineIndex = parts.indexOfFirst { it.any { char -> char.code in 0x4E00..0x9FFF } }
        
        if (firstChineseLineIndex == -1) {
            // No Chinese characters found, return full text
            return explanation
        }
        
        return if (language == "zh") {
            parts.subList(firstChineseLineIndex, parts.size).joinToString("\n")
        } else {
            parts.subList(0, firstChineseLineIndex).joinToString("\n")
        }
    }
}

