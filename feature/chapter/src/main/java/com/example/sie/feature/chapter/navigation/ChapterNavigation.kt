package com.example.sie.feature.chapter.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.chapter.ChapterSelectionScreen

const val CHAPTER_SELECTION_ROUTE = "chapter_selection"

fun NavController.navigateToChapterSelection(navOptions: NavOptions? = null) {
    navigate(CHAPTER_SELECTION_ROUTE, navOptions)
}

fun NavGraphBuilder.chapterSelectionScreen(
    onBackClick: () -> Unit,
    onStartStudy: (List<String>) -> Unit
) {
    composable(route = CHAPTER_SELECTION_ROUTE) {
        ChapterSelectionScreen(
            onBackClick = onBackClick,
            onStartStudy = onStartStudy
        )
    }
}
