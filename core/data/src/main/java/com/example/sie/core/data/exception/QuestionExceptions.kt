package com.example.sie.core.data.exception

/**
 * Exception thrown when bookmark operations fail.
 */
class BookmarkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when wrong question operations fail.
 */
class WrongQuestionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
