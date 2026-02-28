package com.example.sie.core.data.repository

import androidx.datastore.core.DataStore
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.UserData
import com.sie.core.datastore.UserPreferences
import com.sie.core.datastore.DarkThemeConfig as DarkThemeConfigProto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineUserDataRepository @Inject constructor(
    private val userPreferences: DataStore<UserPreferences>,
) : UserDataRepository {

    override val userData: Flow<UserData> = userPreferences.data
        .map {
            UserData(
                darkThemeConfig = when (it.darkThemeConfig) {
                    DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT
                    DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
                    else -> DarkThemeConfig.FOLLOW_SYSTEM
                },
                useDynamicColor = it.useDynamicColor,
                fontSizeScale = it.fontSizeScale,
                language = it.language.ifEmpty { "zh" } // Default to zh
            )
        }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferences.updateData {
            it.toBuilder()
                .setDarkThemeConfig(
                    when (darkThemeConfig) {
                        DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                        DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                        DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    }
                )
                .build()
        }
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferences.updateData {
            it.toBuilder()
                .setUseDynamicColor(useDynamicColor)
                .build()
        }
    }

    override suspend fun setFontSizeScale(scale: Int) {
        userPreferences.updateData {
            it.toBuilder()
                .setFontSizeScale(scale)
                .build()
        }
    }

    override suspend fun setLanguage(language: String) {
        userPreferences.updateData {
            it.toBuilder()
                .setLanguage(language)
                .build()
        }
    }
}
