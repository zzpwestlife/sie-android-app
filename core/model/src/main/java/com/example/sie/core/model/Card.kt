package com.example.sie.core.model

enum class CardStatus {
    NEW,
    LEARNING,
    REVIEWING,
    MASTERED
}

data class Card(
    val id: Int,
    val front: String,
    val back: String,
    val category: String,
    val image: String? = null,
    val status: CardStatus = CardStatus.NEW,
    val nextReviewTime: Long = 0,
    val reviewCount: Int = 0,
    val proficiency: Int = 0 // 0-100, can be used to determine MASTERED
)
