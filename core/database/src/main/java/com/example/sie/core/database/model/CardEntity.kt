package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sie.core.model.Card
import com.example.sie.core.model.CardStatus

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey
    val id: Int,
    val front: String,
    val back: String,
    val category: String,
    val image: String? = null,
    val status: CardStatus = CardStatus.NEW,
    val nextReviewTime: Long = 0,
    val reviewCount: Int = 0,
    val proficiency: Int = 0
)

fun CardEntity.asExternalModel() = Card(
    id = id,
    front = front,
    back = back,
    category = category,
    image = image,
    status = status,
    nextReviewTime = nextReviewTime,
    reviewCount = reviewCount,
    proficiency = proficiency
)

fun Card.asEntity() = CardEntity(
    id = id,
    front = front,
    back = back,
    category = category,
    image = image,
    status = status,
    nextReviewTime = nextReviewTime,
    reviewCount = reviewCount,
    proficiency = proficiency
)
