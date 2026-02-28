package com.example.sie.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.home.BookmarkedRoute

const val bookmarkedRoute = "bookmarked_route"

fun NavController.navigateToBookmarked(navOptions: NavOptions? = null) {
    this.navigate(bookmarkedRoute, navOptions)
}

fun NavGraphBuilder.bookmarkedScreen(onBackClick: () -> Unit) {
    composable(route = bookmarkedRoute) {
        BookmarkedRoute(onBackClick = onBackClick)
    }
}
