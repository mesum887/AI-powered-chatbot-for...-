package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.database.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class StudyRepository(
    private val chatDao: ChatDao,
    private val noteDao: NoteDao,
    private val flashcardDao: FlashcardDao,
    private val quizDao: QuizDao,
    private val goalDao: GoalDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // --- Chat Sessions & Messages ---
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()
    val favoriteMessages: Flow<List<ChatMessage>> = chatDao.getFavoriteMessages()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String, subject: String): String {
        val id = UUID.randomUUID().toString()
        chatDao.insertSession(ChatSession(id, title, System.currentTimeMillis(), subject))
        return id
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun sendMessage(sessionId: String, userText: String, systemPrompt: String? = null, isComplex: Boolean = false): String {
        // 1. Insert User Message
        val userMsgId = UUID.randomUUID().toString()
        chatDao.insertMessage(
            ChatMessage(userMsgId, sessionId, "user", userText, System.currentTimeMillis())
        )

        // 2. Call Gemini
        val aiResponse = GeminiClient.getAiResponse(
            prompt = userText,
            systemInstruction = systemPrompt,
            isComplex = isComplex
        )

        // 3. Insert AI Message
        val aiMsgId = UUID.randomUUID().toString()
        chatDao.insertMessage(
            ChatMessage(aiMsgId, sessionId, "ai", aiResponse, System.currentTimeMillis())
        )

        return aiResponse
    }

    suspend fun toggleMessageFavorite(msgId: String, isFavorite: Boolean) {
        chatDao.updateMessageFavorite(msgId, isFavorite)
    }

    // --- Study Notes ---
    val allNotes: Flow<List<StudyNote>> = noteDao.getAllNotes()
    val offlineNotes: Flow<List<StudyNote>> = noteDao.getOfflineNotes()
    val favoriteNotes: Flow<List<StudyNote>> = noteDao.getFavoriteNotes()

    suspend fun saveNote(title: String, content: String, subject: String, isOffline: Boolean) {
        val id = UUID.randomUUID().toString()
        noteDao.insertNote(StudyNote(id, title, content, System.currentTimeMillis(), false, subject, isOffline))
    }

    suspend fun deleteNote(noteId: String) {
        noteDao.deleteNote(noteId)
    }

    suspend fun toggleNoteFavorite(noteId: String, isFavorite: Boolean) {
        noteDao.updateNoteFavorite(noteId, isFavorite)
    }

    // --- Flashcards ---
    val allFlashcards: Flow<List<Flashcard>> = flashcardDao.getAllFlashcards()
    val allDeckNames: Flow<List<String>> = flashcardDao.getAllDeckNames()

    fun getFlashcardsByDeck(deckName: String): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsByDeck(deckName)

    fun getDueFlashcards(now: Long = System.currentTimeMillis()): Flow<List<Flashcard>> =
        flashcardDao.getDueFlashcards(now)

    suspend fun addFlashcard(deckName: String, front: String, back: String) {
        val card = Flashcard(
            id = UUID.randomUUID().toString(),
            deckName = deckName,
            front = front,
            back = back
        )
        flashcardDao.insertFlashcard(card)
    }

    suspend fun reviewFlashcard(cardId: Flashcard, score: Int) {
        // Spaced Repetition Logic (SuperMemo-2 SM2 algorithm variant)
        // score is 1-5 where 5 is perfect memory, 1 is total blackout
        val q = score.coerceIn(0, 5)
        var reps = cardId.repetitions
        var interval = cardId.intervalDays
        var ef = cardId.easeFactor

        if (q >= 3) {
            if (reps == 0) {
                interval = 1
            } else if (reps == 1) {
                interval = 6
            } else {
                interval = (interval * ef).toInt().coerceAtLeast(1)
            }
            reps += 1
        } else {
            reps = 0
            interval = 1
        }

        ef = (ef + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f))).coerceAtLeast(1.3f)

        val updatedCard = cardId.copy(
            repetitions = reps,
            intervalDays = interval,
            easeFactor = ef,
            nextReviewTime = System.currentTimeMillis() + (interval * 24L * 60 * 60 * 1000)
        )
        flashcardDao.updateFlashcard(updatedCard)
    }

    suspend fun deleteFlashcard(id: String) {
        flashcardDao.deleteFlashcard(id)
    }

    // --- Quiz History ---
    val quizHistories: Flow<List<QuizHistory>> = quizDao.getAllQuizHistories()

    suspend fun saveQuizHistory(title: String, score: Int, total: Int, subject: String) {
        quizDao.insertQuizHistory(
            QuizHistory(UUID.randomUUID().toString(), title, score, total, System.currentTimeMillis(), subject)
        )
    }

    suspend fun clearQuizHistory() {
        quizDao.clearHistory()
    }

    // --- Study Goals ---
    fun getGoalsByDate(dateString: String): Flow<List<StudyGoal>> =
        goalDao.getGoalsByDate(dateString)

    suspend fun addGoal(task: String, targetMin: Int, dateString: String) {
        goalDao.insertGoal(
            StudyGoal(UUID.randomUUID().toString(), task, targetMin, 0, false, dateString)
        )
    }

    suspend fun updateGoal(goal: StudyGoal) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: String) {
        goalDao.deleteGoal(id)
    }

    // --- AI Feature Requests helper ---

    suspend fun aiSummarizeNotes(content: String): String {
        val prompt = "Summarize the following study notes in detail. Structure it with bullet points, a clear overview, and core concepts:\n\n$content"
        return GeminiClient.getAiResponse(prompt, "You are StudyMind AI, an expert academic summarizer.")
    }

    suspend fun aiExplainTopicSimple(topic: String): String {
        val prompt = "Explain the topic '$topic' in extremely simple terms, as if explaining to a 10-year-old. Use clear analogies and examples."
        return GeminiClient.getAiResponse(prompt, "You are StudyMind AI, a teacher who makes complex ideas simple and fun.")
    }

    suspend fun aiSolveMath(expression: String): String {
        val prompt = "Solve this mathematical problem step-by-step with clear explanation for each step:\n\n$expression"
        return GeminiClient.getAiResponse(prompt, "You are StudyMind AI, a step-by-step math solver.")
    }

    suspend fun aiCheckGrammar(text: String): String {
        val prompt = "Analyze and correct the grammar of the following text. List the errors found and provide the polished version:\n\n$text"
        return GeminiClient.getAiResponse(prompt, "You are StudyMind AI, a precise grammar checker and editor.")
    }

    suspend fun aiTranslate(text: String, toUrdu: Boolean): String {
        val direction = if (toUrdu) "English to Urdu" else "Urdu to English"
        val prompt = "Translate the following text accurately from $direction. Maintain appropriate tone and context:\n\n$text"
        return GeminiClient.getAiResponse(prompt, "You are StudyMind AI, a fluent bilingual translator.")
    }

    // JSON parsing helper for generated quizzes
    data class GeneratedQuestion(
        val question: String,
        val options: List<String>,
        val answerIndex: Int,
        val explanation: String
    )

    suspend fun aiGenerateQuiz(notesContent: String): List<GeneratedQuestion> {
        val prompt = """
            Generate exactly 5 multiple choice questions from the following notes content.
            Return ONLY a raw JSON array matching this format:
            [
              {
                "question": "Question text here?",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "answerIndex": 0,
                "explanation": "Why Option A is correct"
              }
            ]
            Do not enclose in markdown codeblocks. Just raw json content.
            Notes content:
            $notesContent
        """.trimIndent()

        val jsonStr = GeminiClient.getAiResponse(prompt, "You are a professional quiz generator.", isComplex = true, jsonMode = true)
        
        // Let's strip markdown wrappers if they are present in response (just in case)
        val cleanedJson = jsonStr.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val listType = Types.newParameterizedType(List::class.java, GeneratedQuestion::class.java)
            val adapter = moshi.adapter<List<GeneratedQuestion>>(listType)
            adapter.fromJson(cleanedJson) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: parse manually or return dummy questions
            emptyList()
        }
    }
}
