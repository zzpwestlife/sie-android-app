package com.example.sie.feature.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyStats(
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val startTime: Long = System.currentTimeMillis()
)

sealed interface StudyUiState {
    data object Loading : StudyUiState
    data class Success(
        val currentQuestion: Question,
        val selectedOptionIndex: Int? = null,
        val isAnswerRevealed: Boolean = false,
        val isCorrect: Boolean = false,
        val stats: StudyStats = StudyStats(),
        val hasPrevious: Boolean = false
    ) : StudyUiState
    data class Error(val message: String) : StudyUiState
}

private data class StudyHistoryItem(
    var question: Question,
    var selectedOptionIndex: Int? = null,
    var isAnswerRevealed: Boolean = false,
    var isCorrect: Boolean = false
)

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()
    
    // Language state
    private val _language = MutableStateFlow("zh")
    val language: StateFlow<String> = _language.asStateFlow()

    private val history = mutableListOf<StudyHistoryItem>()
    // Cache all questions to avoid repeated DB calls and ensure no immediate repeats
    private var allQuestionsCache: List<Question> = emptyList()
    // Queue of shuffled questions to be served
    private val availableQuestions = mutableListOf<Question>()

    private var currentIndex = -1
    private var stats = StudyStats()

    // Support for chapter-based learning
    private var selectedCategories: List<String>? = null

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { userData ->
                _language.value = userData.language
            }
        }
        initializeQuestions()
    }

    fun startChapterStudy(categories: List<String>) {
        selectedCategories = categories
        initializeChapterQuestions()
    }

    private fun initializeChapterQuestions() {
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            try {
                val categories = selectedCategories ?: run {
                    // Fallback to all questions if no categories specified
                    initializeQuestions()
                    return@launch
                }

                questionRepository.getQuestionsByCategories(categories).collect { questions ->
                    if (questions.isNotEmpty()) {
                        allQuestionsCache = smartSortQuestions(questions)

                        if (history.isEmpty()) {
                            availableQuestions.clear()
                            availableQuestions.addAll(allQuestionsCache)
                            loadNextQuestion()
                        }

                        this.cancel()
                    }
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = StudyUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun smartSortQuestions(questions: List<Question>): List<Question> {
        return questions.sortedWith(
            compareByDescending<Question> { it.isWrong }           // 1. Wrong questions first
                .thenByDescending { it.wrongCount }                // 2. Higher error count first
                .thenBy { it.lastStudiedAt ?: 0L }                 // 3. Unstudied or oldest first
                .thenBy { it.id }                                   // 4. Stable sort by ID
        )
    }

    private fun initializeQuestions() {
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            try {
                // Observe the database until questions are available
                // This handles the race condition where DatabaseCallback is still populating data
                questionRepository.getAllQuestions().collect { all ->
                    if (all.isNotEmpty()) {
                        allQuestionsCache = all
                        
                        // Only initialize if we haven't started yet
                        if (history.isEmpty()) {
                            availableQuestions.clear()
                            availableQuestions.addAll(all.shuffled())
                            loadNextQuestion()
                        }
                        
                        // Once we have data, stop observing to avoid unexpected updates during practice
                        this.cancel()
                    }
                    // If list is empty, we stay in Loading state waiting for population
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _uiState.value = StudyUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadNextQuestion() {
        if (currentIndex < history.size - 1) {
            // Load from history (moving forward)
            currentIndex++
            updateUiState()
        } else {
            // Load new question
            if (availableQuestions.isEmpty()) {
                // Reshuffle all questions if exhausted (Infinite mode)
                if (allQuestionsCache.isNotEmpty()) {
                    availableQuestions.addAll(allQuestionsCache.shuffled())
                }
            }
            
            if (availableQuestions.isNotEmpty()) {
                val nextQuestion = availableQuestions.removeAt(0)
                history.add(StudyHistoryItem(nextQuestion))
                currentIndex++
                updateUiState()
            }
        }
    }

    fun loadPreviousQuestion() {
        if (currentIndex > 0) {
            currentIndex--
            updateUiState()
        }
    }

    fun selectOption(index: Int) {
        val currentItem = history.getOrNull(currentIndex) ?: return

        if (!currentItem.isAnswerRevealed) {
            val isCorrect = index == currentItem.question.correctAnswerIndex

            // Update history item
            currentItem.selectedOptionIndex = index
            currentItem.isAnswerRevealed = true
            currentItem.isCorrect = isCorrect

            // Update stats
            stats = stats.copy(
                totalAnswered = stats.totalAnswered + 1,
                correctCount = if (isCorrect) stats.correctCount + 1 else stats.correctCount
            )

            // Mark question as studied
            viewModelScope.launch {
                try {
                    questionRepository.markQuestionAsStudied(currentItem.question.id)
                } catch (_: Exception) {
                    // Silently ignore
                }
            }

            // Mark wrong questions in database for review later
            if (!isCorrect) {
                viewModelScope.launch {
                    try {
                        questionRepository.markAsWrong(currentItem.question.id)
                    } catch (_: Exception) {
                        // Silently ignore — marking wrong questions is non-critical
                    }
                }
            }

            updateUiState()
        }
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            try {
                questionRepository.toggleBookmark(questionId)
                // Update the bookmark state in the current history item
                val currentItem = history.getOrNull(currentIndex) ?: return@launch
                val updatedQuestion = currentItem.question.copy(
                    isBookmarked = !currentItem.question.isBookmarked
                )
                currentItem.question = updatedQuestion
                updateUiState()
            } catch (_: Exception) {
                // Silently handle bookmark toggle failure
            }
        }
    }

    private fun updateUiState() {
        val currentItem = history.getOrNull(currentIndex) ?: return

        _uiState.value = StudyUiState.Success(
            currentQuestion = currentItem.question,
            selectedOptionIndex = currentItem.selectedOptionIndex,
            isAnswerRevealed = currentItem.isAnswerRevealed,
            isCorrect = currentItem.isCorrect,
            stats = stats,
            hasPrevious = currentIndex > 0
        )
    }
}
