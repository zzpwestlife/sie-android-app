package com.example.sie.feature.exam.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.exam.ExamRoute

const val examRoute = "exam_route"

fun NavController.navigateToExam(navOptions: NavOptions? = null) {
    this.navigate(examRoute, navOptions)
}

fun NavGraphBuilder.examScreen(onBackClick: () -> Unit) {
    composable(route = examRoute) {
        ExamRoute(onBackClick = onBackClick)
    }
}
