package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("UPDATE chat_messages SET isFavorite = :isFav WHERE id = :msgId")
    suspend fun updateMessageFavorite(msgId: String, isFav: Boolean)

    @Query("SELECT * FROM chat_messages WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMessages(): Flow<List<ChatMessage>>
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM study_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<StudyNote>>

    @Query("SELECT * FROM study_notes WHERE isOffline = 1 ORDER BY timestamp DESC")
    fun getOfflineNotes(): Flow<List<StudyNote>>

    @Query("SELECT * FROM study_notes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteNotes(): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNote)

    @Query("DELETE FROM study_notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("UPDATE study_notes SET isFavorite = :isFav WHERE id = :noteId")
    suspend fun updateNoteFavorite(noteId: String, isFav: Boolean)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards ORDER BY nextReviewTime ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT DISTINCT deckName FROM flashcards")
    fun getAllDeckNames(): Flow<List<String>>

    @Query("SELECT * FROM flashcards WHERE deckName = :deckName")
    fun getFlashcardsByDeck(deckName: String): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE nextReviewTime <= :now")
    fun getDueFlashcards(now: Long): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("DELETE FROM flashcards WHERE id = :id")
    suspend fun deleteFlashcard(id: String)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_histories ORDER BY timestamp DESC")
    fun getAllQuizHistories(): Flow<List<QuizHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizHistory(history: QuizHistory)

    @Query("DELETE FROM quiz_histories")
    suspend fun clearHistory()
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM study_goals WHERE dateString = :date ORDER BY id ASC")
    fun getGoalsByDate(date: String): Flow<List<StudyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: StudyGoal)

    @Update
    suspend fun updateGoal(goal: StudyGoal)

    @Query("DELETE FROM study_goals WHERE id = :id")
    suspend fun deleteGoal(id: String)
}
