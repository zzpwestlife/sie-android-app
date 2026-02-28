package com.example.sie_android_app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.designsystem.theme.SieTheme
import com.example.sie.core.model.DarkThemeConfig
import com.example.sie.core.model.UserData
import com.example.sie_android_app.ui.SieApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userDataRepository: UserDataRepository

    private var currentLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeLanguageChanges()

        setContent {
            val userData by userDataRepository.userData.collectAsState(
                initial = UserData(
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
                    useDynamicColor = false,
                    fontSizeScale = 0,
                    language = "zh"
                )
            )

            SieTheme(
                darkTheme = shouldUseDarkTheme(userData.darkThemeConfig),
                fontScale = userData.fontSizeScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SieApp()
                }
            }
        }
    }
    
    @Composable
    private fun shouldUseDarkTheme(
        darkThemeConfig: DarkThemeConfig,
    ): Boolean = when (darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }

    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            userDataRepository.userData
                .map { it.language }
                .distinctUntilChanged()
                .collect { language ->
                    if (currentLanguage != null && currentLanguage != language) {
                        applyLocale(language)
                        recreate()
                    } else {
                        applyLocale(language)
                        currentLanguage = language
                    }
                }
        }
    }

    private fun applyLocale(languageCode: String) {
        val locale = when (languageCode) {
            "en" -> Locale.ENGLISH
            "zh" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.SIMPLIFIED_CHINESE
        }
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        currentLanguage = languageCode
    }
}
