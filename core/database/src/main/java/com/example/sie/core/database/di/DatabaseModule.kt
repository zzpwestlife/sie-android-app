package com.example.sie.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.sie.core.database.AppDatabase
import com.example.sie.core.database.DatabaseCallback
import com.example.sie.core.database.MIGRATION_9_10
import com.example.sie.core.database.MIGRATION_10_11
import com.example.sie.core.database.MIGRATION_11_12
import com.example.sie.core.database.dao.CardDao
import com.example.sie.core.database.dao.ExamResultDao
import com.example.sie.core.database.dao.QuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<AppDatabase>,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "sie-database"
    )
    .addCallback(
        DatabaseCallback(
            context,
            databaseProvider,
            CoroutineScope(Dispatchers.IO + SupervisorJob())
        )
    )
    .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
    .fallbackToDestructiveMigration()
    .build()

    @Provides
    fun providesQuestionDao(
        database: AppDatabase,
    ): QuestionDao = database.questionDao()

    @Provides
    fun providesExamResultDao(
        database: AppDatabase,
    ): ExamResultDao = database.examResultDao()

    @Provides
    fun providesCardDao(
        database: AppDatabase,
    ): CardDao = database.cardDao()
}
