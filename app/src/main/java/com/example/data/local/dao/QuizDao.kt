package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getQuestionsByCategory(category: String): List<QuestionEntity>

    @Query("SELECT DISTINCT category FROM questions")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionsCount(): Int

    // Statistics
    @Query("SELECT * FROM statistics WHERE id = 1")
    fun getStatistics(): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStatistics(stats: StatisticsEntity)

    // User Preferences
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getUserPreferences(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserPreferences(prefs: UserPreferencesEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongAnswer(wrongAnswer: WrongAnswerEntity)
}
