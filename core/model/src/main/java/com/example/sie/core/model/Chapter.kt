package com.example.sie.core.model

data class Chapter(
    val name: String,              // e.g., "1. Macroeconomics"
    val displayName: String,        // Localized name, e.g., "宏观经济学"
    val totalQuestions: Int,        // Total questions in this chapter
    val studiedQuestions: Int,      // Questions with lastStudiedAt != null
    val correctCount: Int,          // Correctly answered questions
    val wrongCount: Int,            // Total wrong answers
    val accuracyRate: Float,        // Percentage: correctCount / studiedQuestions * 100
    val lastStudiedAt: Long?,       // Latest lastStudiedAt in this chapter
    val isSelected: Boolean = false, // UI state: is this chapter selected?
    val proportion: Float = 0f,     // Percentage of total questions in this chapter
    val includedCategories: List<String> = listOf(name) // List of actual category strings included in this chapter
)

/**
 * Extract localized display name from category string.
 *
 * Categories use format: "1. Macroeconomics\n1. 宏观经济学"
 * - For English (language="en"): returns "1. Macroeconomics"
 * - For Chinese (language="zh"): returns "1. 宏观经济学"
 */
fun String.extractLocalizedName(language: String = "en"): String {
    val parts = this.split("\n")
    return if (language == "zh") {
        parts.lastOrNull() ?: this
    } else {
        parts.firstOrNull() ?: this
    }
}
