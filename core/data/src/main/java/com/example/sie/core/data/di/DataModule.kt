package com.example.sie.core.data.di

import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.OfflineExamRepository
import com.example.sie.core.data.repository.OfflineQuestionRepository
import com.example.sie.core.data.repository.OfflineUserDataRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindQuestionRepository(
        questionRepository: OfflineQuestionRepository
    ): QuestionRepository

    @Binds
    abstract fun bindUserDataRepository(
        userDataRepository: OfflineUserDataRepository
    ): UserDataRepository

    @Binds
    abstract fun bindExamRepository(
        examRepository: OfflineExamRepository
    ): ExamRepository
}
