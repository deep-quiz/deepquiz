package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.QuizResult

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

fun QuizResultEntity.toDomainModel(): QuizResult {
    return QuizResult(
        id = id,
        category = category,
        score = score,
        totalQuestions = totalQuestions,
        timestamp = timestamp
    )
}

fun QuizResult.toEntity(): QuizResultEntity {
    return QuizResultEntity(
        category = category,
        score = score,
        totalQuestions = totalQuestions,
        timestamp = timestamp
    )
}
