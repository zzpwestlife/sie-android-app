package com.example.sie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sie.core.database.converters.Converters
import com.example.sie.core.database.dao.CardDao
import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.CardEntity
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

@Database(entities = [QuestionEntity::class, ExamResultEntity::class, CardEntity::class], version = 12, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun cardDao(): CardDao
}
