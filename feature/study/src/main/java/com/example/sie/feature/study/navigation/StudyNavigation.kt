package com.example.sie.feature.study.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.study.StudyRoute

const val studyRoute = "study_route"

fun NavController.navigateToStudy(navOptions: NavOptions? = null) {
    this.navigate(studyRoute, navOptions)
}

fun NavGraphBuilder.studyScreen() {
    composable(route = studyRoute) {
        StudyRoute()
    }
}
