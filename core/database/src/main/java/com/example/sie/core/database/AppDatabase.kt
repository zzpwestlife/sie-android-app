package com.example.sie.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sie.core.database.converters.Converters
import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.dao.QuestionDao
import com.example.sie.core.database.model.ExamResultEntity
import com.example.sie.core.database.model.QuestionEntity

@Database(entities = [QuestionEntity::class, ExamResultEntity::class], version = 9, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examResultDao(): ExamResultDao
}
