package com.example.di

import androidx.room.Room
import com.example.data.local.QuizDatabase
import com.example.data.repository.QuizRepositoryImpl
import com.example.data.repository.AIAssistantRepositoryImpl
import com.example.domain.repository.AIAssistantRepository
import com.example.domain.repository.QuizRepository
import com.example.presentation.quiz.QuizViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.example.data.remote.AuthManager
import com.example.presentation.multiplayer.MultiplayerViewModel

val appModule = module {
    single { AuthManager(androidContext()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            QuizDatabase::class.java,
            "quiz_database"
        )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    single { get<QuizDatabase>().quizResultDao() }
    single { get<QuizDatabase>().quizDao() }

    single<QuizRepository> { QuizRepositoryImpl(get(), get()) }
    single<AIAssistantRepository> { AIAssistantRepositoryImpl() }

    viewModel { QuizViewModel(get(), get()) }
    viewModel { MultiplayerViewModel(get()) }
}
