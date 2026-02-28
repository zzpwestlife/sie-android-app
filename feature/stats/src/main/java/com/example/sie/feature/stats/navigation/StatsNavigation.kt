package com.example.sie.feature.stats.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.stats.StatsRoute

const val statsRoute = "stats_route"

fun NavController.navigateToStats(navOptions: NavOptions? = null) {
    this.navigate(statsRoute, navOptions)
}

fun NavGraphBuilder.statsScreen() {
    composable(route = statsRoute) {
        StatsRoute()
    }
}
