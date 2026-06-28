package com.example.domain.model

data class QuizResult(
    val id: Int = 0,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)
