package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessage
import com.example.data.database.ChatSession
import com.example.data.database.Flashcard
import com.example.data.database.QuizHistory
import com.example.data.database.StudyGoal
import com.example.data.database.StudyNote
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppScreen {
    AUTH,
    DASHBOARD,
    CHAT,
    NOTES,
    FLASHCARDS,
    POMODORO,
    AI_TOOLS,
    SETTINGS,
    UPGRADE
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = StudyRepository(
        db.chatDao(),
        db.noteDao(),
        db.flashcardDao(),
        db.quizDao(),
        db.goalDao()
    )

    private val prefs = application.getSharedPreferences("studymind_prefs", Context.MODE_PRIVATE)

    // --- User Session & App Configurations ---
    private val _currentUserEmail = MutableStateFlow(prefs.getString("user_email", "") ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _streak = MutableStateFlow(prefs.getInt("streak_count", 1))
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("language", "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedScreen = MutableStateFlow(AppScreen.AUTH)
    val selectedScreen: StateFlow<AppScreen> = _selectedScreen.asStateFlow()

    // --- Daily Usage tracking (Free Tier Limits) ---
    private val _dailyChatCount = MutableStateFlow(prefs.getInt("daily_chat_count", 0))
    val dailyChatCount: StateFlow<Int> = _dailyChatCount.asStateFlow()

    private val lastUsageDate = prefs.getString("last_usage_date", "")

    init {
        // Handle streak and reset usage limits daily
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (lastUsageDate != today) {
            _dailyChatCount.value = 0
            prefs.edit().putInt("daily_chat_count", 0).putString("last_usage_date", today).apply()

            // Handle streak check
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
            val streakDate = prefs.getString("streak_date", "")
            if (streakDate == yesterday) {
                // Keep streak going or increment when studying today
            } else if (streakDate != today) {
                // Streak broken, but we keep it at at least 1 when active today
                _streak.value = 1
                prefs.edit().putInt("streak_count", 1).apply()
            }
        }

        // If user was already signed in, skip auth screen
        if (_currentUserEmail.value.isNotEmpty()) {
            _selectedScreen.value = AppScreen.DASHBOARD
        }
    }

    // --- Reactive Database Streams ---
    val allSessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<StudyNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteNotes: StateFlow<List<StudyNote>> = repository.favoriteNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineNotes: StateFlow<List<StudyNote>> = repository.offlineNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeckNames: StateFlow<List<String>> = repository.allDeckNames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizHistories: StateFlow<List<QuizHistory>> = repository.quizHistories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active states ---
    val currentSessionId = MutableStateFlow<String?>(null)

    val currentMessages: StateFlow<List<ChatMessage>> = currentSessionId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForSession(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Goals ---
    private val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayGoals: StateFlow<List<StudyGoal>> = repository.getGoalsByDate(todayString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Pomodoro State ---
    private val _pomodoroSecondsLeft = MutableStateFlow(25 * 60)
    val pomodoroSecondsLeft: StateFlow<Int> = _pomodoroSecondsLeft.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroMode = MutableStateFlow("Study") // "Study" or "Break"
    val pomodoroMode: StateFlow<String> = _pomodoroMode.asStateFlow()

    // --- Active Quiz State ---
    private val _activeQuizQuestions = MutableStateFlow<List<StudyRepository.GeneratedQuestion>>(emptyList())
    val activeQuizQuestions: StateFlow<List<StudyRepository.GeneratedQuestion>> = _activeQuizQuestions.asStateFlow()

    private val _quizCurrentIndex = MutableStateFlow(0)
    val quizCurrentIndex: StateFlow<Int> = _quizCurrentIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizGenerating = MutableStateFlow(false)
    val isQuizGenerating: StateFlow<Boolean> = _isQuizGenerating.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _summarizedResult = MutableStateFlow<String?>(null)
    val summarizedResult: StateFlow<String?> = _summarizedResult.asStateFlow()

    // --- Active Tool States ---
    private val _explanationResult = MutableStateFlow<String?>(null)
    val explanationResult: StateFlow<String?> = _explanationResult.asStateFlow()

    private val _mathResult = MutableStateFlow<String?>(null)
    val mathResult: StateFlow<String?> = _mathResult.asStateFlow()

    private val _grammarResult = MutableStateFlow<String?>(null)
    val grammarResult: StateFlow<String?> = _grammarResult.asStateFlow()

    private val _translationResult = MutableStateFlow<String?>(null)
    val translationResult: StateFlow<String?> = _translationResult.asStateFlow()

    private val _isToolLoading = MutableStateFlow(false)
    val isToolLoading: StateFlow<Boolean> = _isToolLoading.asStateFlow()

    // --- Selected Flashcard Deck ---
    val selectedDeck = MutableStateFlow<String?>(null)
    val flashcardsInSelectedDeck: StateFlow<List<Flashcard>> = selectedDeck
        .flatMapLatest { deckName ->
            if (deckName != null) repository.getFlashcardsByDeck(deckName) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Authentication ---
    fun registerOrLogin(name: String, email: String) {
        _currentUserEmail.value = email
        _currentUserName.value = name
        prefs.edit()
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
        _selectedScreen.value = AppScreen.DASHBOARD
    }

    fun logout() {
        _currentUserEmail.value = ""
        _currentUserName.value = ""
        prefs.edit()
            .remove("user_email")
            .remove("user_name")
            .apply()
        _selectedScreen.value = AppScreen.AUTH
    }

    fun selectScreen(screen: AppScreen) {
        _selectedScreen.value = screen
    }

    // --- Premium Flow ---
    fun togglePremium() {
        val newStatus = !_isPremium.value
        _isPremium.value = newStatus
        prefs.edit().putBoolean("is_premium", newStatus).apply()
    }

    // --- Chat Functions ---
    fun selectChatSession(sessionId: String?) {
        currentSessionId.value = sessionId
        if (sessionId != null) {
            _selectedScreen.value = AppScreen.CHAT
        }
    }

    fun startNewChat(title: String, subject: String) {
        viewModelScope.launch {
            val id = repository.createNewSession(title, subject)
            selectChatSession(id)
        }
    }

    fun sendChatMessage(text: String, subject: String = "General") {
        if (text.trim().isEmpty()) return

        val sessionId = currentSessionId.value ?: return

        // Daily Limit Check for Free Users
        if (!_isPremium.value && _dailyChatCount.value >= 10) {
            viewModelScope.launch {
                db.chatDao().insertMessage(
                    ChatMessage(
                        UUID.randomUUID().toString(),
                        sessionId,
                        "ai",
                        "You have reached your daily free limit of 10 AI chats. Please upgrade to StudyMind Pro for unlimited instant AI chats, document summaries, and advanced features!",
                        System.currentTimeMillis()
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            // Track Usage
            if (!_isPremium.value) {
                val newCount = _dailyChatCount.value + 1
                _dailyChatCount.value = newCount
                prefs.edit().putInt("daily_chat_count", newCount).apply()
            }

            // Study active increment
            incrementStreak()

            val promptPrefix = "You are StudyMind AI, a helpful tutor in $subject. Answer in Urdu or English as requested by user language context. Be conversational, clean, and exact."
            repository.sendMessage(sessionId, text, promptPrefix)
        }
    }

    fun deleteChat(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (currentSessionId.value == sessionId) {
                currentSessionId.value = null
            }
        }
    }

    fun toggleMessageFavorite(msgId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleMessageFavorite(msgId, isFav)
        }
    }

    // --- Notes & PDF Summary ---
    fun summarizeTextNotes(title: String, content: String, subject: String, isOffline: Boolean) {
        if (content.trim().isEmpty()) return
        _isSummarizing.value = true
        _selectedScreen.value = AppScreen.NOTES

        viewModelScope.launch {
            val summary = repository.aiSummarizeNotes(content)
            _summarizedResult.value = summary
            repository.saveNote(title, summary, subject, isOffline)
            _isSummarizing.value = false
            incrementStreak()
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    fun toggleNoteFav(noteId: String, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleNoteFavorite(noteId, isFav)
        }
    }

    // --- Quiz Generation ---
    fun generateQuizFromNotes(notesContent: String, title: String, subject: String) {
        if (notesContent.trim().isEmpty()) return
        _isQuizGenerating.value = true
        _quizCurrentIndex.value = 0
        _quizScore.value = 0
        _activeQuizQuestions.value = emptyList()

        viewModelScope.launch {
            val quiz = repository.aiGenerateQuiz(notesContent)
            _activeQuizQuestions.value = quiz
            _isQuizGenerating.value = false
            incrementStreak()
        }
    }

    fun answerQuizQuestion(selectedOptionIndex: Int) {
        val questions = _activeQuizQuestions.value
        val currentIndex = _quizCurrentIndex.value
        if (questions.isNotEmpty() && currentIndex < questions.size) {
            val correctIndex = questions[currentIndex].answerIndex
            if (selectedOptionIndex == correctIndex) {
                _quizScore.value += 1
            }
            _quizCurrentIndex.value += 1

            // Finished quiz! Save score
            if (_quizCurrentIndex.value >= questions.size) {
                saveQuizScore("Quiz: " + (if (questions.isNotEmpty()) questions[0].question.take(20) + "..." else "AI Study Quiz"), _quizScore.value, questions.size)
            }
        }
    }

    fun saveQuizScore(title: String, score: Int, total: Int) {
        viewModelScope.launch {
            repository.saveQuizHistory(title, score, total, "General")
        }
    }

    // --- Flashcard Review ---
    fun addFlashcard(deckName: String, front: String, back: String) {
        viewModelScope.launch {
            repository.addFlashcard(deckName, front, back)
        }
    }

    fun rateFlashcard(card: Flashcard, score: Int) {
        viewModelScope.launch {
            repository.reviewFlashcard(card, score)
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            repository.deleteFlashcard(cardId)
        }
    }

    // --- Planner Goals ---
    fun addStudyGoal(task: String, minutes: Int) {
        viewModelScope.launch {
            repository.addGoal(task, minutes, todayString)
        }
    }

    fun toggleGoalCompleted(goal: StudyGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(isCompleted = !goal.isCompleted))
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    // --- AI Utilities ---
    fun explainTopic(topic: String) {
        if (topic.trim().isEmpty()) return
        _isToolLoading.value = true
        viewModelScope.launch {
            _explanationResult.value = repository.aiExplainTopicSimple(topic)
            _isToolLoading.value = false
            incrementStreak()
        }
    }

    fun solveMath(expression: String) {
        if (expression.trim().isEmpty()) return
        _isToolLoading.value = true
        viewModelScope.launch {
            _mathResult.value = repository.aiSolveMath(expression)
            _isToolLoading.value = false
            incrementStreak()
        }
    }

    fun checkGrammar(text: String) {
        if (text.trim().isEmpty()) return
        _isToolLoading.value = true
        viewModelScope.launch {
            _grammarResult.value = repository.aiCheckGrammar(text)
            _isToolLoading.value = false
            incrementStreak()
        }
    }

    fun translate(text: String, toUrdu: Boolean) {
        if (text.trim().isEmpty()) return
        _isToolLoading.value = true
        viewModelScope.launch {
            _translationResult.value = repository.aiTranslate(text, toUrdu)
            _isToolLoading.value = false
            incrementStreak()
        }
    }

    // --- Pomodoro Clock Tick ---
    fun tickPomodoro() {
        if (!_isPomodoroRunning.value) return
        val sec = _pomodoroSecondsLeft.value
        if (sec > 0) {
            _pomodoroSecondsLeft.value = sec - 1
        } else {
            // Finished current timer segment!
            _isPomodoroRunning.value = false
            if (_pomodoroMode.value == "Study") {
                _pomodoroMode.value = "Break"
                _pomodoroSecondsLeft.value = 5 * 60 // 5 min default break
                // Log minutes to streak progress
                viewModelScope.launch {
                    incrementStreak()
                    // Update target goals if any
                    val activeGoals = todayGoals.value
                    if (activeGoals.isNotEmpty()) {
                        val firstUncompleted = activeGoals.firstOrNull { !it.isCompleted } ?: activeGoals[0]
                        repository.updateGoal(firstUncompleted.copy(spentMinutes = firstUncompleted.spentMinutes + 25))
                    }
                }
            } else {
                _pomodoroMode.value = "Study"
                _pomodoroSecondsLeft.value = 25 * 60
            }
        }
    }

    fun togglePomodoro() {
        _isPomodoroRunning.value = !_isPomodoroRunning.value
    }

    fun resetPomodoro() {
        _isPomodoroRunning.value = false
        _pomodoroMode.value = "Study"
        _pomodoroSecondsLeft.value = 25 * 60
    }

    fun setPomodoroTime(minutes: Int) {
        _isPomodoroRunning.value = false
        _pomodoroSecondsLeft.value = minutes * 60
    }

    // --- Streak & settings ---
    private fun incrementStreak() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val streakDate = prefs.getString("streak_date", "")
        if (streakDate != today) {
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
            val currentCount = if (streakDate == yesterday) _streak.value + 1 else 1
            _streak.value = currentCount
            prefs.edit()
                .putString("streak_date", today)
                .putInt("streak_count", currentCount)
                .apply()
        }
    }

    fun toggleTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        prefs.edit().putBoolean("dark_theme", newTheme).apply()
    }

    fun changeLanguage(lang: String) {
        _language.value = lang
        prefs.edit().putString("language", lang).apply()
    }
}
