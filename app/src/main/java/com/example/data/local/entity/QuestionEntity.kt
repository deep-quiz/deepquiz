package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Question

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

fun QuestionEntity.toDomainModel(): Question {
    return Question(
        id = id,
        category = category,
        text = text,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation
    )
}

fun Question.toEntity(): QuestionEntity {
    return QuestionEntity(
        id = id,
        category = category,
        text = text,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation
    )
}
