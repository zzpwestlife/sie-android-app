package com.example.sie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.sie.core.database.converters.Converters
import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.ExamResultEntity
import com.example.sie.core.database.model.QuestionEntity

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE questions ADD COLUMN wrongCount INTEGER NOT NULL DEFAULT 0"
        )
    }
}

@Database(entities = [QuestionEntity::class, ExamResultEntity::class], version = 10, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
}
