package com.example.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Question
import com.example.domain.model.QuizResult
import com.example.domain.repository.AIAssistantRepository
import com.example.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface QuizScreen {
    data class Welcome(
        val categories: List<String>
    ) : QuizScreen

    data class ActiveQuiz(
        val category: String,
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val currentQuestion: Question,
        val selectedOptionIndex: Int?,
        val isAnswered: Boolean,
        val score: Int,
        val timeRemaining: Int,
        val fiftyFiftyUsed: Boolean,
        val skipUsed: Boolean,
        val eliminatedOptions: List<Int>,
        val isPaused: Boolean = false
    ) : QuizScreen

    data class Result(
        val category: String,
        val score: Int,
        val totalQuestions: Int,
        val answeredQuestions: List<Pair<Question, Int?>> // List of Pair (Question, UserSelectedOptionIndex)
    ) : QuizScreen

    object Multiplayer : QuizScreen
}

data class QuizStats(
    val totalQuizzes: Int = 0,
    val averageAccuracy: Float = 0f,
    val highScoresByCategory: Map<String, Int> = emptyMap(),
    val recentResults: List<QuizResult> = emptyList(),
    val level: Int = 1,
    val xp: Int = 0,
    val coins: Int = 0,
    val streak: Int = 0
)

class QuizViewModel(
    private val repository: QuizRepository,
    private val aiRepository: AIAssistantRepository
) : ViewModel() {

    // Screens navigation
    private val _currentScreen = MutableStateFlow<QuizScreen>(QuizScreen.Welcome(emptyList()))
    val currentScreen: StateFlow<QuizScreen> = _currentScreen.asStateFlow()

    private val _aiExplanation = MutableStateFlow<String?>(null)
    val aiExplanation: StateFlow<String?> = _aiExplanation.asStateFlow()

    private val _aiTutorHint = MutableStateFlow<String?>(null)
    val aiTutorHint: StateFlow<String?> = _aiTutorHint.asStateFlow()
    
    private val _aiPerformanceAnalysis = MutableStateFlow<String?>(null)
    val aiPerformanceAnalysis: StateFlow<String?> = _aiPerformanceAnalysis.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private var allCategories = listOf<String>()

    init {
        viewModelScope.launch {
            allCategories = repository.getAllCategories() + "AI Generated"
            _currentScreen.value = QuizScreen.Welcome(allCategories)
        }
    }

    // Database flow for history
    val resultsHistory: StateFlow<List<QuizResult>> = repository.getRecentResults()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived statistics from database
    val stats: StateFlow<QuizStats> = combine(
        resultsHistory,
        repository.getStatistics()
    ) { list, statsEntity ->
        if (list.isEmpty()) {
            QuizStats(
                level = statsEntity?.level ?: 1,
                xp = statsEntity?.xp ?: 0,
                coins = statsEntity?.coins ?: 0,
                streak = statsEntity?.currentStreak ?: 0
            )
        } else {
            val total = list.size
            var totalQuestions = 0
            var totalCorrect = 0
            val highs = mutableMapOf<String, Int>()

            list.forEach { res ->
                totalQuestions += res.totalQuestions
                totalCorrect += res.score
                val currentHigh = highs[res.category] ?: 0
                if (res.score > currentHigh) {
                    highs[res.category] = res.score
                }
            }

            val accuracy = if (totalQuestions > 0) {
                (totalCorrect.toFloat() / totalQuestions) * 100
            } else {
                0f
            }

            QuizStats(
                totalQuizzes = total,
                averageAccuracy = accuracy,
                highScoresByCategory = highs,
                recentResults = list.take(5),
                level = statsEntity?.level ?: 1,
                xp = statsEntity?.xp ?: 0,
                coins = statsEntity?.coins ?: 0,
                streak = statsEntity?.currentStreak ?: 0
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuizStats()
    )

    // Current Active Quiz state
    private var quizCategory: String = "Mixed"
    private var activeQuestionsList = listOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedOptionIndex: Int? = null
    private var isAnswered = false
    private val answeredQuestionsHistory = mutableListOf<Pair<Question, Int?>>()
    
    private var timeRemaining = 15
    private var timerJob: kotlinx.coroutines.Job? = null
    private var fiftyFiftyUsed = false
    private var skipUsed = false
    private var eliminatedOptions = emptyList<Int>()
    private var isPaused = false

    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            timerJob?.cancel()
        } else {
            startTimer()
        }
        updateActiveQuizState()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timeRemaining = 15
        timerJob = viewModelScope.launch {
            while (timeRemaining > 0 && !isAnswered) {
                kotlinx.coroutines.delay(1000)
                timeRemaining--
                updateActiveQuizState()
            }
            if (timeRemaining == 0 && !isAnswered) {
                // Time's up
                isAnswered = true
                answeredQuestionsHistory.add(Pair(activeQuestionsList[currentQuestionIndex], null))
                updateActiveQuizState()
            }
        }
    }

    fun useFiftyFifty() {
        if (fiftyFiftyUsed || isAnswered) return
        fiftyFiftyUsed = true
        val currentQuestion = activeQuestionsList[currentQuestionIndex]
        val correctIndex = currentQuestion.correctAnswerIndex
        val wrongIndices = currentQuestion.options.indices.filter { it != correctIndex }.shuffled()
        eliminatedOptions = wrongIndices.take(2)
        updateActiveQuizState()
    }

    fun useSkip() {
        if (skipUsed || isAnswered) return
        skipUsed = true
        answeredQuestionsHistory.add(Pair(activeQuestionsList[currentQuestionIndex], null))
        if (currentQuestionIndex < activeQuestionsList.lastIndex) {
            currentQuestionIndex++
            resetForNextQuestion()
            updateActiveQuizState()
        } else {
            finishQuiz()
        }
    }

    private fun resetForNextQuestion() {
        selectedOptionIndex = null
        isAnswered = false
        eliminatedOptions = emptyList()
        _aiExplanation.value = null
        _aiTutorHint.value = null
        _aiPerformanceAnalysis.value = null
        startTimer()
    }

    fun navigateToMultiplayer() {
        _currentScreen.value = QuizScreen.Multiplayer
    }

    fun startQuiz(category: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            quizCategory = category
            
            if (category == "AI Generated") {
                val topic = "Random Trivia"
                val rawAiResponse = aiRepository.generateQuizQuestions(topic, 10, "medium")
                // Parse AI response format: Question|Opt1|Opt2|Opt3|Opt4|CorrectIndex
                val aiQuestions = rawAiResponse.lines()
                    .filter { it.contains("|") }
                    .mapIndexedNotNull { index, line ->
                        try {
                            val parts = line.split("|")
                            if (parts.size >= 6) {
                                Question(
                                    id = 1000 + index,
                                    text = parts[0].trim(),
                                    options = listOf(parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim()),
                                    correctAnswerIndex = parts[5].trim().toIntOrNull() ?: 0,
                                    category = "AI Generated",
                                    explanation = "AI generated question."
                                )
                            } else null
                        } catch (e: Exception) { null }
                    }
                activeQuestionsList = if (aiQuestions.isNotEmpty()) aiQuestions else repository.getQuestionsByCategory("Mixed").shuffled().take(10)
            } else {
                val filtered = repository.getQuestionsByCategory(category).shuffled()
                val maxQuestions = if (category == "Mixed") 15 else 10
                activeQuestionsList = filtered.take(maxQuestions)
            }

            _isAiLoading.value = false

            currentQuestionIndex = 0
            score = 0
            fiftyFiftyUsed = false
            skipUsed = false
            answeredQuestionsHistory.clear()
            
            resetForNextQuestion()
            updateActiveQuizState()
        }
    }

    fun selectOption(optionIndex: Int) {
        if (isAnswered) return // Prevent duplicate selection
        timerJob?.cancel() // Stop timer when answered

        val currentQuestion = activeQuestionsList[currentQuestionIndex]
        selectedOptionIndex = optionIndex
        isAnswered = true

        val isCorrect = optionIndex == currentQuestion.correctAnswerIndex
        if (isCorrect) {
            // Combo points logic could go here
            score++
        } else {
            // Log wrong answer to DB
            viewModelScope.launch {
                repository.saveWrongAnswer(currentQuestion.id)
            }
        }

        // Save selection to history of active session
        answeredQuestionsHistory.add(Pair(currentQuestion, optionIndex))

        updateActiveQuizState()
    }

    fun nextQuestion() {
        if (!isAnswered) return // Must answer first

        if (currentQuestionIndex < activeQuestionsList.lastIndex) {
            currentQuestionIndex++
            resetForNextQuestion()
            updateActiveQuizState()
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        // End of Quiz - Save result and go to Result screen
        timerJob?.cancel()
        val finalScore = score
        val finalTotal = activeQuestionsList.size
        val finalCategory = quizCategory
        val finalHistory = answeredQuestionsHistory.toList()

        _currentScreen.value = QuizScreen.Result(
            category = finalCategory,
            score = finalScore,
            totalQuestions = finalTotal,
            answeredQuestions = finalHistory
        )

        // Save to database
        viewModelScope.launch {
            repository.saveResult(
                QuizResult(
                    category = finalCategory,
                    score = finalScore,
                    totalQuestions = finalTotal
                )
            )
            // Update stats
            val currentStats = repository.getStatistics().firstOrNull()
            val xpGained = finalScore * 15
            val coinsGained = finalScore * 5
            val newXp = (currentStats?.xp ?: 0) + xpGained
            val newCoins = (currentStats?.coins ?: 0) + coinsGained
            
            var newStreak = currentStats?.currentStreak ?: 0
            if (finalScore == finalTotal) {
                newStreak++
            } else {
                newStreak = 0
            }
            
            val newLevel = (newXp / 100) + 1
            
            val updatedStats = com.example.data.local.entity.StatisticsEntity(
                id = 1,
                quizzesTaken = (currentStats?.quizzesTaken ?: 0) + 1,
                questionsAnswered = (currentStats?.questionsAnswered ?: 0) + finalTotal,
                correctAnswers = (currentStats?.correctAnswers ?: 0) + finalScore,
                wrongAnswers = (currentStats?.wrongAnswers ?: 0) + (finalTotal - finalScore),
                xp = newXp,
                coins = newCoins,
                currentStreak = newStreak,
                maxStreak = maxOf(newStreak, currentStats?.maxStreak ?: 0),
                level = newLevel
            )
            repository.updateStatistics(updatedStats)
        }
    }
        
    fun quitQuiz() {
        _currentScreen.value = QuizScreen.Welcome(allCategories)
    }

    fun clearStats() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun getExplanation() {
        if (!isAnswered) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val question = activeQuestionsList[currentQuestionIndex]
            val correctOption = question.options[question.correctAnswerIndex]
            _aiExplanation.value = aiRepository.getExplanation(question.text, correctOption)
            _isAiLoading.value = false
        }
    }

    fun getTutorHint() {
        if (isAnswered) return
        viewModelScope.launch {
            _isAiLoading.value = true
            val question = activeQuestionsList[currentQuestionIndex]
            _aiTutorHint.value = aiRepository.getTutorHint(question.text, question.options)
            _isAiLoading.value = false
        }
    }
    
    fun getPerformanceAnalysis() {
        viewModelScope.launch {
            _isAiLoading.value = true
            val correct = score
            val total = activeQuestionsList.size
            val wrong = total - correct
            _aiPerformanceAnalysis.value = aiRepository.getPerformanceAnalysis(correct, wrong, quizCategory)
            _isAiLoading.value = false
        }
    }

    private fun updateActiveQuizState() {
        if (currentQuestionIndex in activeQuestionsList.indices) {
            _currentScreen.value = QuizScreen.ActiveQuiz(
                category = quizCategory,
                currentQuestionIndex = currentQuestionIndex,
                totalQuestions = activeQuestionsList.size,
                currentQuestion = activeQuestionsList[currentQuestionIndex],
                selectedOptionIndex = selectedOptionIndex,
                isAnswered = isAnswered,
                score = score,
                timeRemaining = timeRemaining,
                fiftyFiftyUsed = fiftyFiftyUsed,
                skipUsed = skipUsed,
                eliminatedOptions = eliminatedOptions,
                isPaused = isPaused
            )
        }
    }
}
