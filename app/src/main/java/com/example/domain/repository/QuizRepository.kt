package com.example.domain.repository

import com.example.domain.model.Question
import com.example.domain.model.QuizResult
import com.example.data.local.entity.StatisticsEntity
import com.example.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun getQuestionsByCategory(category: String): List<Question>
    suspend fun getAllCategories(): List<String>
    fun getRecentResults(): Flow<List<QuizResult>>
    fun getTopScores(): Flow<List<QuizResult>>
    suspend fun saveResult(result: QuizResult)
    suspend fun clearAll()
    
    fun getStatistics(): Flow<StatisticsEntity?>
    suspend fun updateStatistics(stats: StatisticsEntity)
    
    fun getUserPreferences(): Flow<UserPreferencesEntity?>
    suspend fun updateUserPreferences(prefs: UserPreferencesEntity)
    
    suspend fun saveWrongAnswer(questionId: Int)
}
