package com.example.sie.core.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.sie.core.datastore.DarkThemeConfig
import com.sie.core.datastore.UserPreferences
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SiePreferencesDataSource @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) {
    val userData = userPreferences.data
        .map {
            UserData(
                darkThemeConfig = it.darkThemeConfig,
                useDynamicColor = it.useDynamicColor,
                fontSizeScale = it.fontSizeScale,
                language = it.language.ifEmpty { "zh" } // Default to zh
            )
        }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.toBuilder().setDarkThemeConfig(darkThemeConfig).build()
        }
    }

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData {
            it.toBuilder().setUseDynamicColor(useDynamicColor).build()
        }
    }
    
    suspend fun setFontSizeScale(scale: Int) {
        userPreferences.updateData {
            it.toBuilder().setFontSizeScale(scale).build()
        }
    }

    suspend fun setLanguage(language: String) {
        userPreferences.updateData {
            it.toBuilder().setLanguage(language).build()
        }
    }
}

data class UserData(
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val fontSizeScale: Int,
    val language: String
)
