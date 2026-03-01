package com.example.sie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sie.core.database.converters.Converters
import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.ExamAnswerEntity
import com.example.sie.core.database.model.ExamResultEntity
import com.example.sie.core.database.model.QuestionEntity

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN wrongCount INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add indices for performance optimization
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_bookmark_category ON questions(isBookmarked, category, id)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_wrong_category ON questions(isWrong, wrongCount, category, id)"
        )
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cards` (
                `id` INTEGER NOT NULL,
                `front` TEXT NOT NULL,
                `back` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `image` TEXT,
                `status` TEXT NOT NULL DEFAULT 'NEW',
                `nextReviewTime` INTEGER NOT NULL DEFAULT 0,
                `reviewCount` INTEGER NOT NULL DEFAULT 0,
                `proficiency` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add lastStudiedAt column
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN lastStudiedAt INTEGER"
        )

        // Add index for chapter-based queries
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS index_category_studied ON questions(category, lastStudiedAt, id)"
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exam_answers` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `examResultId` INTEGER NOT NULL,
                `questionId` INTEGER NOT NULL,
                `selectedOptionIndex` INTEGER NOT NULL,
                `isCorrect` INTEGER NOT NULL DEFAULT 0,
                `isFlagged` INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(`examResultId`) REFERENCES `exam_results`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_exam_answers_examResultId` ON `exam_answers`(`examResultId`)"
        )
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE exam_answers ADD COLUMN isAnswered INTEGER NOT NULL DEFAULT 1"
        )
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop cards table - flashcard feature removed
        database.execSQL("DROP TABLE IF EXISTS cards")
    }
}

@Database(entities = [QuestionEntity::class, ExamResultEntity::class, ExamAnswerEntity::class], version = 16, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
}
