package com.example.sie.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONArray
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class)
class QuestionBankAssetTest {

    @Test
    fun validateQuestionsJson() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val assetManager = context.assets

        try {
            val inputStream = assetManager.open("questions.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            assertTrue(
                "Question count should be at least 251, but was ${jsonArray.length()}",
                jsonArray.length() >= 251
            )

            val existingIds = HashSet<Int>()

            for (i in 0 until jsonArray.length()) {
                val question = jsonArray.getJSONObject(i)

                val requiredFields = listOf(
                    "id", "content_en", "content_zh", "options_en", "options_zh",
                    "correctAnswerIndex", "explanation_en", "explanation_zh",
                    "category_en", "category_zh", "category_short"
                )
                for (field in requiredFields) {
                    assertTrue("Question at index $i missing field: $field", question.has(field))
                }

                val id = question.getInt("id")
                assertTrue("Duplicate ID found: $id", !existingIds.contains(id))
                existingIds.add(id)

                val content_en = question.getString("content_en")
                val content_zh = question.getString("content_zh")
                assertTrue("Question $id content_en should not be empty", content_en.isNotBlank())
                assertTrue("Question $id content_zh should not be empty", content_zh.isNotBlank())

                val options_en = question.getJSONArray("options_en")
                val options_zh = question.getJSONArray("options_zh")
                assertTrue("Question $id options_en length must be >= 2", options_en.length() >= 2)
                assertTrue("Question $id options_zh length must be >= 2", options_zh.length() >= 2)
                assertTrue("Question $id options_en and options_zh should have same length",
                    options_en.length() == options_zh.length())

                val correctAnswerIndex = question.getInt("correctAnswerIndex")
                assertTrue(
                    "Question $id correctAnswerIndex $correctAnswerIndex out of bounds (options_en length: ${options_en.length()})",
                    correctAnswerIndex >= 0 && correctAnswerIndex < options_en.length()
                )

                val explanation_en = question.getString("explanation_en")
                val explanation_zh = question.getString("explanation_zh")
                assertTrue("Question $id explanation_en should not be empty", explanation_en.isNotBlank())
                assertTrue("Question $id explanation_zh should not be empty", explanation_zh.isNotBlank())

                val category_en = question.getString("category_en")
                val category_zh = question.getString("category_zh")
                val category_short = question.getString("category_short")
                assertTrue("Question $id category_en should not be empty", category_en.isNotBlank())
                assertTrue("Question $id category_zh should not be empty", category_zh.isNotBlank())
                assertTrue("Question $id category_short should not be empty", category_short.isNotBlank())
            }

        } catch (e: Exception) {
            fail("Failed to validate questions.json: ${e.message}")
        }
    }
}
