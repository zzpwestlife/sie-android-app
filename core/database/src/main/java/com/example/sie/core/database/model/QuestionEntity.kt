package com.example.sie.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.sie.core.model.Question
import org.json.JSONArray
import org.json.JSONException

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["isBookmarked", "category", "id"], name = "index_bookmark_category"),
        Index(value = ["isWrong", "wrongCount", "category", "id"], name = "index_wrong_category"),
        Index(value = ["category", "lastStudiedAt", "id"], name = "index_category_studied")
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content: String,
    val options: String, // Stored as JSON string
    val correctAnswerIndex: Int,
    val explanation: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val isWrong: Boolean = false,
    val wrongCount: Int = 0,  // Track wrong answer count
    val lastStudiedAt: Long? = null  // NEW: Timestamp in milliseconds
)

fun QuestionEntity.asExternalModel() = Question(
    id = id,
    content = content,
    options = try {
        val jsonArray = JSONArray(options)
        List(jsonArray.length()) { jsonArray.getString(it) }
    } catch (e: JSONException) {
        listOf()
    },
    correctAnswerIndex = correctAnswerIndex,
    explanation = explanation,
    category = category,
    isBookmarked = isBookmarked,
    isWrong = isWrong,
    wrongCount = wrongCount,
    lastStudiedAt = lastStudiedAt
)
