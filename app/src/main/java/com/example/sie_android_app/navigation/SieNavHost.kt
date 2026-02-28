package com.example.sie_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.sie.feature.home.navigation.homeRoute
import com.example.sie.feature.home.navigation.homeScreen
import com.example.sie.feature.study.navigation.studyScreen
import com.example.sie.feature.study.navigation.navigateToStudy
import com.example.sie.feature.exam.navigation.examScreen
import com.example.sie.feature.exam.navigation.navigateToExam
import com.example.sie.feature.stats.navigation.statsScreen
import com.example.sie.feature.stats.navigation.navigateToStats
import com.example.sie.feature.settings.navigation.settingsScreen
import com.example.sie.feature.settings.navigation.navigateToSettings

@Composable
fun SieNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        homeScreen(
            onTopicSelectionClick = { navController.navigateToStudy() },
            onMockExamClick = { navController.navigateToExam() },
            onStatsClick = { navController.navigateToStats() },
            onSettingsClick = { navController.navigateToSettings() }
        )
        studyScreen()
        examScreen(
            onBackClick = { navController.popBackStack() }
        )
        statsScreen()
        settingsScreen()
    }
}
