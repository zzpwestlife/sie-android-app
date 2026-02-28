package com.example.sie.feature.study.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sie.feature.study.StudyRoute
import java.net.URLDecoder
import java.net.URLEncoder

private const val URL_CHARACTER_ENCODING = "UTF-8"

class StudyArgs(val categories: List<String>) {
    constructor(savedStateHandle: SavedStateHandle) : this(
        URLDecoder.decode(checkNotNull(savedStateHandle[STUDY_CATEGORIES_ARG]), URL_CHARACTER_ENCODING).split(",")
    )
}

const val STUDY_CATEGORIES_ARG = "study_categories"
const val studyRoute = "study_route/{$STUDY_CATEGORIES_ARG}"

fun NavController.navigateToStudy(categories: List<String>, navOptions: NavOptions? = null) {
    val encodedCategories = URLEncoder.encode(categories.joinToString(","), URL_CHARACTER_ENCODING)
    val route = "study_route/$encodedCategories"
    android.util.Log.d("StudyNavigation", "Navigating to: $route (original categories: $categories)")
    this.navigate(route, navOptions)
}

fun NavGraphBuilder.studyScreen(onBackClick: () -> Unit) {
    composable(
        route = studyRoute,
        arguments = listOf(
            navArgument(STUDY_CATEGORIES_ARG) { type = NavType.StringType }
        )
    ) {
        StudyRoute(onBackClick = onBackClick)
    }
}
