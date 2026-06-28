package com.example.domain.model

data class Question(
    val id: Int,
    val category: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)
