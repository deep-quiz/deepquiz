package com.example.domain.repository

interface AIAssistantRepository {
    suspend fun getTopicSummary(topic: String): String
    suspend fun getExplanation(question: String, correctAnswer: String): String
    suspend fun getTutorHint(question: String, options: List<String>): String
    suspend fun generateQuizQuestions(topic: String, count: Int, difficulty: String): String
    suspend fun getPerformanceAnalysis(correct: Int, wrong: Int, category: String): String
}
