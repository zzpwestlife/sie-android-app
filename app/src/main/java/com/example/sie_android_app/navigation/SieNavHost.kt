package com.example.sie_android_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.sie.feature.home.navigation.homeRoute
import com.example.sie.feature.home.navigation.homeScreen
import com.example.sie.feature.home.navigation.bookmarkedScreen
import com.example.sie.feature.home.navigation.navigateToBookmarked
import com.example.sie.feature.home.navigation.wrongQuestionsScreen
import com.example.sie.feature.home.navigation.navigateToWrongQuestions
import com.example.sie.feature.study.navigation.studyScreen
import com.example.sie.feature.study.navigation.navigateToStudy
import com.example.sie.feature.exam.navigation.examScreen
import com.example.sie.feature.exam.navigation.examHistoryScreen
import com.example.sie.feature.exam.navigation.examDetailScreen
import com.example.sie.feature.exam.navigation.navigateToExam
import com.example.sie.feature.exam.navigation.navigateToExamHistory
import com.example.sie.feature.exam.navigation.navigateToExamDetail
import com.example.sie.feature.stats.navigation.statsScreen
import com.example.sie.feature.stats.navigation.navigateToStats
import com.example.sie.feature.settings.navigation.settingsScreen
import com.example.sie.feature.settings.navigation.navigateToSettings
import com.example.sie.feature.card.navigation.cardScreen
import com.example.sie.feature.card.navigation.navigateToCard
import com.example.sie.feature.card.navigation.cardLearningScreen
import com.example.sie.feature.card.navigation.navigateToCardLearning
import com.example.sie.feature.card.navigation.cardCreateScreen
import com.example.sie.feature.card.navigation.navigateToCardCreate
import com.example.sie.feature.chapter.navigation.chapterSelectionScreen
import com.example.sie.feature.chapter.navigation.navigateToChapterSelection

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
            onTopicSelectionClick = { navController.navigateToChapterSelection() },
            onMockExamClick = { navController.navigateToExam() },
            onStatsClick = { navController.navigateToStats() },
            onBookmarkedClick = { navController.navigateToBookmarked() },
            onWrongQuestionsClick = { navController.navigateToWrongQuestions() },
            onFlashcardsClick = { navController.navigateToCard() },
            onSettingsClick = { navController.navigateToSettings() }
        )
        chapterSelectionScreen(
            onBackClick = { navController.popBackStack() },
            onStartStudy = { selectedCategories ->
                navController.navigateToStudy(selectedCategories)
            }
        )
        studyScreen(
            onBackClick = { navController.popBackStack() }
        )
        examScreen(
            onBackClick = { navController.popBackStack() }
        )
        examHistoryScreen(
            onBackClick = { navController.popBackStack() },
            onExamClick = { examResultId -> navController.navigateToExamDetail(examResultId) }
        )
        examDetailScreen(
            onBackClick = { navController.popBackStack() }
        )
        bookmarkedScreen(
            onBackClick = { navController.popBackStack() }
        )
        wrongQuestionsScreen(
            onBackClick = { navController.popBackStack() }
        )
        cardScreen(
            onBackClick = { navController.popBackStack() },
            onStartLearning = { navController.navigateToCardLearning() },
            onCreateCard = { navController.navigateToCardCreate() }
        )
        cardLearningScreen(
            onBackClick = { navController.popBackStack() }
        )
        cardCreateScreen(
            onBackClick = { navController.popBackStack() }
        )
        statsScreen(
            onExamHistoryClick = { navController.navigateToExamHistory() },
            onExamResultClick = { examResultId -> navController.navigateToExamDetail(examResultId) }
        )
        settingsScreen()
    }
}
