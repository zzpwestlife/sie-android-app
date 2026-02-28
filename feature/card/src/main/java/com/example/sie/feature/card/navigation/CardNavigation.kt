package com.example.sie.feature.card.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.sie.feature.card.CardCreateRoute
import com.example.sie.feature.card.CardLearningRoute
import com.example.sie.feature.card.CardRoute

const val cardRoute = "card_route"
const val cardLearningRoute = "card_learning_route"
const val cardCreateRoute = "card_create_route"

fun NavController.navigateToCard(navOptions: NavOptions? = null) {
    this.navigate(cardRoute, navOptions)
}

fun NavController.navigateToCardLearning(navOptions: NavOptions? = null) {
    this.navigate(cardLearningRoute, navOptions)
}

fun NavController.navigateToCardCreate(navOptions: NavOptions? = null) {
    this.navigate(cardCreateRoute, navOptions)
}

fun NavGraphBuilder.cardScreen(
    onBackClick: () -> Unit,
    onStartLearning: () -> Unit,
    onCreateCard: () -> Unit
) {
    composable(route = cardRoute) {
        CardRoute(
            onBackClick = onBackClick,
            onStartLearning = onStartLearning,
            onCreateCard = onCreateCard
        )
    }
}

fun NavGraphBuilder.cardLearningScreen(
    onBackClick: () -> Unit
) {
    composable(route = cardLearningRoute) {
        CardLearningRoute(
            onBackClick = onBackClick
        )
    }
}

fun NavGraphBuilder.cardCreateScreen(
    onBackClick: () -> Unit
) {
    composable(route = cardCreateRoute) {
        CardCreateRoute(
            onBackClick = onBackClick
        )
    }
}
