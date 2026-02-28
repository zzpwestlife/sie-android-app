package com.example.sie.feature.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sie.core.common.AppConfig
import com.example.sie.core.data.repository.ExamRepository
import com.example.sie.core.data.repository.QuestionRepository
import com.example.sie.core.data.repository.UserDataRepository
import com.example.sie.core.model.ExamAnswer
import com.example.sie.core.model.ExamResult
import com.example.sie.core.model.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val examRepository: ExamRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExamUiState>(ExamUiState.Loading)
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()
    
    // Language state
    private val _language = MutableStateFlow("zh")
    val language: StateFlow<String> = _language.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { userData ->
                _language.value = userData.language
            }
        }
        // Don't auto start, show Intro first
        _uiState.value = ExamUiState.Intro
    }

    fun startExam() {
        _uiState.value = ExamUiState.Loading
        viewModelScope.launch {
            try {
                // Fetch all questions to perform weighted selection
                val allQuestions = questionRepository.getAllQuestionsList()
                
                if (allQuestions.isNotEmpty()) {
                    val selectedQuestions = selectWeightedQuestions(allQuestions)
                    
                    _uiState.value = ExamUiState.InProgress(
                        questions = selectedQuestions,
                        currentQuestionIndex = 0,
                        userAnswers = emptyMap(),
                        timeLeftMillis = AppConfig.EXAM_DURATION_MILLIS
                    )
                    startTimer()
                } else {
                    // Handle empty state
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun selectWeightedQuestions(allQuestions: List<Question>): List<Question> {
        // Weights definition
        val weights = mapOf(
            "1. Macroeconomics / 宏观经济学" to 0.19, // 18-20% -> 19%
            "2. Stocks / 股票" to 0.15,
            "3. ETF / 交易所交易基金" to 0.10,
            "4. Options / 期权" to 0.10,
            "5. Funds / 基金" to 0.10,
            "6. Indexes / 常见指数" to 0.07, // 6-8% -> 7%
            "7. Financial Analysis / 财报分析" to 0.15,
            "8. Revenue & Industry / 营收与行业分析" to 0.07, // 6-8% -> 7%
            "9. Financial Ethics / 金融道德" to 0.04 // 3-5% -> 4%
        )
        
        val totalQuestions = 32
        val selectedQuestions = mutableListOf<Question>()
        
        // Group questions by category
        val questionsByCategory = allQuestions.groupBy { it.category }
        
        // Calculate count for each category
        weights.forEach { (category, weight) ->
            val count = (totalQuestions * weight).toInt()
            val questions = questionsByCategory[category] ?: emptyList()
            if (questions.isNotEmpty()) {
                selectedQuestions.addAll(questions.shuffled().take(count))
            }
        }
        
        // Fill remaining if any (due to rounding or missing categories)
        val remainingCount = totalQuestions - selectedQuestions.size
        if (remainingCount > 0) {
            val alreadySelectedIds = selectedQuestions.map { it.id }.toSet()
            val remainingPool = allQuestions.filter { it.id !in alreadySelectedIds }
            selectedQuestions.addAll(remainingPool.shuffled().take(remainingCount))
        }
        
        return selectedQuestions.shuffled()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (state is ExamUiState.InProgress) {
                        val newTime = state.timeLeftMillis - 1000
                        if (newTime <= 0) {
                            submitExam() // Auto submit
                            return@launch
                        }
                        state.copy(timeLeftMillis = newTime)
                    } else {
                        return@launch
                    }
                }
            }
        }
    }

    fun onAnswerSelected(questionId: Int, answerIndex: Int) {
        _uiState.update { state ->
            if (state is ExamUiState.InProgress) {
                state.copy(
                    userAnswers = state.userAnswers + (questionId to answerIndex)
                )
            } else {
                state
            }
        }
    }

    fun onFlagQuestion(questionId: Int) {
        _uiState.update { state ->
            if (state is ExamUiState.InProgress) {
                val newFlagged = if (state.flaggedQuestions.contains(questionId)) {
                    state.flaggedQuestions - questionId
                } else {
                    state.flaggedQuestions + questionId
                }
                state.copy(flaggedQuestions = newFlagged)
            } else {
                state
            }
        }
    }

    fun toggleBookmark(questionId: Int) {
        viewModelScope.launch {
            questionRepository.toggleBookmark(questionId)
            // Update local state to reflect the change immediately
            _uiState.update { state ->
                when (state) {
                    is ExamUiState.InProgress -> {
                        val updatedQuestions = state.questions.map { 
                            if (it.id == questionId) it.copy(isBookmarked = !it.isBookmarked) else it 
                        }
                        state.copy(questions = updatedQuestions)
                    }
                    is ExamUiState.Finished -> {
                        val updatedQuestions = state.questions.map { 
                            if (it.id == questionId) it.copy(isBookmarked = !it.isBookmarked) else it 
                        }
                        state.copy(questions = updatedQuestions)
                    }
                    else -> state
                }
            }
        }
    }


    fun onNextQuestion() {
        _uiState.update { state ->
            if (state is ExamUiState.InProgress && state.currentQuestionIndex < state.questions.size - 1) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else {
                state
            }
        }
    }

    fun onPreviousQuestion() {
        _uiState.update { state ->
            if (state is ExamUiState.InProgress && state.currentQuestionIndex > 0) {
                state.copy(currentQuestionIndex = state.currentQuestionIndex - 1)
            } else {
                state
            }
        }
    }
    
    fun onQuestionSelected(index: Int) {
         _uiState.update { state ->
            if (state is ExamUiState.InProgress && index in state.questions.indices) {
                state.copy(currentQuestionIndex = index)
            } else {
                state
            }
        }
    }

    fun submitExam() {
        timerJob?.cancel()
        _uiState.update { state ->
            if (state is ExamUiState.InProgress) {
                val correctCount = calculateCorrectCount(state.questions, state.userAnswers)
                val score = if (state.questions.isNotEmpty()) (correctCount * 100) / state.questions.size else 0
                val passed = score >= 70 // 70% passing score

                saveResultWithAnswers(
                    score, state.questions.size, correctCount,
                    state.questions, state.userAnswers, state.flaggedQuestions
                )
                markWrongQuestions(state.questions, state.userAnswers)

                ExamUiState.Finished(
                    score = score, // percentage
                    totalQuestions = state.questions.size,
                    passed = passed,
                    userAnswers = state.userAnswers,
                    questions = state.questions
                )
            } else {
                state
            }
        }
    }

    private fun saveResultWithAnswers(
        score: Int,
        totalQuestions: Int,
        correctCount: Int,
        questions: List<Question>,
        userAnswers: Map<Int, Int>,
        flaggedQuestions: Set<Int>
    ) {
        viewModelScope.launch {
            val answers = questions.map { question ->
                val isAnswered = userAnswers.containsKey(question.id)
                ExamAnswer(
                    examResultId = 0,
                    questionId = question.id,
                    selectedOptionIndex = userAnswers[question.id] ?: -1,
                    isCorrect = isAnswered && userAnswers[question.id] == question.correctAnswerIndex,
                    isFlagged = question.id in flaggedQuestions,
                    isAnswered = isAnswered
                )
            }
            examRepository.saveExamResultWithAnswers(
                ExamResult(
                    id = 0,
                    date = System.currentTimeMillis(),
                    score = score,
                    totalQuestions = totalQuestions,
                    correctCount = correctCount
                ),
                answers
            )
        }
    }

    private fun markWrongQuestions(questions: List<Question>, userAnswers: Map<Int, Int>) {
        val wrongQuestionIds = mutableListOf<Int>()
        val correctQuestionIds = mutableListOf<Int>()

        questions.forEach { question ->
            val userAnswer = userAnswers[question.id]
            if (userAnswer == null || userAnswer != question.correctAnswerIndex) {
                wrongQuestionIds.add(question.id)
            } else {
                correctQuestionIds.add(question.id)
            }
        }

        viewModelScope.launch {
            try {
                // Mark wrong questions
                if (wrongQuestionIds.isNotEmpty()) {
                    questionRepository.markAsWrongBatch(wrongQuestionIds)
                }
                // Clear wrong flag for correct questions
                if (correctQuestionIds.isNotEmpty()) {
                    correctQuestionIds.forEach { id ->
                        questionRepository.removeFromWrong(id)
                    }
                }
            } catch (_: Exception) {
                // 静默处理
            }
        }
    }

    private fun calculateCorrectCount(questions: List<Question>, userAnswers: Map<Int, Int>): Int {
        var correctCount = 0
        questions.forEach { question ->
            val userAnswer = userAnswers[question.id]
            if (userAnswer == question.correctAnswerIndex) {
                correctCount++
            }
        }
        return correctCount
    }
    
    fun resetExam() {
        _uiState.value = ExamUiState.Intro
    }
}

sealed interface ExamUiState {
    data object Loading : ExamUiState
    data object Intro : ExamUiState
    data class InProgress(
        val questions: List<Question>,
        val currentQuestionIndex: Int,
        val userAnswers: Map<Int, Int>,
        val timeLeftMillis: Long,
        val flaggedQuestions: Set<Int> = emptySet()
    ) : ExamUiState
    data class Finished(
        val score: Int,
        val totalQuestions: Int,
        val passed: Boolean,
        val userAnswers: Map<Int, Int>,
        val questions: List<Question>
    ) : ExamUiState
}
