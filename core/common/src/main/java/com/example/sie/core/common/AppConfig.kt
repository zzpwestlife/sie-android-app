package com.example.sie.core.common

/**
 * Centralized configuration for the application.
 * Adjust values here to change app behavior without searching through feature modules.
 */
object AppConfig {
    // Exam Configuration
    
    /**
     * Duration of the exam in milliseconds.
     * Default: 30 * 60 * 1000L (30 minutes)
     * Debug/Test: 90 * 1000L (1.5 minutes)
     */
    const val EXAM_DURATION_MILLIS = 90 * 1000L // 1.5 minutes for testing
    
    /**
     * Passing score percentage (0-100).
     */
    const val EXAM_PASSING_SCORE = 70
    
    /**
     * Total number of questions in an exam.
     */
    const val EXAM_QUESTION_COUNT = 32
}
