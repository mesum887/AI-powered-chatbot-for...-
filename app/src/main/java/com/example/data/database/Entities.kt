package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String = "General"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val sessionId: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val subject: String = "General",
    val isOffline: Boolean = false
)

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey val id: String,
    val deckName: String,
    val front: String,
    val back: String,
    val nextReviewTime: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0
)

@Entity(tableName = "quiz_histories")
data class QuizHistory(
    @PrimaryKey val id: String,
    val title: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String = "General"
)

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey val id: String,
    val task: String,
    val targetMinutes: Int = 30,
    val spentMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val dateString: String // YYYY-MM-DD
)
