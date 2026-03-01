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
        Index(value = ["isBookmarked", "category_en", "id"], name = "index_bookmark_category"),
        Index(value = ["isWrong", "wrongCount", "category_en", "id"], name = "index_wrong_category"),
        Index(value = ["category_en", "lastStudiedAt", "id"], name = "index_category_studied")
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val content_en: String,
    val content_zh: String,
    val options_en: String,
    val options_zh: String,
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
)

fun QuestionEntity.asExternalModel() = Question(
    id = id,
    content_en = content_en,
    content_zh = content_zh,
    options_en = try {
        val jsonArray = JSONArray(options_en)
        List(jsonArray.length()) { jsonArray.getString(it) }
    } catch (e: JSONException) {
        listOf()
    },
    options_zh = try {
        val jsonArray = JSONArray(options_zh)
        List(jsonArray.length()) { jsonArray.getString(it) }
    } catch (e: JSONException) {
        listOf()
    },
    correctAnswerIndex = correctAnswerIndex,
    explanation_en = explanation_en,
    explanation_zh = explanation_zh,
    category_en = category_en,
    category_zh = category_zh,
    category_short = category_short,
    isBookmarked = isBookmarked,
    isWrong = isWrong,
    wrongCount = wrongCount,
    lastStudiedAt = lastStudiedAt
)
