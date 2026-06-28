package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.QuizResultDao
import com.example.data.local.dao.QuizDao
import com.example.data.local.entity.QuizResultEntity
import com.example.data.local.entity.*

@Database(
    entities = [
        QuizResultEntity::class,
        QuestionEntity::class,
        WrongAnswerEntity::class,
        BookmarkEntity::class,
        UserPreferencesEntity::class,
        StatisticsEntity::class,
        AchievementEntity::class
    ], 
    version = 4, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizResultDao(): QuizResultDao
    abstract fun quizDao(): QuizDao
}
