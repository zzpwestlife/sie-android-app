package com.example.sie.feature.exam.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.sie.feature.exam.ExamDetailRoute
import com.example.sie.feature.exam.ExamHistoryRoute
import com.example.sie.feature.exam.ExamRoute

const val examRoute = "exam_route"
const val examHistoryRoute = "exam_history_route"
const val examDetailRoute = "exam_detail_route/{examResultId}"

fun NavController.navigateToExam(navOptions: NavOptions? = null) {
    this.navigate(examRoute, navOptions)
}

fun NavController.navigateToExamHistory(navOptions: NavOptions? = null) {
    this.navigate(examHistoryRoute, navOptions)
}

fun NavController.navigateToExamDetail(examResultId: Int, navOptions: NavOptions? = null) {
    this.navigate("exam_detail_route/$examResultId", navOptions)
}

fun NavGraphBuilder.examScreen(onBackClick: () -> Unit) {
    composable(route = examRoute) {
        ExamRoute(onBackClick = onBackClick)
    }
}

fun NavGraphBuilder.examHistoryScreen(
    onBackClick: () -> Unit,
    onExamClick: (Int) -> Unit
) {
    composable(route = examHistoryRoute) {
        ExamHistoryRoute(
            onBackClick = onBackClick,
            onExamClick = onExamClick
        )
    }
}

fun NavGraphBuilder.examDetailScreen(
    onBackClick: () -> Unit
) {
    composable(
        route = examDetailRoute,
        arguments = listOf(navArgument("examResultId") { type = NavType.IntType })
    ) {
        ExamDetailRoute(onBackClick = onBackClick)
    }
}
