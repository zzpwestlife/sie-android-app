package com.example.sie.core.data.repository

import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userData: Flow<UserData>

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)
    suspend fun setFontSizeScale(scale: Int)
    suspend fun setLanguage(language: String)
}
