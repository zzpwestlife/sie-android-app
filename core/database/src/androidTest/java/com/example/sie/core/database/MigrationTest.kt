package com.example.sie.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate9To10_addsWrongCountColumn() {
        // Create database at version 9
        helper.createDatabase(TEST_DB, 9).apply {
            // Insert test question without wrongCount
            execSQL(
                """
                INSERT INTO questions (id, content, options, correctAnswerIndex, explanation, category, isBookmarked, isWrong)
                VALUES (1, 'Test?', '["A", "B", "C", "D"]', 0, 'Test explanation', 'Math/Algebra', 0, 0)
                """.trimIndent()
            )
            close()
        }

        // Migrate to version 10
        val db = helper.runMigrationsAndValidate(TEST_DB, 10, true, MIGRATION_9_10)

        // Verify wrongCount column exists with default value 0
        val cursor = db.query("SELECT wrongCount FROM questions WHERE id = 1")
        cursor.use {
            assert(it.moveToFirst())
            val wrongCount = it.getInt(it.getColumnIndexOrThrow("wrongCount"))
            assertEquals(0, wrongCount, "Default wrongCount should be 0")
        }
    }
}
