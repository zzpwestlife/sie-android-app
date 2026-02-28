package com.example.sie.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.home.WrongQuestionsRoute

const val wrongQuestionsRoute = "wrong_questions_route"

fun NavController.navigateToWrongQuestions(navOptions: NavOptions? = null) {
    this.navigate(wrongQuestionsRoute, navOptions)
}

fun NavGraphBuilder.wrongQuestionsScreen(onBackClick: () -> Unit) {
    composable(route = wrongQuestionsRoute) {
        WrongQuestionsRoute(onBackClick = onBackClick)
    }
}
