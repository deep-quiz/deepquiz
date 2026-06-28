package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_answers")
data class WrongAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val theme: String = "System",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val language: String = "en"
)

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val id: Int = 1,
    val quizzesTaken: Int = 0,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val xp: Int = 0,
    val coins: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val level: Int = 1
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
