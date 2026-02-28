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

            // 1. Assert length >= 251
            assertTrue(
                "Question count should be at least 251, but was ${jsonArray.length()}",
                jsonArray.length() >= 251
            )

            val existingIds = HashSet<Int>()
            val nonAsciiRegex = Regex("[^\\x00-\\x7F]")

            for (i in 0 until jsonArray.length()) {
                val question = jsonArray.getJSONObject(i)

                // 2. Validate Required Fields
                val requiredFields = listOf("id", "content", "options", "correctAnswerIndex", "explanation", "category")
                for (field in requiredFields) {
                    assertTrue("Question at index $i missing field: $field", question.has(field))
                }

                // 3. Validate ID Uniqueness
                val id = question.getInt("id")
                assertTrue("Duplicate ID found: $id", !existingIds.contains(id))
                existingIds.add(id)

                // 4. Validate Bilingual Content (contains newline or non-ASCII)
                val content = question.getString("content")
                val isBilingual = content.contains("\n") || nonAsciiRegex.containsMatchIn(content)
                assertTrue(
                    "Question $id content should be bilingual (contain newline or non-ASCII characters)",
                    isBilingual
                )

                // 5. Validate Options
                val options = question.getJSONArray("options")
                assertTrue("Question $id options length must be >= 2", options.length() >= 2)

                // 6. Validate CorrectAnswerIndex within bounds
                val correctAnswerIndex = question.getInt("correctAnswerIndex")
                assertTrue(
                    "Question $id correctAnswerIndex $correctAnswerIndex out of bounds (options length: ${options.length()})",
                    correctAnswerIndex >= 0 && correctAnswerIndex < options.length()
                )
            }

        } catch (e: Exception) {
            fail("Failed to validate questions.json: ${e.message}")
        }
    }
}
