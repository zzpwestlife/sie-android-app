package com.example.sie.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.home.HomeRoute

const val homeRoute = "home_route"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    this.navigate(homeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onTopicSelectionClick: () -> Unit,
    onMockExamClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    composable(route = homeRoute) {
        HomeRoute(
            onTopicSelectionClick = onTopicSelectionClick,
            onMockExamClick = onMockExamClick,
            onStatsClick = onStatsClick,
            onSettingsClick = onSettingsClick,
        )
    }
}
