package com.example.data.repository

import com.example.data.local.QuestionsData
import com.example.data.local.dao.QuizResultDao
import com.example.data.local.dao.QuizDao
import com.example.data.local.entity.*
import com.example.domain.model.Question
import com.example.domain.model.QuizResult
import com.example.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuizRepositoryImpl(
    private val resultDao: QuizResultDao,
    private val quizDao: QuizDao
) : QuizRepository {

    suspend fun seedDatabaseIfNeeded() {
        if (quizDao.getQuestionsCount() == 0) {
            val entities = QuestionsData.questions.map { it.toEntity() }
            quizDao.insertQuestions(entities)
        }
    }

    override suspend fun getQuestionsByCategory(category: String): List<Question> {
        seedDatabaseIfNeeded() // Ensure we have data
        return if (category == "Mixed") {
            // Fetch all and shuffle for Mixed
            // Since we can't easily fetch all without a query, we'll fetch all categories and combine, or just a new query.
            // Let's add a getAllQuestions to Dao, or we can just fetch all categories and flatten.
            // For now, we know the categories.
            val categories = quizDao.getAllCategories()
            val allQuestions = mutableListOf<QuestionEntity>()
            categories.forEach {
                allQuestions.addAll(quizDao.getQuestionsByCategory(it))
            }
            allQuestions.map { it.toDomainModel() }.shuffled().take(15)
        } else {
            quizDao.getQuestionsByCategory(category).map { it.toDomainModel() }.shuffled()
        }
    }

    override suspend fun getAllCategories(): List<String> {
        seedDatabaseIfNeeded()
        return quizDao.getAllCategories()
    }

    override fun getRecentResults(): Flow<List<QuizResult>> {
        return resultDao.getAllResults().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getTopScores(): Flow<List<QuizResult>> {
        return resultDao.getTopScores().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun saveResult(result: QuizResult) {
        resultDao.insertResult(result.toEntity())
    }
    
    override suspend fun clearAll() {
        resultDao.clearAll()
    }
    
    override fun getStatistics(): Flow<StatisticsEntity?> = quizDao.getStatistics()
    
    override suspend fun updateStatistics(stats: StatisticsEntity) {
        quizDao.updateStatistics(stats)
    }
    
    override fun getUserPreferences(): Flow<UserPreferencesEntity?> = quizDao.getUserPreferences()
    
    override suspend fun updateUserPreferences(prefs: UserPreferencesEntity) {
        quizDao.updateUserPreferences(prefs)
    }
    
    override suspend fun saveWrongAnswer(questionId: Int) {
        quizDao.insertWrongAnswer(WrongAnswerEntity(questionId = questionId))
    }
}
