package com.example.sie_android_app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.sie_android_app.navigation.SieNavHost

@Composable
fun SieApp() {
    val navController = rememberNavController()
    SieNavHost(navController = navController)
}
