package com.example.sie.core.common.navigation

/**
 * Interface for describing the Sie navigation destinations
 */
interface SieNavigationDestination {
    /**
     * Defines a specific route this destination belongs to.
     * Route is a String that defines the path to your composable.
     * You can think of it as an implicit deep link that leads to that destination.
     * Each destination should have a unique route.
     */
    val route: String

    /**
     * Defines a specific destination ID.
     * This is needed when using nested graphs via the navigation library, but for this app
     * we'll start with flat routes.
     */
    val destination: String
}
