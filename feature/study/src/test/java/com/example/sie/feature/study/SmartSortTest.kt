package com.example.sie.feature.study

import com.example.sie.core.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartSortTest {

    private fun smartSort(questions: List<Question>): List<Question> {
        return questions.sortedWith(
            compareByDescending<Question> { it.isWrong }
                .thenByDescending { it.wrongCount }
                .thenBy { it.lastStudiedAt ?: 0L }
                .thenBy { it.id }
        )
    }

    @Test
    fun `wrong questions appear first`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = false),
            createQuestion(id = 2, isWrong = true),
            createQuestion(id = 3, isWrong = false)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // Wrong question first
    }

    @Test
    fun `higher wrongCount appears first among wrong questions`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = true, wrongCount = 1),
            createQuestion(id = 2, isWrong = true, wrongCount = 3),
            createQuestion(id = 3, isWrong = true, wrongCount = 2)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // wrongCount=3
        assertEquals(3, sorted[1].id) // wrongCount=2
        assertEquals(1, sorted[2].id) // wrongCount=1
    }

    @Test
    fun `unstudied questions appear before studied among correct questions`() {
        val questions = listOf(
            createQuestion(id = 1, lastStudiedAt = 1000L),
            createQuestion(id = 2, lastStudiedAt = null),
            createQuestion(id = 3, lastStudiedAt = 500L)
        )

        val sorted = smartSort(questions)

        assertEquals(2, sorted[0].id) // Unstudied (null)
        assertEquals(3, sorted[1].id) // Older studied
        assertEquals(1, sorted[2].id) // Recent studied
    }

    @Test
    fun `complete sorting order is correct`() {
        val questions = listOf(
            createQuestion(id = 1, isWrong = false, lastStudiedAt = 1000L),
            createQuestion(id = 2, isWrong = true, wrongCount = 2),
            createQuestion(id = 3, isWrong = true, wrongCount = 3),
            createQuestion(id = 4, isWrong = false, lastStudiedAt = null),
            createQuestion(id = 5, isWrong = false, lastStudiedAt = 500L)
        )

        val sorted = smartSort(questions)

        // Expected order: 3 (wrong, count=3), 2 (wrong, count=2), 4 (unstudied), 5 (old), 1 (recent)
        assertEquals(listOf(3, 2, 4, 5, 1), sorted.map { it.id })
    }

    private fun createQuestion(
        id: Int,
        isWrong: Boolean = false,
        wrongCount: Int = 0,
        lastStudiedAt: Long? = null
    ) = Question(
        id = id,
        content = "Question $id",
        options = listOf("A", "B", "C", "D"),
        correctAnswerIndex = 0,
        explanation = "Explanation",
        category = "Test",
        isWrong = isWrong,
        wrongCount = wrongCount,
        lastStudiedAt = lastStudiedAt
    )
}
