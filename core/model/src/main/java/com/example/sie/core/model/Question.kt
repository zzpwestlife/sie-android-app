package com.example.sie.core.model

data class Question(
    val id: Int,
    val content_en: String,
    val content_zh: String,
    val options_en: List<String>,
    val options_zh: List<String>,
    val correctAnswerIndex: Int,
    val explanation_en: String,
    val explanation_zh: String,
    val category_en: String,
    val category_zh: String,
    val category_short: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,
    val lastStudiedAt: Long? = null
) {
    fun getContent(language: String): String {
        return if (language == "zh") content_zh else content_en
    }

    fun getOptions(language: String): List<String> {
        return if (language == "zh") options_zh else options_en
    }

    fun getOption(index: Int, language: String): String {
        val options = getOptions(language)
        return if (index in options.indices) options[index] else ""
    }

    fun getExplanation(language: String): String {
        return if (language == "zh") explanation_zh else explanation_en
    }

    fun getCategory(language: String): String {
        return if (language == "zh") category_zh else category_en
    }

    fun getCategoryShort(): String {
        return category_short
    }
}
