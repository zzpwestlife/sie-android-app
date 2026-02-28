package com.example.sie.core.model

data class UserData(
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val fontSizeScale: Int = 0, // 0 = default
    val language: String = "zh" // Default to Chinese
)
